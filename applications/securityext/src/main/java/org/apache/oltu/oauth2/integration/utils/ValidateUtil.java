/**
 * Project Name:efbiz
 * File Name:ValidateUtil.java
 * Package Name:org.apache.oltu.oauth2.integration.utils
 * Date:2016年7月16日下午5:21:27
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package org.apache.oltu.oauth2.integration.utils;

import javax.inject.Singleton;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.GenericDispatcherFactory;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;

/**
 * ClassName:ValidateUtil <br/>
 * Function: 校验工具类<br/>
 * Date:     2016年7月16日 下午5:21:27 <br/>
 * @author   thanos_t@163.com
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class ValidateUtil {
	
	public static final String module = ValidateUtil.class.getName();
	
	@Singleton
	private static final Delegator delegator = DelegatorFactory.getDelegator("default");
	
	@Singleton
	private static final LocalDispatcher dispatcher = new GenericDispatcherFactory().createLocalDispatcher("default", delegator);

	
	/**
	 * 验证ClientID 是否正确
	 * 
	 * @param oauthRequest
	 * @return
	 */
	public static boolean validateOAuth2AppKeyAndAppSecret(OAuthTokenRequest oauthRequest) {
		GenericValue app = null;
		try {
			app = delegator.findOne("App", UtilMisc.toMap("appKey", oauthRequest.getClientId()), false);
		} catch (GenericEntityException e) {
			Debug.logWarning(e, module);
		}
		if (UtilValidate.isNotEmpty(app) && app.getString("appSecret").equals(oauthRequest.getClientSecret())) {
			return true;
		}
		return false;
	}

	/**
	 * 验证AppKeyAndAppSecret 是否正确
	 * 
	 * @param oauthRequest
	 * @return
	 */
	public static boolean validateOAuth2AppKeyAndAppSecret(String appKey, String appSecret) {
		GenericValue app = null;
		try {
			app = delegator.findOne("App", UtilMisc.toMap("appKey", appKey), false);
		} catch (GenericEntityException e) {
			Debug.logWarning(e, module);
		}
		if (UtilValidate.isNotEmpty(app) && app.getString("appSecret").equals(appSecret)) {
			return true;
		}

		return false;
	}
}

