/**
 * Project Name:efbiz
 * File Name:EntityResoure.java
 * Package Name:org.efbiz.rest.entity
 * Date:2016年6月28日下午7:37:49
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package org.efbiz.rest.entity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericDelegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceDispatcher;
import org.efbiz.rest.util.RestResponseUtil;
import org.json.JSONObject;

/**
 * ClassName:EntityResoure <br/>
 * Function: 查询JSON格式实体数据. <br/>
 * Date:     2016年6月28日 下午7:37:49 <br/>
 * @author   thanos
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
@Path("/entity")
public class EntityResoure {
	public static String module = EntityResoure.class.getName();
	
	public static GenericDelegator delegator = (GenericDelegator) DelegatorFactory.getDelegator("default");
	public static LocalDispatcher dispatcher = ServiceDispatcher.getInstance(delegator).getLocalDispatcher("default");
	@Context
	public HttpServletRequest request;
	
	@Context
	public HttpServletResponse response;
	
	@GET
	@Path("/get/{entityName}/{key}/{keyValue}")  
	@Produces(MediaType.TEXT_PLAIN)
	public Response get(@PathParam("entityName") String entityName,
						@PathParam("key") String key,
						@PathParam("keyValue") String keyValue) {
    	JSONObject resultJson =  new JSONObject();
    	if(UtilValidate.isNotEmpty(entityName)){
    		resultJson.put("result","success");
    		resultJson.put("entityName", entityName);
    	}else{
    		resultJson.put("result","error");
    		resultJson.put("errorMsg", "实体名称不存在");
    		return	Response
						 .ok(resultJson.toString(), MediaType.APPLICATION_JSON_TYPE)
						 .status(RestResponseUtil.HTTP_CODE_417)
						 .build(); 
    	}
    	GenericValue entity = null;
    	try {
    		entity = delegator.findOne(entityName, UtilMisc.toMap(key, keyValue), true);
		} catch (GenericEntityException e) {
			Debug.logError(e, "获取用户信息失败", module);
			return RestResponseUtil.getResponse(RestResponseUtil.HTTP_CODE_500); 
		}
    	if(UtilValidate.isNotEmpty(entity)){
    		resultJson.put("entityDatas", entity);
    	}
		return Response
				 .ok(resultJson.toString(), MediaType.APPLICATION_JSON_TYPE)
				 .status(RestResponseUtil.HTTP_CODE_200)
				 .build(); 
	}
}

