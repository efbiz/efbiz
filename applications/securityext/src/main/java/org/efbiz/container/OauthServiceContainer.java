/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.efbiz.container;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import org.apache.ofbiz.base.container.Container;
import org.apache.ofbiz.base.container.ContainerConfig;
import org.apache.ofbiz.base.container.ContainerException;
import org.apache.ofbiz.base.start.Start;
import org.apache.ofbiz.base.start.StartupCommand;
import org.apache.ofbiz.base.util.Debug;

/**
 * NamingServiceContainer
 *
 */

public class OauthServiceContainer implements Container {

    public static final String module = OauthServiceContainer.class.getName();
    
    protected String configFileLocation = null;
    protected boolean isRunning = false;
    protected int oauthPort = 10087;
    protected String oauthHost = null;
    protected String oauthApp = "oauth";
    protected String packages = "org.apache.oltu.oauth2.integration.endpoints";
    protected final HashMap<String, String> initParams = new HashMap<String, String>();
    private String name;
    protected HttpServer oauthServer;
    public boolean start() throws ContainerException {
    	String oauthUri = "http://" + oauthHost + ":" + oauthPort +"/" + oauthApp;
    	try {
    		Debug.logInfo("认证服务启动，访问地址：" + oauthUri, module);
			oauthServer = GrizzlyWebContainerFactory.create(URI.create(oauthUri),initParams);
		} catch (IOException e) {
			Debug.logError(e, "认证服务启动失败！",module);
		}

        isRunning = true;
        return isRunning;
    }

    public void stop() throws ContainerException {
        if (isRunning) {
            oauthServer.shutdown();
            isRunning = false;
        }
    }

    public String getName() {
        return name;
    }

	@Override
	public void init(List<StartupCommand> ofbizCommands, String name, String configFile) throws ContainerException {
		

        this.name =name;
        this.configFileLocation = configFile;

        ContainerConfig.Configuration cfg = ContainerConfig.getConfiguration(name, configFile);

        String port = ContainerConfig.getPropertyValue(cfg, "port", "10087"); 
        if (port != null) {
            try {
                this.oauthPort = Integer.parseInt(port) + Start.getInstance().getConfig().portOffset;
            } catch (Exception e) {
                throw new ContainerException("Invalid port defined in container [naming-container] configuration or as portOffset; not a valid int");
            }
        }

        String host = ContainerConfig.getPropertyValue(cfg, "host", "localhost"); 
        if (host != null ) {
            this.oauthHost =  host ;
        }
        
        String packages = ContainerConfig.getPropertyValue(cfg, "packages", "org.apache.oltu.oauth2.integration.endpoints"); 
        if (packages != null ) {
        	this.packages =  packages;
        }
        
        String oauthApp = ContainerConfig.getPropertyValue(cfg, "oauthApp", "oauth"); 
        if (oauthApp != null) {
        	this.oauthApp =  oauthApp;
        }
        
    	initParams.put("jersey.config.server.provider.packages", this.packages);
	}
}
