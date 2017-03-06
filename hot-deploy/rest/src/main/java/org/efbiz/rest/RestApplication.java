/**
 * Project Name:efbiz
 * File Name:RestApplication.java
 * Package Name:org.efbiz.rest
 * Date:2016年8月12日上午9:22:05
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package org.efbiz.rest;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * ClassName:RestApplication <br/>
 * Date:     2016年8月12日 上午9:22:05 <br/>
 * @author   thanos
 * @version  
 * @since    JDK 1.8
 * @see 	 
 */
@ApplicationPath("")
public class RestApplication extends ResourceConfig {
    public RestApplication() {
        packages("org.efbiz.rest");
    }
}

