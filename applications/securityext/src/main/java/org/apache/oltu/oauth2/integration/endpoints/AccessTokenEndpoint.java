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
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilHttp;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.cache.UtilCache;
import org.apache.ofbiz.base.util.cache.UtilCacheInf;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.service.GenericDispatcherFactory;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ModelService;
import org.apache.ofbiz.webapp.stats.VisitHandler;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.error.ServerErrorType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.integration.utils.ValidateUtil;

/**
 * 
 * get access token
 * 
 */
@Path("/acess_token")
public class AccessTokenEndpoint {
 
	public static final String module = AccessTokenEndpoint.class.getName();
	public static final String oauthProperties = "oauth.properties";
	@Singleton
	Delegator delegator = DelegatorFactory.getDelegator("default");
	
	@Singleton
	private LocalDispatcher dispatcher = new GenericDispatcherFactory().createLocalDispatcher("default", delegator);

	 //错误页面
    private static String errorPage = UtilProperties.getPropertyValue(oauthProperties, "oauth.errorPage","https://localhost:8443/partymgr/control/checkLogin");
  
    public static final String AUTHENTICATION_HEADER = "Authorization";
	public static final String INVALID_CLIENT_DESCRIPTION = "客户端认证失败";
	public static final String INVALID_CLIENT_GRANT = "客户端未授权";
	private static final UtilCacheInf<String, String> tokensCache = (UtilCacheInf<String, String>) UtilCache.createUtilCache("tokensCache");
	private static final UtilCacheInf<String, String> oauthCache = (UtilCacheInf<String, String>) UtilCache.findCache("oauthCache");
	 
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	public Response accessToken(@Context HttpServletRequest request)
			throws OAuthSystemException, URISyntaxException {

		OAuthTokenRequest oauthRequest = null;
//		String scope = "";
		OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

		try {
			//构建oauth2请求
            oauthRequest = new OAuthTokenRequest(request);
			
            //验证redirecturl格式是否合法 (8080端口测试)
            if (!Pattern.compile("^[a-zA-Z]+://(\\w+(-\\w+)*)(\\.(\\w+(-\\w+)*))*(\\?\\s*)?$").matcher(oauthRequest.getRedirectURI()).matches()) {
            	return Response.temporaryRedirect(new URI(errorPage + "?error=" + ServerErrorType.CLIENT_ID_IS_NULL)).build();
            }
            
            //验证appkey是否正确
            if (!ValidateUtil.validateOAuth2AppKeyAndAppSecret(oauthRequest)){
                return Response.temporaryRedirect(new URI(errorPage + "?error=" + OAuthError.CodeResponse.UNAUTHORIZED_CLIENT)).build();
            }
            
            String authzCode = oauthRequest.getCode();
            //验证AUTHORIZATION_CODE , 其他的还有PASSWORD 或 REFRESH_TOKEN (考虑到更新令牌的问题，在做修改)
            if (GrantType.AUTHORIZATION_CODE.name().equalsIgnoreCase(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE))) {
                if (tokensCache.get(authzCode) == null) {
                    return Response.temporaryRedirect(new URI(errorPage + "?error=" + AccessTokenEndpoint.INVALID_CLIENT_GRANT)).build();
                }
            }else if (GrantType.CLIENT_CREDENTIALS.name().equalsIgnoreCase(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE))) {
            	String authCredentials = request.getHeader(AUTHENTICATION_HEADER);
        		// header value format will be "Basic encodedstring" for Basic
        		// authentication. Example "Basic YWRtaW46YWRtaW4="
        		final String encodedUserPassword = authCredentials.replaceFirst("Basic"
        				+ " ", "");
        		String usernameAndPassword =  new String(Base64.decodeBase64(encodedUserPassword));;
        		final StringTokenizer tokenizer = new StringTokenizer(
        				usernameAndPassword, ":");
//        		final StringTokenizer tokenizer = new StringTokenizer(
//        				encodedUserPassword, ":");
        		final String appKey = tokenizer.nextToken();
        		final String appSecret = tokenizer.nextToken();
	    		if (!ValidateUtil.validateOAuth2AppKeyAndAppSecret(appKey,appSecret)){
	                 return Response.temporaryRedirect(new URI(errorPage + "?error=" + OAuthError.CodeResponse.UNAUTHORIZED_CLIENT)).build();
	            }
            }else if (GrantType.PASSWORD.name().equalsIgnoreCase(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE))) {
            	final String username = oauthRequest.getParam(OAuth.OAUTH_USERNAME);
        		final String password =oauthRequest.getParam(OAuth.OAUTH_PASSWORD);
        		 Map<String, Object> result = null;
    	        try {
    	            // get the visit id to pass to the userLogin for history
    	            String visitId = VisitHandler.getVisitId(request.getSession());
    	            result = dispatcher.runSync("userLogin", UtilMisc.toMap("login.username", username, "login.password", password, "visitId", visitId, "locale", UtilHttp.getLocale(request)));
    	        } catch (GenericServiceException e) {
    	            Debug.logError(e, "Error calling userLogin service", module);
    	            return Response.temporaryRedirect(new URI(errorPage + "?error=" + AccessTokenEndpoint.INVALID_CLIENT_GRANT)).build();
    	        }
    	        if (!ModelService.RESPOND_SUCCESS.equals(result.get(ModelService.RESPONSE_MESSAGE))) {
    	        	 return Response.temporaryRedirect(new URI(errorPage + "?error=" + AccessTokenEndpoint.INVALID_CLIENT_GRANT)).build();
    	        }
            }
            //生成token
            final String accessToken = oauthIssuerImpl.accessToken();
            String refreshToken = oauthIssuerImpl.refreshToken();
            //cache.put(accessToken,cache.get(authzCode).get());
            tokensCache.put(refreshToken, accessToken,	216000000);
            Debug.logInfo("accessToken : "+accessToken +"  refreshToken: "+refreshToken,module);
            //清除授权码 确保一个code只能使用一次
            if(oauthCache != null){
            	  oauthCache.remove(authzCode);
            }
          
            //构建oauth2授权返回信息
            OAuthResponse oauthResponse = OAuthASResponse
                                          .tokenResponse(HttpServletResponse.SC_OK)
                                          .setAccessToken(accessToken)
                                          .setExpiresIn("3600")
                                          .setRefreshToken(refreshToken)
                                          .buildJSONMessage();
            URI url = new URI(oauthRequest.getRedirectURI());
			return Response.status(oauthResponse.getResponseStatus()).location(url)
					.build();
        } catch(OAuthProblemException ex) {
            OAuthResponse oauthResponse = OAuthResponse
                                          .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                                          .error(ex)
                                          .buildJSONMessage();
            URI url = null;
			try {
				url = new URI(oauthResponse.getLocationUri());
			} catch (URISyntaxException e) {
				Debug.logInfo(e, module);
			}
            return Response.status(oauthResponse.getResponseStatus()).location(url)
					.build();
        }  
		 
	}
	 
}