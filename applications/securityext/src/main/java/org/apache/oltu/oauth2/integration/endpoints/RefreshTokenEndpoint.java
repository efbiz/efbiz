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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.cache.UtilCache;
import org.apache.ofbiz.base.util.cache.UtilCacheInf;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.service.GenericDispatcherFactory;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.integration.utils.ValidateUtil;

/**
 * get refresh token
 */
@Path("/refresh_token")
public class RefreshTokenEndpoint {

	public static final String module = RefreshTokenEndpoint.class.getName();
	
	public static final String oauthProperties = "oauth.properties";
	@Singleton
	Delegator delegator = DelegatorFactory.getDelegator("default");

	@Singleton
	private LocalDispatcher dispatcher = new GenericDispatcherFactory().createLocalDispatcher("default", delegator);

	// 错误页面
	private static String errorPage = UtilProperties.getPropertyValue(oauthProperties, "oauth.errorPage", "https://localhost:8443/partymgr/control/checkLogin");

	public static final String AUTHENTICATION_HEADER = "Authorization";
	public static final String INVALID_CLIENT_DESCRIPTION = "客户端认证失败";
	public static final String INVALID_CLIENT_GRANT = "客户端未授权";
	private static final UtilCacheInf<String, String> tokensCache = (UtilCacheInf<String, String>) UtilCache.createUtilCache("tokensCache");

	/**
	 * 刷新令牌
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws OAuthSystemException
	 * @url http://localhost:8080/oauth2/refresh_token?client_id={AppKey}&grant_type=refresh_token&refresh_token={refresh_token}
	 */
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces("application/json")
	public Response accessToken(@Context HttpServletRequest request)
			throws OAuthSystemException, URISyntaxException {
		OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
		try {
			// 构建oauth2请求
			OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);

			// 验证appkey是否正确
			if (!ValidateUtil.validateOAuth2AppKeyAndAppSecret(oauthRequest)) {
				return Response.temporaryRedirect(new URI(errorPage + "?error=" + OAuthError.CodeResponse.UNAUTHORIZED_CLIENT)).build();
			}
			// 验证是否是refresh_token
			if (!GrantType.REFRESH_TOKEN.name().equalsIgnoreCase(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE))) {
				return Response.temporaryRedirect(new URI(errorPage + "?error=" + RefreshTokenEndpoint.INVALID_CLIENT_GRANT)).build();
			}
			// 刷新access_token有效期
			// access_token是调用授权关系接口的调用凭证，由于access_token有效期（目前为2个小时）较短，当access_token超时后，可以使用refresh_token进行刷新，access_token刷新结果有两种：
			// 1. 若access_token已超时，那么进行refresh_token会获取一个新的access_token，新的超时时间；
			// 2. 若access_token未超时，那么进行refresh_token不会改变access_token，但超时时间会刷新，相当于续期access_token。
			// refresh_token拥有较长的有效期（30天），当refresh_token失效的后，需要用户重新授权。

			Object cache_refreshToken = tokensCache.get(oauthRequest.getRefreshToken());
			// access_token已超时
			if (cache_refreshToken == null) {
				// 生成token
				final String access_Token = oauthIssuerImpl.accessToken();
				String refresh_Token = oauthIssuerImpl.refreshToken();
				tokensCache.put(refresh_Token, access_Token, 3600);
				Debug.logInfo("access_Token : " + access_Token + "  refresh_Token: " + refresh_Token, module);
				// 构建oauth2授权返回信息
				OAuthResponse oauthResponse = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK).setAccessToken(access_Token).setExpiresIn("3600").setRefreshToken(refresh_Token).buildJSONMessage();
				URI url = new URI(oauthRequest.getRedirectURI());
				return Response.status(oauthResponse.getResponseStatus()).location(url).build();
			}
			// access_token未超时
			tokensCache.put(oauthRequest.getRefreshToken(), cache_refreshToken.toString());
			// 构建oauth2授权返回信息
			OAuthResponse oauthResponse = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK).setAccessToken(cache_refreshToken.toString()).setExpiresIn("3600").setRefreshToken(oauthRequest.getRefreshToken()).buildJSONMessage();
			URI url = new URI(oauthRequest.getRedirectURI());
			return Response.status(oauthResponse.getResponseStatus()).location(url).build();
		} catch (OAuthProblemException ex) {
			OAuthResponse oauthResponse = OAuthResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED).error(ex).buildJSONMessage();
			URI url = new URI(errorPage);
			return Response.status(oauthResponse.getResponseStatus()).location(url).build();
		}
	}

	
}