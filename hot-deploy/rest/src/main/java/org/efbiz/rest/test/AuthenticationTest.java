/**
 * Project Name:efbiz
 * File Name:AuthenticationTest.java
 * Package Name:org.efbiz.rest.test
 * Date:2016年7月29日上午11:04:58
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package org.efbiz.rest.test;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;


/**
 * ClassName:AuthenticationTest <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年7月29日 上午11:04:58 <br/>
 * @author   Administrator
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class AuthenticationTest  { 
	/**
	 * @param args
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		
		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget webTarget = client.target("http://example.com/rest");
		WebTarget resourceWebTarget = webTarget.path("resource");
		WebTarget helloworldWebTarget = resourceWebTarget.path("helloworld");
		WebTarget helloworldWebTargetWithQueryParam =
		        helloworldWebTarget.queryParam("greeting", "Hi World!");
		 
		Invocation.Builder invocationBuilder =
		        helloworldWebTargetWithQueryParam.request(MediaType.TEXT_PLAIN_TYPE);
		invocationBuilder.header("some-header", "true");
		 
//		Response response = invocationBuilder.get();
//		System.out.println(response.getStatus());
//		System.out.println(response.getEntity());
	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri(
				"http://localhost:8080/jersey/").build();
	}
}

