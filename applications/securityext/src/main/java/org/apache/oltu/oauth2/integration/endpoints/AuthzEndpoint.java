/**
 *       Copyright 2010 Newcastle University
 *
 *          http://research.ncl.ac.uk/smart/
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oltu.oauth2.integration.endpoints;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.base.util.cache.UtilCache;
import org.apache.ofbiz.base.util.cache.UtilCacheInf;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityUtil;
import org.apache.ofbiz.service.GenericDispatcherFactory;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.error.ServerErrorType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ResponseType;

 

/**
 * 
 * client request authorization
 * 
 */
@Path("/authz")
public class AuthzEndpoint {

	public static final String module = AuthzEndpoint.class.getName();
	
	public static final String oauthProperties = "oauth.properties";
	
	@Singleton
	Delegator delegator = DelegatorFactory.getDelegator("default");
	
	@Singleton
	private LocalDispatcher dispatcher = new GenericDispatcherFactory().createLocalDispatcher("default", delegator);

	//登录页面
    private static String loginPage = UtilProperties.getPropertyValue(oauthProperties, "oauth.loginPage","https://localhost:8443/partymgr/control/checkLogin");
    //错误页面
    private static String errorPage = UtilProperties.getPropertyValue(oauthProperties, "oauth.errorPage","https://localhost:8443/partymgr/control/checkLogin");
    
    //认证失败描述
	public static final String INVALID_CLIENT_DESCRIPTION = "客户端认证失败";

	//缓存
	private static final UtilCacheInf<String, String> oauthCache =(UtilCacheInf<String, String>) UtilCache.createUtilCache("oauthCache");
	
	
	/* *
	 * 构建OAuth2授权请求 [需要client_id与redirect_uri绝对地址]
	 * @param request
	 * @param session
	 * @param model
	 * @return 返回授权码(code)有效期10分钟，客户端只能使用一次[与client_id和redirect_uri一一对应关系]
	 * @throws OAuthSystemException
	 * @throws IOException
	 * @url http://localhost:10087/oauth/authz?client_id={AppKey}&response_type=code&redirect_uri={YourSiteUrl}
	 * @test http://localhost:10087/oauth/authz?client_id=fbed1d1b4b1449daa4bc49397cbe2350&response_type=code&redirect_uri=http://localhost:8080/client/oauth_callback
	 */
	@GET
	public Response authorize(@Context HttpServletRequest request)
			throws URISyntaxException, OAuthSystemException {

		OAuthAuthzRequest oauthRequest = null;
		OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(
				new MD5Generator());
		try {
			oauthRequest = new OAuthAuthzRequest(request); 
			/*
			 * 当前登录的用户， 并获得对应用户的userLoginId
			 */
			GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
			if (UtilValidate.isEmpty(userLogin)) {
				// 用户没有登录就跳转到登录页面
				return Response.temporaryRedirect(new URI(loginPage)).build();
			} 
			// 根据response_type创建response
			String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);

			OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse
					.authorizationResponse(request,
							HttpServletResponse.SC_FOUND);
			
			GenericValue app = null;
			if (oauthRequest.getClientId() != null && !"".equals(oauthRequest.getClientId())) {
				List<GenericValue> apps = null;
				try {
					apps = delegator.findByAnd("App", UtilMisc.toMap("appKey", oauthRequest.getClientId()), null, false);
				} catch (GenericEntityException e) {
					Debug.logWarning(e, module);
				}
				apps = EntityUtil.filterByDate(apps);
				if(UtilValidate.isNotEmpty(apps)){
					app = EntityUtil.getFirst(apps);
				}
				if(UtilValidate.isEmpty(app)){
					return Response.temporaryRedirect(new URI(errorPage + "?error=" + ServerErrorType.CLIENT_ID_IS_NULL)).build();
				}
			} else {
				return Response.temporaryRedirect(new URI(errorPage + "?error=" + ServerErrorType.CLIENT_ID_IS_NULL)).build();
			}

			String scope = oauthRequest.getParam(OAuth.OAUTH_SCOPE);
			
			// 授权请求类型
			if (responseType.equals(ResponseType.CODE.toString())) {
				String code = oauthIssuerImpl.authorizationCode();
				builder.setCode(code);
				oauthCache.put(code, scope,216000000);
			}
			
			OAuthResponse response = null;
			// 客户端跳转URI
			String redirectURI = oauthRequest
					.getParam(OAuth.OAUTH_REDIRECT_URI);
			if (!Pattern.compile("^[a-zA-Z]+://(\\w+(-\\w+)*)(\\.(\\w+(-\\w+)*))*(\\?\\s*)?$").matcher(oauthRequest.getRedirectURI()).matches()) {
				response = OAuthASResponse .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                                          .setError(OAuthError.CodeResponse.INVALID_REQUEST)
                                          .setErrorDescription(OAuthError.OAUTH_ERROR_URI)
                                          .buildJSONMessage();
            }else{
            	response = builder.location(redirectURI).setParam("scope", scope)
    					.buildQueryMessage();
            }
			URI url = new URI(response.getLocationUri());
			return Response.status(response.getResponseStatus()).location(url)
					.build();

		} catch (OAuthProblemException e) {
			return Response.temporaryRedirect(new URI(errorPage+"?error="+ServerErrorType.BAD_RQUEST)).build();
		}
	}
	
	private String getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(System.currentTimeMillis());
	}
}
