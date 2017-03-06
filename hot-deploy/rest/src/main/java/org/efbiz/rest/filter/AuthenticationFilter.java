/**
 * Project Name:efbiz
 * File Name:AuthenticationFilter.java
 * Package Name:org.efbiz.rest.filter
 * Date:2016年7月14日下午2:00:09
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package org.efbiz.rest.filter;

import java.io.IOException;

import javax.inject.Singleton;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.efbiz.rest.service.AuthenticationService;
import org.efbiz.rest.util.SignatureHelper;

/**
 * ClassName:AuthenticationFilter <br/>
 * Date:     2016年7月14日 下午2:00:09 <br/>
 * @author   Administrator
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class AuthenticationFilter implements javax.servlet.Filter {
	public static final String AUTHENTICATION_HEADER = "Authorization";
	
	public static final String module = AuthenticationFilter.class.getName();
	
	@Singleton
	private AuthenticationService authenticationService = new AuthenticationService();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filter) throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			
			OAuthAccessResourceRequest oauthRequest = null;
			try {
				oauthRequest = new OAuthAccessResourceRequest((HttpServletRequest)request);
			} catch (OAuthSystemException e) {
				if (response instanceof HttpServletResponse) {
					HttpServletResponse httpServletResponse = (HttpServletResponse) response;
					httpServletResponse
							.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				}
			} catch (OAuthProblemException e) {
				if (response instanceof HttpServletResponse) {
					HttpServletResponse httpServletResponse = (HttpServletResponse) response;
					httpServletResponse
							.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				}
			}
			boolean authenticationStatus = false;
			//使用Auth2.0认证
			if(UtilValidate.isNotEmpty(oauthRequest)){
				String accessToken = null;
	            //获取验证accesstoken
	            try {
					 accessToken = oauthRequest.getAccessToken();
					 authenticationStatus = authenticationService
								.authenticate(accessToken);
				} catch (OAuthSystemException e) {
					Debug.logWarning(e, module);
					if (response instanceof HttpServletResponse) {
						HttpServletResponse httpServletResponse = (HttpServletResponse) response;
						httpServletResponse
								.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					}
				}
			}else{
				//使用 apiKey和 api私钥签名认证
				String url = SignatureHelper.createSortedParams((HttpServletRequest)request);
				Debug.logInfo("请求验签参数：" + url, module);
				String signature = ((HttpServletRequest) request).getHeader(SignatureHelper.SIGNATURE_HEADER);
				String apiKey = ((HttpServletRequest) request).getHeader(SignatureHelper.APPID_HEADER);
				String signType = ((HttpServletRequest) request).getHeader(SignatureHelper.SIGNTYPE_HEADER);
				try {
					authenticationStatus = SignatureHelper.validateSignature(url, signature, apiKey,signType);
				} catch (Exception e) {
					HttpServletResponse httpServletResponse = (HttpServletResponse) response;
					httpServletResponse
							.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				}
				
			}
			 
			if (authenticationStatus) {
				filter.doFilter(request, response);
			} else {
				if (response instanceof HttpServletResponse) {
					HttpServletResponse httpServletResponse = (HttpServletResponse) response;
					httpServletResponse
							.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				}
			}
		}
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}
}
