/**
 * Project Name:CassEC
 * File Name:UtilCacheFactory.java
 * Package Name:org.ofbiz.base.util.cache
 * Date:2016年1月7日下午4:52:30
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package org.apache.ofbiz.base.util.cache;

import org.apache.ofbiz.base.util.UtilProperties;

/**
 * ClassName:UtilCacheFactory <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年1月7日 下午4:52:30 <br/>
 * @author   Administrator
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class UtilCacheFactory {
	protected boolean enabledRedisSession = true;
	private static UtilCacheFactory utilCacheFactory = null;
	private UtilCacheFactory(){
		enabledRedisSession = UtilProperties.getPropertyValue("redis.properties", "redis.cache.enabled").equals("Y");
	}
	public static synchronized  UtilCacheFactory instance(){
		if(utilCacheFactory == null){
				utilCacheFactory = new UtilCacheFactory(); 
		}
		return utilCacheFactory ;
	}
	
	public UtilCacheInf getCacheUtil(){
		if(enabledRedisSession){
			return UtilRedisCache.createUtilRedisCache();
		}else{
			return (UtilCacheInf) UtilCache.createUtilCache();
		}
	}
	
	public UtilCacheInf getCacheUtil(String name){
		if(enabledRedisSession){
			return UtilRedisCache.createUtilRedisCache(name);
		}else{
			return (UtilCacheInf) UtilCache.createUtilCache(name);
		}
	}
	
	public UtilCacheInf getCacheUtil(String name,long expire){
		if(enabledRedisSession){
			return UtilRedisCache.createUtilRedisCache(name,expire);
		}else{
			return (UtilCacheInf)UtilCache.createUtilCache(name,0,expire);
		}
	}
}

