/*
 * Copyright 2016 The Crossing Project
 *
 * The Crossing Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package club.jmint.crossing.client.call;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import club.jmint.crossing.client.config.ClientCallInfo;
import club.jmint.crossing.client.config.ClientConfig;
import club.jmint.crossing.client.utils.CrossLog;
import club.jmint.crossing.client.utils.Utils;
import club.jmint.crossing.specs.protobuf.CrossingReqProto;
import club.jmint.crossing.specs.protobuf.CrossingRespProto;
import club.jmint.crossing.specs.CrossException;
import club.jmint.crossing.specs.ErrorCode;
import club.jmint.crossing.specs.ParamBuilder;

/**
 * @author shc
 *
 */
public class CrossingCall implements ICall {
	private SocketChannel sockch = null;
	private ClientConfig config = null;
	ByteBuffer byteBuf = ByteBuffer.allocate(8192);
	private String currentInf;
	private ClientCallInfo cci;
	
	public CrossingCall(SocketChannel sc, ClientConfig config){
		this.sockch = sc;
		this.config = config;
		init();
	}
	
	private synchronized void init(){
	}
	
	private String getCciByInf(String inf) {
		this.currentInf = inf;
		String[] el = currentInf.split("@");//inf format: xxx@yyy@zzz
		this.cci = config.getClientCallInfo(el[1]);
		if (cci==null){
			cci = config.getClientCallInfo("DEFAULT");
		}
		return el[1];
	}
	
	private String getSignedParams(String p) throws CrossException{
		String signed=ParamBuilder.buildSignedParams(p,cci.signKey);
		
		CrossLog.logger.debug("Signed Params: " + signed);
		
		return signed;
	}
	
	private String getEncryptedParams(String signedParams) throws CrossException{
		String encrypted=ParamBuilder.buildEncryptedParams(signedParams,cci.encryptKey);
		
		CrossLog.logger.debug("Encrypted Params: " + encrypted);

		return encrypted;
	}
	
	private String checkSignAndRemove(String p) throws CrossException{
		CrossLog.logger.debug("Response signed params: " + p);
		String rp = ParamBuilder.checkSignAndRemove(p,cci.signKey);
		return rp;
	}
	
	private String getDecryptedParams(String encryptParams) throws CrossException{
		String dep = ParamBuilder.buildDecryptedParams(encryptParams,cci.decryptKey);
		return dep;
	}
	
	public String doSyncCall(String inf, String params) throws CrossException{
		//verify inf format and params format
		ParamBuilder.checkCallInfFormat(inf);
		ParamBuilder.checkCallParamFormat(params);
		
		getCciByInf(inf);
		
		String signedp = getSignedParams(params);
		CrossLog.logger.info("Invoked call: " + inf);
		String response = syncCall(inf, signedp, false);
		
		//
		String nosign = checkSignAndRemove(response);
		return nosign;
	}
	
	public String doSyncCall(String inf, String params, boolean isEncrypt) throws CrossException{
		//verify inf format and params format
		ParamBuilder.checkCallInfFormat(inf);
		ParamBuilder.checkCallParamFormat(params);
		
		if (!isEncrypt){
			return doSyncCall(inf, params);
		}
		
		getCciByInf(inf);
		
		String signedp = getSignedParams(params);
		String encryptedp = getEncryptedParams(signedp);
		CrossLog.logger.info("Invoked call(*): " + inf);
		String response = syncCall(inf, encryptedp, isEncrypt);
		
		//
		String decryptedres = getDecryptedParams(response);
		String nosignret = checkSignAndRemove(decryptedres);
		
		return nosignret;
	}
		

	/* (non-Javadoc)
	 * @see club.jmint.crossing.call.ICall#syncCall(java.lang.String, java.lang.String, boolean)
	 */
	public synchronized String syncCall(String inf, String params, boolean isEncrypt) throws CrossException{
		CrossingReqProto.CrossingReq.Builder builder = CrossingReqProto.CrossingReq.newBuilder();
		CrossingRespProto.CrossingResp resp = null;
		
		try{
			InetSocketAddress isa = (InetSocketAddress) sockch.getLocalAddress();
			String ip = isa.getAddress().getHostAddress();

			builder.setSeqId(Utils.getSeqId(ip));
			builder.setInterfaceName(inf);
			builder.setParams(params);
			builder.setIsEncrypt(isEncrypt);

			// sending a call
			CrossingReqProto.CrossingReq callmsg = builder.build();
			CrossLog.logger.debug("Sending a request:\n" + callmsg);
			long stime = Utils.getTimeInMillis();

			int bodyLen = callmsg.toByteArray().length;
			int headerLen = CodedOutputStream.computeRawVarint32Size(bodyLen);
			CodedOutputStream headerOut = CodedOutputStream.newInstance(sockch.socket().getOutputStream(), headerLen);
			headerOut.writeRawVarint32(bodyLen);
			callmsg.writeTo(headerOut);
			headerOut.flush();

			// read and wait the response for server side
			int s = sockch.read(byteBuf);
			byte[] arr = byteBuf.array();

			final byte[] buf = new byte[5];
			for (int i = 0; i < buf.length; i++) {
				buf[i] = arr[i];
				if (buf[i] >= 0) {
					int length = CodedInputStream.newInstance(buf, 0, i + 1).readRawVarint32();

					if (length < 0) {
						throw new CrossException(ErrorCode.CROSSING_ERR_CORRUPTED_FRAME.getCode(),
								ErrorCode.CROSSING_ERR_CORRUPTED_FRAME.getInfo());
					} else {
						byte[] realarr = Utils.copyBytes(arr, i + 1, length);
						CodedInputStream cisx = CodedInputStream.newInstance(realarr, 0, length);
						resp = CrossingRespProto.CrossingResp.parseFrom(cisx);
						// System.out.println(resp);
						byteBuf.clear();
						break;
					}
				}
			}

			long etime = Utils.getTimeInMillis();
			long delay = etime - stime;
			CrossLog.logger.debug(String.format("Received a response(in %dms):\n", delay) + resp);
			
		}catch(IOException e){
			throw new CrossException(ErrorCode.CROSSING_ERR_IO.getCode(),
					ErrorCode.CROSSING_ERR_IO.getInfo());
		}
		
		return resp.getParams();
	}

}
