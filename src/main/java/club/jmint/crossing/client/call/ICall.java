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

import club.jmint.crossing.specs.CrossException;

/**
 * @author shc
 * standardize the way of client call 
 */
public interface ICall {
	/**
	 * 
	 * @param inf	Service interface within the class
	 * @param params Input parameters with JSON format, <br/>
	 * 				Example: <br/>
	 * 					{"key1":"value1","key2":"value2"} <br/>
	 * @param isEncrypt	Encrypt the parameters in JSON format <br/>
	 * 				Example(after sign or encryption): <br/>
	 * 					signValue = Sign( {"key1":"value1","key2":"value2"}signKey ); <br/>
	 * 					{"sign":"signValue","params":{"key1":"value1","key2":"value2"}} <br/>
	 * 					encrypted value = Encrypt( {"params":{"key1":"value1","key2":"value2"},"sign":"signValue"} ) <br/>
	 * 					{"encrypted":"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"} <br/>
	 * 
	 * @return String JSON string with signature or encryption information <br/>
	 * 			Example: <br/>
	 * 				Encrypted: {"encrypted":"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx","errorCode":"0","erroDesc":"success"} <br/>
	 * 				Non-encrypted: {"sign":"signValue","errorCode":"0","erroDesc":"success","params":{"key1":"value1","key2":"value2"}} <br/>
	 * @throws CrossException 
	 */
	public String syncCall(String inf, String params, boolean isEncrypt) throws CrossException;
}
