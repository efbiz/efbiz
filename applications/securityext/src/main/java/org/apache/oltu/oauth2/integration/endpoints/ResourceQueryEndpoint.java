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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.apache.oltu.oauth2.integration.Common;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;

 


/**
 *
 *
 *
 */
@Path("/resource_query")
public class ResourceQueryEndpoint {
    
    //登录页面
    private static final String loginPage = "http://www.baidu.com/";
    
    @GET
    @Produces("text/html")
    public Response get(@Context HttpServletRequest request) throws OAuthSystemException, ParseException, URISyntaxException {

        try {

        	//构建OAuth资源请求
            OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request,
                ParameterStyle.QUERY);

            //获取Access Token
            String accessToken = oauthRequest.getAccessToken();
//            RefreshToken rt = refreshTokenDao.validAccessToke(accessToken);
           
            //验证Access Token
//            if (rt==null) {
//            	// 如果不存在/过期了，跳转到登录页面
//				return Response.temporaryRedirect(new URI(loginPage)).build();
////                OAuthResponse oauthResponse = OAuthRSResponse
////                    .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
////                    .setRealm(Common.RESOURCE_SERVER_NAME)
////                    .setError(OAuthError.ResourceResponse.INVALID_TOKEN)
////                    .buildHeaderMessage();
////
////                return Response.status(Response.Status.UNAUTHORIZED)
////                    .header(OAuth.HeaderType.WWW_AUTHENTICATE,
////                        oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
////                    .build();
//            }else{
//                //校验令牌是否过期
//            	String createTime = rt.getCreate_time();
//            	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            	Date date = sdf.parse(createTime);
//            	long expire = Long.valueOf(rt.getExpire())*1000;
//            	Date now = new Date(date.getTime()+expire);
//            	boolean isExpire = System.currentTimeMillis()>now.getTime();
//            	if(isExpire){
//            		return Response.temporaryRedirect(new URI(loginPage)).build();
//            	}
//            }

            // 返回资源 这里作为父资源，如果校验通过则返回子资源
            return Response.status(Response.Status.OK).entity(accessToken).build();

        } catch (OAuthProblemException e) {
        	//检查是否设置了错误码
            String errorCode = e.getError();
            if (OAuthUtils.isEmpty(errorCode)) {

                OAuthResponse oauthResponse = OAuthRSResponse
                    .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                    .setRealm(Common.RESOURCE_SERVER_NAME)
                    .buildHeaderMessage();

                return Response.status(Response.Status.UNAUTHORIZED)
                    .header(OAuth.HeaderType.WWW_AUTHENTICATE,
                        oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
                    .build();
            }

            OAuthResponse oauthResponse = OAuthRSResponse
                .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setRealm(Common.RESOURCE_SERVER_NAME)
                .setError(e.getError())
                .setErrorDescription(e.getDescription())
                .setErrorUri(e.getUri())
                .buildHeaderMessage();

            return Response.status(Response.Status.BAD_REQUEST)
                .header(OAuth.HeaderType.WWW_AUTHENTICATE,
                    oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE))
                .build();
        }
    }
}
