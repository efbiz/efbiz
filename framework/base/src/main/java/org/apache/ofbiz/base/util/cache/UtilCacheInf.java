/**
 * Project Name:CassEC
 * File Name:UtilCacheInf.java
 * Package Name:org.ofbiz.base.util.cache.cachefactory
 * Date:2016年1月7日下午3:36:48
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package org.apache.ofbiz.base.util.cache;
/**
 * ClassName:UtilCacheInf <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年1月7日 下午3:36:48 <br/>
 * @author   Administrator
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public interface  UtilCacheInf<K, V> {
	 public V remove(K key) ;
	 public void erase();
	 public V get(K key) ;
	 public V put(K key, V value, long expireTimeMillis);
	 public V put(K key, V value);
}

