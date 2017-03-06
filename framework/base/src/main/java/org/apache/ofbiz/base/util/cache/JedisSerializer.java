/**
 * Project Name:CassEC
 * File Name:JedisSerializer.java
 * Package Name:org.ofbiz.base.util.cache
 * Date:2016年1月6日上午8:39:14
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package org.apache.ofbiz.base.util.cache;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.ofbiz.base.lang.JSON;
import org.apache.ofbiz.base.util.UtilObject;

/**
 * ClassName:JedisSerializer <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年1月6日 上午8:39:14 <br/>
 * @author   Administrator
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class JedisSerializer<K, V> extends HashMap<K, V> implements Serializable {
    /**
	 * serialVersionUID:TODO(用一句话描述这个变量表示什么).
	 * @since JDK 1.6
	 */
	private static final long serialVersionUID = 1L;

	public static byte[] serialize(Object o) throws IOException {
    	JSON json= JSON.from(o);
        return UtilObject.getBytes(json);
    }

    public static Object deserialize(byte[] bytes) throws IOException {
    	JSON json = (JSON)UtilObject.getObject(bytes);
    	JedisSerializer<?, ?> o =  json.toObject(JedisSerializer.class);
    	if(!o.isEmpty()){
    		System.out.println(o);
    		for(Object key :o.keySet()){
    			System.out.println(o.get(key));
    			System.out.println(o.get(key).getClass().getCanonicalName());
    		}
    	}
    	return o;
    }
   
}

