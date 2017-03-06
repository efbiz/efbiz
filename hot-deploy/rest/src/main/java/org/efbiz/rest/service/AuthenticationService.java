/**
 * Project Name:efbiz
 * File Name:AuthenticationService.java
 * Package Name:org.efbiz.rest.service
 * Date:2016年7月14日下午2:02:43
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package org.efbiz.rest.service;

import org.apache.ofbiz.base.util.cache.UtilCache;
import org.apache.ofbiz.base.util.cache.UtilCacheInf;

/**
 * ClassName:AuthenticationService <br/>
 * Function: 鉴权服务类. <br/>
 * Reason:	 实现认证. <br/>
 * Date:     2016年7月14日 下午2:02:43 <br/>
 * @author   Administrator
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class AuthenticationService {
	
	public static String module = AuthenticationService.class.getName();
	private static final UtilCacheInf<String, String> tokensCache =(UtilCacheInf<String, String>) UtilCache.findCache("tokensCache");
	
	public boolean authenticate(String authCredentials) {
		if (null == authCredentials){
			return false;
		}
		return tokensCache.get(authCredentials)!=null;
	}
}
