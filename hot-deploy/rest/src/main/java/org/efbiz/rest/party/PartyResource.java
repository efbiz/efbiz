package org.efbiz.rest.party;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.Dispatch;

import org.apache.ofbiz.base.lang.JSON;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.GenericDispatcherFactory;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.LocalDispatcherFactory;
import org.apache.ofbiz.service.ServiceDispatcher;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.ofbiz.webapp.stats.VisitHandler;
import org.efbiz.rest.util.RestResponseUtil;

@Path("/party")
public class PartyResource{
	public static String module = PartyResource.class.getName();
	
	@GET    
    @Path("/getPerson/{param}")      
    @Produces("text/plain;charset=UTF-8")    
    public Response getPerson(@PathParam("param") String partyId) throws Exception {
		Delegator delegator = DelegatorFactory.getDelegator("default");
		GenericValue person = delegator.findOne("PartyAndUserLoginAndPerson", false, UtilMisc.toMap("partyId", partyId));
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("person", person);
		JSON json = JSON.from(result);
		return Response
				 .ok(json.toString(), MediaType.APPLICATION_JSON_TYPE)
				 .status(RestResponseUtil.HTTP_CODE_417)
				 .encoding("UTF-8")
				 .build(); 
    }  
	
	@SuppressWarnings("unchecked")
	@POST  
    @Path("/login")  
    @Consumes({MediaType.APPLICATION_JSON})
    public Response login(String data) { 
		Delegator delegator = DelegatorFactory.getDelegator("default");
		LocalDispatcher dispatcher = ServiceDispatcher.getLocalDispatcher("default", delegator);
		JSON json = null;
		try {
   		 json = JSON.from(data);
		} catch (Exception e) {
			Debug.logWarning(e, "获取登录参数失败", module);
			return RestResponseUtil.getResponse(RestResponseUtil.HTTP_CODE_417);
		}
		
		HashMap<String, String> userLogin = null;
		try {
			userLogin = (HashMap<String,String>) json.toObject(HashMap.class);
		} catch (IOException e) {
			Debug.logWarning(e, "获取登录参数失败", module);
			return RestResponseUtil.getResponse(RestResponseUtil.HTTP_CODE_417);
		}
        Map<String, Object> result = null;
        try {
            // get the visit id to pass to the userLogin for history
            result = dispatcher.runSync("userLogin", UtilMisc.toMap("login.username",userLogin.get("username") , "login.password", userLogin.get("password")));
        } catch (GenericServiceException e) {
            Debug.logError(e, "Error calling userLogin service", module);
            return RestResponseUtil.getResponse(RestResponseUtil.HTTP_CODE_417);
        }
        
        //登录成功后返回用户信息
        if(ServiceUtil.isSuccess(result)){
        	/*
        	GenericValue person;
			try {
				person = delegator.findOne("PartyAndUserLoginAndPerson", false, UtilMisc.toMap("partyId", userLogin.get("username")));
				result.put("person", person);
			} catch (GenericEntityException e) {
				Debug.logError(e, "Error calling findOne [PartyAndUserLoginAndPerson]", module);
				return RestResponseUtil.getResponse(RestResponseUtil.HTTP_CODE_417);
			}
			 */
        	result = ServiceUtil.returnSuccess();
        }
       
        JSON resultJson = null;
		try {
			resultJson = JSON.from(result);
		} catch (IOException e) {
			Debug.logError(e, "Error calling JSON.from ["+result+"]", module);
			return RestResponseUtil.getResponse(RestResponseUtil.HTTP_CODE_417);
		}
		return Response
				 .ok(resultJson.toString(), MediaType.APPLICATION_JSON_TYPE)
				 .status(RestResponseUtil.HTTP_CODE_200)
				 .encoding("UTF-8")
				 .build(); 
	}  
	
	@GET    
    @Path("/createPerson")
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces("text/plain;charset=UTF-8")    
    public String createPerson(@Context UriInfo ui) throws Exception {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		Delegator delegator = DelegatorFactory.getDelegator("default");
		//GenericValue person = delegator.findOne("Person", false, UtilMisc.toMap("partyId", partyId));
		Map<String, Object> result = ServiceUtil.returnSuccess();
		result.put("mapPerson", queryParams);
		JSON json = JSON.from(result);
		return json.toString();    
    }
}