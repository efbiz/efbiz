package org.efbiz.rest;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Priorities;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.ofbiz.base.util.Debug;
import org.efbiz.rest.util.ActiveMqUtil;

@Path("/hello")
@Priority(Priorities.AUTHENTICATION) 
public class HelloResource{
	
	public static final String module = HelloResource.class.getName();
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String sayHello() {
		Map<String,String> map = new HashMap<String,String>();
		try {
			ActiveMqUtil.sendMessage(map);
		} catch (Exception e) {
			Debug.logWarning(e, module);
		}
		return "success";
	}

}