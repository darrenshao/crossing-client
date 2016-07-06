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
package club.jmint.crossing.client.utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author shc
 *
 */
public class Utils {
	private static final Random r = new Random();
	private static int seq = r.nextInt();

	public static String getIpStrFromSeqId(long seqId){
		String hex = Long.toHexString(seqId);
		//Be careful, the leading Hex 0s removed automatically by the method Long.toHexString,
		//And changed the string total length of hex.
		String ipHexStr = hex.substring(0,hex.length()-8);
		long ip = Long.parseLong(ipHexStr, 16);
		return int2IpStr(ip);
	}
	
	/**
	 * create a unique sequence id based on machine characteristics,
	 * 
	 * @return string
	 */
	public static long getSeqId(String ip) {
		long seqId = 0x0000000000000000;
		long intip = ipStr2int(ip);
		long lip = 0x0000000000000000 | intip;
		lip = lip << 32;
		seqId = seqId | lip;
		seqId = seqId | (++seq);
		//System.out.println(Long.toHexString(seqId));
		return seqId++;
	}
	
	public static long ipStr2int(String ip) {
		long intip = 0x0000000000000000;
		String[] ints = ip.split("\\.");
		intip = intip | ((Long.parseLong(ints[0]) << 24) & 0x00000000ff000000);
		intip = intip | ((Long.parseLong(ints[1]) << 16) & 0x0000000000ff0000);
		intip = intip | ((Long.parseLong(ints[2]) << 8) & 0x000000000000ff00);
		intip = intip | (Long.parseLong(ints[3]) & 0x00000000000000ff);
		//System.out.println(int2IpStr(intip));
		return intip;
	}
	
	public static String int2IpStr(long ip){
		String ipStr = String.format("%d.%d.%d.%d", 
				(ip & 0x00000000ff000000) >> 24, 
				(ip & 0x0000000000ff0000) >> 16, 
				(ip & 0x000000000000ff00) >> 8, 
				ip &0x00000000000000ff);
		return ipStr;
	}
	
	   /**
	    * Validate ip address with regular expression
	    * @param ip ip address for validation
	    * @return true valid ip address, false invalid ip address
	    */
	public static boolean isValidIpFormat(String ip){
		Pattern pattern;
		Matcher matcher;
		
		if (ip==null) return false;
		final String IPADDRESS_PATTERN = 
				"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

		pattern = Pattern.compile(IPADDRESS_PATTERN);
		matcher = pattern.matcher(ip);
		return matcher.matches();
	}
	
	public static String getReadableIPString(byte[] bytes){
		if (bytes==null) return null;
		if (bytes.length != 4) return null;
		String ip = String.format("%d.%d.%d.%d", 
				(short)(bytes[0] & 0x00ff), (short)(bytes[1] & 0x00ff), 
				(short)(bytes[2] & 0x00ff), (short)(bytes[3] & 0x00ff));
		return ip;
	}
	
	
	/**
	 * format parameters with standard URL: key1=value1&key2=value2
	 */
	public static String getURLParamStr(HashMap<String,String> map,boolean isEncrypt){
		StringBuffer url = new StringBuffer();
		Iterator<Entry<String,String>> it = map.entrySet().iterator();
		Entry<String,String> en;
		while(it.hasNext()){
			en = it.next();
			url.append(en.getKey()+"="+en.getValue());
			if (it.hasNext()){
				url.append("&");
			}
		}
		return url.toString();
	}
	
	public static long getTimeInMillis(){
		return Calendar.getInstance().getTimeInMillis();
	}
	
	public static String getDateTime(){
		return Calendar.getInstance().getTime().toString();
	}
	
	/**
	 * Copy given length of bytes from a byte array to another byte array
	 * @param from
	 * @param to
	 * @param len
	 * @return len
	 */
	public static byte[] copyBytes(byte[] from, int offset, int len){
		byte[] to = new byte[len];
		
		for (int i=0; i<len; i++){
			to[i] = from[offset+i];
		}
		return to;
	}
	
	/**
	 * Print bytes in Hex format.
	 * @param bytes
	 */
	public static void printHex(byte[] bytes){
		if (bytes==null) return;
		int lines = bytes.length / 16;
		if (bytes.length % 16 > 0){
			lines++;
		}
		String readable = new String(bytes);
		int maxlen = readable.length();
		
		for(int i=1;i<=lines;i++){
			
			for(int j=0;j<16;j++){
				int dot1 = j + (i-1)*16;
				if (dot1 < maxlen){
					System.out.print(String.format("%2X ", bytes[dot1]));
				}
			}
			System.out.print("    ");
			
			for(int k=0;k<16;k++){
				int dot2 = k + (i-1)*16;
				if (dot2 < maxlen){
					System.out.print(String.format("%2c ", readable.charAt(dot2)));
				}
			}
			
			System.out.println();
		}
	}
}
