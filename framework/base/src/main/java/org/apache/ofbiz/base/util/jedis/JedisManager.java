/**
 * Project Name:CassEC
 * File Name:JedisManager.java
 * Package Name:com.casstime.ec.common.jedis
 * Date:2015年11月24日下午3:54:26
 * Copyright (c) 2015, chenzhou1025@126.com All Rights Reserved.
 *
 */

package org.apache.ofbiz.base.util.jedis;

import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisException;

/**
 * ClassName:JedisManager <br/>
 * Function: Jedis 缓存管理. <br/>
 * Date: 2016年5月24日 下午3:54:26 <br/>
 * 
 * @author zhijun.tan
 * @version
 * @since JDK 1.6
 * @see
 */
public class JedisManager {

	private static final String JEDISHOST = UtilProperties.getPropertyValue("redis.properties", "redis.cache.host", "localhost");
	private static final Integer JEDISPORT = UtilProperties.getPropertyAsInteger("redis.properties", "redis.cache.port", 6379);
	private static final String JEDISPASSWORD = UtilProperties.getPropertyValue("redis.properties", "redis.cache.password", "Cass8888");
	private static final Integer JEDISMAXIDLE = UtilProperties.getPropertyAsInteger("redis.properties", "redis.cache.maxIdle", 1000);
	private static final Integer JEDISMAXACTIVE = UtilProperties.getPropertyAsInteger("redis.properties", "redis.cache.maxActive", 1000);
	private static final Integer CACHEDB = UtilProperties.getPropertyAsInteger("redis.properties", "redis.cache.db", 0 );

	protected String host = JEDISHOST;
	protected int port = JEDISPORT;
	protected int database = CACHEDB;
	protected int maxIdle = JEDISMAXIDLE;
	protected int maxActive = JEDISMAXACTIVE;
	protected String password = JEDISPASSWORD;
	protected int timeout = Protocol.DEFAULT_TIMEOUT;
	protected JedisPool jedisPool = null;
	private static JedisManager jedisMgr = null;
	
	private JedisManager() {
		this.jedisPool = getJedisPool();
	}

	private JedisManager(String host, int port, String password) {
		this.host = host;
		this.port = port;
		this.password = password;
		this.jedisPool = getJedisPool();
	}

	private JedisManager(String host, int port, String password, int maxIdle, int maxActive) {
		this.host = host;
		this.port = port;
		this.password = password;
		this.maxIdle = maxIdle;
		this.maxActive = maxActive;
		this.jedisPool = getJedisPool();
	}
	
	public static JedisManager getInstence(){
		if(UtilValidate.isEmpty(jedisMgr)){
			jedisMgr = new JedisManager();
			return jedisMgr;
		}else{
			return jedisMgr;
		}
	}
	
	public static JedisManager getInstence(String host, int port, String password){
		if(UtilValidate.isEmpty(jedisMgr)){
			jedisMgr = new JedisManager(host,port,password);
			return jedisMgr;
		}else{
			return jedisMgr;
		}
	}
	
	public static JedisManager getInstence(String host, int port, String password,int maxIdle, int maxActive){
		if(UtilValidate.isEmpty(jedisMgr)){
			jedisMgr = new JedisManager(host,port,password,maxIdle,maxActive);
			return jedisMgr;
		}else{
			return jedisMgr;
		}
	}
	
	private JedisPool getJedisPool() {
		if(UtilValidate.isEmpty(jedisPool)){
			// 创建jedis池配置实例
			JedisPoolConfig config = new JedisPoolConfig();
			// 设置池配置项值
			config.setMaxTotal(maxActive);
			config.setMaxIdle(maxIdle);
			config.setMaxWaitMillis(timeout);
			
			if (UtilValidate.isNotEmpty(password)) {
				jedisPool = new JedisPool(config, host, Integer.valueOf(port), timeout, password, database);
			} else {
				jedisPool = new JedisPool(config, host, Integer.valueOf(port), timeout, null, database);
			}
		}
		return jedisPool;
	}
	
	/**
	 * 获取Jedis实例
	 * 
	 * @return
	 */
	public synchronized Jedis getJedis() {
		try {
			return getJedisPool().getResource();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 释放jedis资源
	 * 
	 * @param jedis
	 */
	public void returnResource(final Jedis jedis) {
		if (jedis != null) {
			jedisPool.returnResourceObject(jedis);
		}
	}

	/**
	 * 查询默认DB数据
	 */
	public String find(String key) {
		return find(key, 0);
	}

	/**
	 * 查询指定DB数据
	 */
	public String find(String key, int dbIndex) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if (UtilValidate.isNotEmpty(dbIndex) || dbIndex != 0) {
				jedis.select(dbIndex);
			}
			return jedis.get(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResourceObject(jedis);
		}
	}

	/**
	 * 查询特定字符串
	 */
	public String findSubStr(String key, Integer startOffset, Integer endOffset) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.getrange(key, startOffset, endOffset);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			jedisPool.returnResourceObject(jedis);
		}
	}

	/**
	 * 向默认DB缓存中设置字符串内容 新增数据|修改
	 * 
	 * @param key
	 *            key
	 * @param value
	 *            value
	 * @return
	 * @throws Exception
	 */
	public int add(String key, String value) throws Exception {
		return add(key, value, 0);
	}

	/**
	 * 向指定DB缓存中设置字符串内容 新增数据|修改
	 * 
	 * @param key
	 *            key
	 * @param value
	 *            value
	 * @return
	 * @throws Exception
	 */
	public int add(String key, String value, int dbIndex) throws Exception {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if (UtilValidate.isNotEmpty(dbIndex) || dbIndex != 0) {
				jedis.select(dbIndex);
			}
			jedis.set(key, value);
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			jedisPool.returnResourceObject(jedis);
		}
	}
	
	/**
     * 向指定DB缓存中设置字符串内容 新增数据|修改
     * 
     * @param key
     *            key
     * @param value
     *            value
     * @return
     * @throws Exception
     */
    public int add(String key, String value, int dbIndex,int expiretime) throws Exception {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (UtilValidate.isNotEmpty(dbIndex) || dbIndex != 0) {
                jedis.select(dbIndex);
            }
            jedis.set(key, value);
            if (UtilValidate.isNotEmpty(expiretime) && expiretime > 0) {
                jedis.expire(key,expiretime);
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            jedisPool.returnResourceObject(jedis);
        }
    }
    

	/**
	 * 删除指定DB缓存中得对象，根据key
	 * 
	 * @param key
	 * @return
	 */
	public int del(String key) {
		return del(key, 0);
	}

	/**
	 * 删除指定DB缓存中得对象，根据key
	 * 
	 * @param key
	 * @return
	 */
	public int del(String key, int dbIndex) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			if (UtilValidate.isNotEmpty(dbIndex) || dbIndex != 0) {
				jedis.select(dbIndex);
			}
			jedis.del(key);
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			jedisPool.returnResourceObject(jedis);
		}
	}

	/**
	 * 创建 Jedis
	 * 
	 * @param
	 * @return
	 */
	public Jedis createJedisServer() {
		return createJedisServer(host, port, timeout, password);
	}

	/**
	 * 创建 Jedis
	 * 
	 * @param
	 * @return
	 */
	public Jedis createJedisServer(String host, int port, int timeout, String password) {
		if (UtilValidate.isEmpty(host) || UtilValidate.isEmpty(port)) {
			throw new JedisException("服务器地址或端口错误");
		}

		Jedis jedis = null;
		if (UtilValidate.isEmpty(timeout) || timeout == -1) {
			jedis = new Jedis(host, port);
		} else {
			jedis = new Jedis(host, port, timeout);
		}

		if (UtilValidate.isNotEmpty(password)) {
			jedis.auth(password);
		}
		return jedis;
	}

	/**
	 * 创建 Jedis
	 * 
	 * @param
	 * @return
	 */
	public Jedis createJedisServer(String host, int port, int timeout) {
		return createJedisServer(host, port, timeout, null);
	}

	/**
	 * 创建 Jedis
	 * 
	 * @param
	 * @return
	 */
	public Jedis createJedisServer(String host, int port) {
		return createJedisServer(host, port, -1, null);
	}
}
