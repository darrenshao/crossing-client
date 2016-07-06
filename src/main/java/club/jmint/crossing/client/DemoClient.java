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

import java.util.HashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class DemoClient {
    public static void main(String[] args){
    	//Create Crossing client call proxy
    	CrossingClient cc = CrossingClient.getInstance();
    
    	//Start Crossing Client
    	try{
    		cc.startup();
    	}catch(Exception e){
    		System.exit(-1);
    	}
    	System.out.println("Starting to run example calls.");
    	
    	
		JsonObject pp4 = new JsonObject();
		pp4.addProperty("type", 4);
		pp4.addProperty("fields", "user_id,user_true_name,status,pay_status,product_id,product_name,ticket_code,ticket_code_url");
		pp4.addProperty("user_id", "1000000005");
    	System.out.println(pp4.toString());

    	CallResult result = cc.serviceCall("TwohalfServer@com.twohalf.mifty.service.gen.OrderService@orderQueryByUser",
	    			pp4, false);
    	if (result!=null && result.isSuccess){
    		System.out.println(result.data);
    	}
    	
    	
    	
    	//Stop Crossing Client
    	cc.shutdown();
    }

}
