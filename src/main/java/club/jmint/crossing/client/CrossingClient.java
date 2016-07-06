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
package club.jmint.crossing.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import club.jmint.crossing.client.call.CrossingCall;
import club.jmint.crossing.client.config.ClientConfig;
import club.jmint.crossing.client.utils.CrossLog;
import club.jmint.crossing.specs.CrossException;
import club.jmint.crossing.specs.ErrorCode;
import club.jmint.crossing.specs.ParamBuilder;

/**
 * @author shc
 *
 */
public class CrossingClient{
	private ClientConfig config = null;
	private CrossingCall ccall = null;
	private final static CrossingClient cc = new CrossingClient();
	private String ip;
	private int port = 9000;
	private SocketChannel sc = null;
	private Object reconWorker = null;
	
	public static CrossingClient getInstance(){
		return cc;
	}
	
	private CrossingClient(){
		init();
	}

	private void init() {
		//load configuration from file: crossing_client.xml
		config = new ClientConfig("conf/crossing_client.xml");
		ip = config.getCrossingServer().ip;
		port = Integer.parseInt(config.getCrossingServer().port);
	}
	
	public void startup() throws CrossException{
		//make connection to crossing server
		try{
			sc = SocketChannel.open();
			sc.connect(new InetSocketAddress(ip, port));
		}catch(IOException e){
			CrossLog.logger.error("failed to make connection to crossing server.");
			CrossLog.printStackTrace(e);
		}
        
        ccall = new CrossingCall(sc, config);
        
        CrossLog.logger.info("Crossing Client started.");
        
        //start reconnect worker
        reconWorker = new ReconnectWorker();
        ((ReconnectWorker)reconWorker).start();
	}

	public void shutdown() {
		if (reconWorker!=null){
			((ReconnectWorker)reconWorker).interrupt();;
		}
		
		try {
			sc.close();
		} catch (IOException e) {
			CrossLog.logger.error("failed to close socket channel.");
			CrossLog.printStackTrace(e);
		}
		
		CrossLog.logger.info("Crossing Client stopped.");
	}
	
	public String call(String inf, String params) throws CrossException{
		return ccall.doSyncCall(inf, params);
	}
	
	public String call(String inf, String params, boolean isEncrypt) throws CrossException{
		return ccall.doSyncCall(inf, params, isEncrypt);
	}
	
	public CallResult serviceCall(String inf, JsonObject params, boolean isEncrypt){
    	String result = null;
    	CallResult cr = new CallResult();
    	try{
    		if (isEncrypt){
    			result = call(inf, params.toString(), true);
    		}else{
    			result = call(inf, params.toString());
    		}
    	}catch(CrossException e){
    		CrossLog.printStackTrace(e);
    		CrossLog.logger.error("service call failed.");
    		return null;
    	}    	
    	
		JsonParser jp = new JsonParser();
		JsonObject jo;
		try{
			jo = (JsonObject)jp.parse(result);
		}catch(JsonSyntaxException e){
    		CrossLog.printStackTrace(e);
    		CrossLog.logger.error("service call failed.");
    		return null;
		}
		
		cr.errorCode = jo.getAsJsonPrimitive("errorCode").getAsInt();
		if (cr.errorCode==ErrorCode.SUCCESS.getCode()){
			cr.isSuccess = true;
		} else {
			cr.isSuccess = false;
		}
		cr.errorInfo = jo.getAsJsonPrimitive("errorInfo").getAsString();
		if (jo.has("params")){
			cr.data = jo.getAsJsonObject("params");
		}
		
		return cr;
	}
	
	public class ReconnectWorker extends Thread{
		private long gap = 3000;
		
		@Override
		public void run() {
			super.run();
			while(true){
				try {
					sleep(gap);
				} catch (InterruptedException e) {
					CrossLog.logger.info("ReconnectWorker terminated.");
					break;
				}
				
				if (sc!=null && !sc.isConnected()){
					try {
						sc.connect(new InetSocketAddress(ip, port));
					} catch (IOException e) {
						CrossLog.printStackTrace(e);
						CrossLog.logger.error("Reconnect to crossing server failed.");
					}
					CrossLog.logger.info("Reconnect to crossing server succeeded.");
				}
				
				if (sc!=null && !sc.isOpen()){
					try {
						sc = SocketChannel.open();
						sc.connect(new InetSocketAddress(ip, port));
					} catch (IOException e) {
						CrossLog.printStackTrace(e);
						CrossLog.logger.error("Reopen channel to crossing server failed.");
					}
					CrossLog.logger.info("Reopen channel to crossing server succeeded.");
				}
			}
		}
		
	}
	
	
	
}
