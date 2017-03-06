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
package org.apache.ofbiz.base.util.cache;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.ObjectType;
import org.apache.ofbiz.base.util.UtilObject;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.base.util.jedis.JedisManager;

import redis.clients.jedis.Jedis;

/**
 * Generalized caching utility. Provides a number of caching features:
 * <ul>
 * <li>Limited or unlimited element capacity
 * <li>If limited, removes elements with the LRU (Least Recently Used) algorithm
 * <li>Keeps track of when each element was loaded into the cache
 * <li>Using the expireTime can report whether a given element has expired
 * <li>Counts misses and hits
 * </ul>
 */
@SuppressWarnings("serial")
public class UtilRedisCache<K, V> implements Serializable,UtilCacheInf<K,V> {

	public static final String module = UtilRedisCache.class.getName();

	/** A static Map to keep track of all of the UtilRedisCache instances. */
	private static final ConcurrentHashMap<String, UtilRedisCache<?, ?>> UtilRedisCacheTable = new ConcurrentHashMap<String, UtilRedisCache<?, ?>>();
	 
	/** The name of the UtilRedisCache instance, is also the key for the instance in UtilRedisCacheTable. */
	private final String name;

	/**
	 * Specifies the amount of time since initial loading before an element will be reported as expired. If set to 0, elements will never expire.
	 */
	protected long expireTimeNanos = 0;

	protected JedisSerializer<Object, Object> jedisTable = null;
	protected byte[] NULL_JEDISSERIALIZER = "null".getBytes();
	protected boolean enabledRedisSession = true;
	protected Integer cacheDb = 0;
	protected JedisManager jedisMgr;

	/**
	 * Constructor which specifies the cacheName as well as the sizeLimit, expireTime and useSoftReference. The passed sizeLimit, expireTime and useSoftReference will be overridden by values from cache.properties if found.
	 * 
	 * @param sizeLimit
	 *            The sizeLimit member is set to this value
	 * @param expireTime
	 *            The expireTime member is set to this value
	 * @param cacheName
	 *            The name of the cache.
	 * @param useSoftReference
	 *            Specifies whether or not to use soft references for this cache.
	 */
	private UtilRedisCache(String cacheName, long expireTimeMillis,String propName, String... propNames) {
		this.name = cacheName;
		this.expireTimeNanos = TimeUnit.NANOSECONDS.convert(expireTimeMillis, TimeUnit.MILLISECONDS);
		enabledRedisSession = UtilProperties.getPropertyValue("redis.properties", "redis.cache.enabled").equals("Y");
		cacheDb = UtilProperties.getPropertyAsInteger("redis.properties", "redis.cache.db", 
				UtilProperties.getPropertyAsInteger("redis.properties", "redis.default.db", 0));
		if (enabledRedisSession) {
			String host = UtilProperties.getPropertyValue("redis.properties", "redis.cache.host");
			String password = UtilProperties.getPropertyValue("redis.properties", "redis.cache.password");
			int port = UtilProperties.getPropertyAsInteger("redis.properties", "redis.cache.port", 6379);
			// IntrospectionUtils.setProperty(redisSessionManager, "timeout", "30");
			// IntrospectionUtils.setProperty(redisSessionManager, "serializationStrategyClass", "");
			jedisMgr = JedisManager.getInstence(host, port, password);
			jedisTable = new JedisSerializer<Object, Object>();
			Jedis jedis = jedisMgr.getJedis();
			try {
				byte[] data = JedisSerializer.serialize(jedisTable);
				if (data == null) {
					jedis.set(name.getBytes(), JedisSerializer.serialize(jedisTable));
				} else if (Arrays.equals(NULL_JEDISSERIALIZER, data)) {
					jedis.set(name.getBytes(), JedisSerializer.serialize(jedisTable));
				} 
				if(expireTimeMillis!=0){
					jedis.expireAt(name.getBytes(), expireTimeMillis);	
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (jedis != null)
					jedisMgr.returnResource(jedis);
			}
		}  
	}
 
	private Object fromKey(Object key) {
		return key == null ? ObjectType.NULL : key;
	}

	private K toKey(Object key) {
		return key == ObjectType.NULL ? null : (K) key;
	}
  

	public boolean isEmpty() {
		if (enabledRedisSession && jedisMgr != null) {
			try {
				jedisTable = loadJedisSerializerFromRedis(name);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return jedisTable.keySet().isEmpty();
		}
		return true;
	}

	/**
	 * Puts or loads the passed element into the cache
	 * 
	 * @param key
	 *            The key for the element, used to reference it in the hashtables and LRU linked list
	 * @param value
	 *            The value of the element
	 */
	public V put(K key, V value) {
		return putInternal(key, value, expireTimeNanos);
	}

	public V putIfAbsent(K key, V value) {
		return putIfAbsentInternal(key, value, expireTimeNanos);
	}

	public V putIfAbsentAndGet(K key, V value) {
		V cachedValue = putIfAbsent(key, value);
		return (cachedValue != null ? cachedValue : value);
	}
 

	/**
	 * Puts or loads the passed element into the cache
	 * 
	 * @param key
	 *            The key for the element, used to reference it in the hashtables and LRU linked list
	 * @param value
	 *            The value of the element
	 * @param expireTimeMillis
	 *            how long to keep this key in the cache
	 */
	public V put(K key, V value, long expireTimeMillis) {
		return putInternal(key, value, TimeUnit.NANOSECONDS.convert(expireTimeMillis, TimeUnit.MILLISECONDS));
	}

	public V putIfAbsent(K key, V value, long expireTimeMillis) {
		return putIfAbsentInternal(key, value, TimeUnit.NANOSECONDS.convert(expireTimeMillis, TimeUnit.MILLISECONDS));
	}

	V putInternal(K key, V value, long expireTimeNanos) {
		Object nulledKey = fromKey(key);
		V oldValue;
		if (enabledRedisSession && jedisMgr != null) {
			try {
				jedisTable = loadJedisSerializerFromRedis(name);
			} catch (IOException e) {
				Debug.logWarning(e, module);
			}
			if (jedisTable == null) {
				enabledRedisSession = false;
				putIfAbsentInternal(key, value, expireTimeNanos);
			} else {
				synchronized (this) {
					oldValue = (V) jedisTable.get(nulledKey);
					jedisTable.put(nulledKey, value);
					try {
						byte[] data = JedisSerializer.serialize(jedisTable);
						if (data == null) {
							return value;
						} else if (Arrays.equals(NULL_JEDISSERIALIZER, data)) {
							throw new IllegalStateException("Race condition encountered: attempted to load key [" + nulledKey + "] which has been created but not yet serialized.");
						} else {
							Jedis jedis = jedisMgr.getJedis();
							jedis.set(name.getBytes(), data);
							if(expireTimeNanos != 0){
								jedis.expireAt(name.getBytes(), expireTimeNanos);
							}
							jedisMgr.returnResource(jedis);
							return value;
						}
					} catch (IOException e) {
						Debug.logWarning(e, module);
					}
				}
			}
		}  
		return null;
	}

	V putIfAbsentInternal(K key, V value, long expireTimeNanos) {
		Object nulledKey = fromKey(key);
		V oldValue;
		if (enabledRedisSession && jedisMgr != null) {
			try {
				jedisTable = loadJedisSerializerFromRedis(name);
			} catch (IOException e) {
				Debug.logWarning(e, module);
			}
			if (jedisTable == null) {
				enabledRedisSession = false;
				oldValue = null;
				// 如果未成功获取JedisSerializer对象，则使用原来的缓存方式
				putIfAbsentInternal(key, value, expireTimeNanos);
			} else {
				synchronized (this) {
					oldValue = (V) jedisTable.get(nulledKey);
					if (oldValue == null) {
						jedisTable.put(nulledKey, value);
						try {
							byte[] data = JedisSerializer.serialize(jedisTable);
							if (data == null) {
								return value;
							} else if (Arrays.equals(NULL_JEDISSERIALIZER, data)) {
								throw new IllegalStateException("Race condition encountered: attempted to load key [" + nulledKey + "] which has been created but not yet serialized.");
							} else {
								Jedis jedis = jedisMgr.getJedis();
								jedis.set(name.getBytes(), data);
								if(expireTimeNanos != 0){
									jedis.expireAt(name.getBytes(), expireTimeNanos);
								}
								jedisMgr.returnResource(jedis);
								return value;
							}
						} catch (IOException e) {
							Debug.logWarning(e, module);
						}
					}
				}
			}
		} 
		return null;
	}

	/**
	 * Gets an element from the cache according to the specified key.
	 * 
	 * @param key
	 *            The key for the element, used to reference it in the hashtables and LRU linked list
	 * @return The value of the element specified by the key
	 */
	public V get(Object key) {
		Object nulledKey = fromKey(key);
		if (enabledRedisSession && jedisMgr != null) {
			try {
				jedisTable = loadJedisSerializerFromRedis(name);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (jedisTable == null) {
				return null;
			} else {
				Object o = jedisTable.get(nulledKey);
				if(UtilValidate.isEmpty(o)){
					return null;
				}
				return 	(V) o;
			}
		} 
		return null;
	}

	public Collection<V> values() {
		if (enabledRedisSession && jedisMgr != null) {
			try {
				jedisTable = loadJedisSerializerFromRedis(name);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (jedisTable == null) {
				return null;
			} else {
				return 	(Collection<V>) jedisTable.keySet();
			}
		}  
		return null;
	}

	private long findSizeInBytes(Object o) {
		try {
			if (o == null) {
				if (Debug.infoOn())
					Debug.logInfo("Found null object in cache: " + getName(), module);
				return 0;
			}
			if (o instanceof Serializable) {
				return UtilObject.getByteCount(o);
			} else {
				if (Debug.infoOn())
					Debug.logInfo("Unable to compute memory size for non serializable object; returning 0 byte size for object of " + o.getClass(), module);
				return 0;
			}
		} catch (NotSerializableException e) {
			// this happens when we try to get the byte count for an object which itself is
			// serializable, but fails to be serialized, such as a map holding unserializable objects
			if (Debug.warningOn()) {
				Debug.logWarning("NotSerializableException while computing memory size; returning 0 byte size for object of " + e.getMessage(), module);
			}
			return 0;
		} catch (Exception e) {
			Debug.logWarning(e, "Unable to compute memory size for object of " + o.getClass(), module);
			return 0;
		}
	}
	 

	/**
	 * Removes an element from the cache according to the specified key
	 * 
	 * @param key
	 *            The key for the element, used to reference it in the hashtables and LRU linked list
	 * @return The value of the removed element specified by the key
	 */
	public V remove(Object key) {
		return this.removeInternal(key);
	}

	/** This is used for internal remove calls because we only want to count external calls */
	@SuppressWarnings("unchecked")
	protected synchronized V removeInternal(Object key) {
		if (key == null) {
			if (Debug.verboseOn())
				Debug.logVerbose("In UtilRedisCache tried to remove with null key, using NullObject" + this.name, module);
		}
		Object nulledKey = fromKey(key);
		 V oldValue;
		if (enabledRedisSession && jedisMgr != null) {
			try {
				jedisTable = loadJedisSerializerFromRedis(name);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (jedisTable == null) {
				return null;
			} else {
				oldValue = (V) jedisTable.get(nulledKey);
				jedisTable.remove(nulledKey);
				try {
					byte[] data = JedisSerializer.serialize(jedisTable);
					if (data == null) {
						return null;
					} else if (Arrays.equals(NULL_JEDISSERIALIZER, data)) {
						throw new IllegalStateException("Race condition encountered: attempted to load key [" + nulledKey + "] which has been created but not yet serialized.");
					} else {
						Jedis jedis = jedisMgr.getJedis();
						jedis.set(name.getBytes(), data);
						jedisMgr.returnResource(jedis);
						return oldValue;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
		}  
		return null;
	}

	/** Removes all elements from this cache */
	public synchronized void erase() {
		if (enabledRedisSession && jedisMgr != null) {
			try {
				jedisTable = loadJedisSerializerFromRedis(name);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (jedisTable != null) {
				jedisTable.clear();
				try {
					byte[] data = JedisSerializer.serialize(jedisTable);
					if (data != null && !(Arrays.equals(NULL_JEDISSERIALIZER, data))) {
						Jedis jedis = jedisMgr.getJedis();
						jedis.del(name.getBytes());
						jedisMgr.returnResource(jedis);
					}  
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}  
		}   
	}

	public void clear() {
		erase();
	}

	/** Removes all elements from this cache */
	public static void clearAllCaches() {
		// We make a copy since clear may take time
		for (UtilRedisCache<?, ?> cache : UtilRedisCacheTable.values()) {
			cache.clear();
		}
	}

	protected JedisSerializer<Object, Object> loadJedisSerializerFromRedis(String cacheName) throws IOException {
		JedisSerializer<Object, Object> jedisSerializer;
		Jedis jedis = null;
		try {
			jedis = jedisMgr.getJedis();
			byte[] data = jedis.get(cacheName.getBytes());
			if (data == null) {
				jedisSerializer = new JedisSerializer<Object, Object>();
			} else if (Arrays.equals(NULL_JEDISSERIALIZER, data)) {
				throw new IllegalStateException("Race condition encountered: attempted to load session[" + cacheName + "] which has been created but not yet serialized.");
			} else {
				jedisSerializer = (JedisSerializer<Object, Object>) JedisSerializer.deserialize(data);
				if (jedisSerializer == null) {
					jedisSerializer = new JedisSerializer<Object, Object>();
				}
			}
			return jedisSerializer;
		} catch (Exception e) {
			throw e;
		} finally {
			if (jedis != null) {
				jedisMgr.returnResource(jedis);
			}
		}
	}

	public static Set<String> getUtilRedisCacheTableKeySet() {
		Set<String> set = new HashSet<String>(UtilRedisCacheTable.size());
		set.addAll(UtilRedisCacheTable.keySet());
		return set;
	}

	/**
	 * Getter for the name of the UtilRedisCache instance.
	 * 
	 * @return The name of the instance
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the expire time for the cache elements. If 0, elements never expire.
	 * 
	 * @param expireTimeMillis
	 *            The expire time for the cache elements
	 */
	public void setExpireTime(long expireTimeMillis) {
		// if expire time was <= 0 and is now greater, fill expire table now
		 
	}

	/**
	 * return the current expire time for the cache elements
	 * 
	 * @return The expire time for the cache elements
	 */
	public long getExpireTime() {
		return TimeUnit.MILLISECONDS.convert(expireTimeNanos, TimeUnit.NANOSECONDS);
	}
 
	/**
	 * Returns the number of elements currently in the cache
	 * 
	 * @return The number of elements currently in the cache
	 */
	public int size() {
		if (enabledRedisSession && jedisMgr != null) {
			try {
				jedisTable = loadJedisSerializerFromRedis(name);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (jedisTable != null) {
				 return jedisTable.size();
			}  
		}   
		return 0;
	}

	/**
	 * Returns a boolean specifying whether or not an element with the specified key is in the cache.
	 * 
	 * @param key
	 *            The key for the element, used to reference it in the hashtables and LRU linked list
	 * @return True is the cache contains an element corresponding to the specified key, otherwise false
	 */
	public boolean containsKey(Object key) {
		Object nulledKey = fromKey(key);
		if (enabledRedisSession && jedisMgr != null) {
			try {
				jedisTable = loadJedisSerializerFromRedis(name);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (jedisTable != null) {
				 return jedisTable.containsKey(nulledKey);
			}  
		}   
		return false;
	}

	  

	/** Checks for a non-expired key in a specific cache */
	public static boolean validKey(String cacheName, Object key) {
		UtilRedisCache<?, ?> cache = findCache(cacheName);
		if (cache != null) {
			if (cache.containsKey(key))
				return true;
		}
		return false;
	}

	public static void clearCachesThatStartWith(String startsWith) {
		for (Map.Entry<String, UtilRedisCache<?, ?>> entry : UtilRedisCacheTable.entrySet()) {
			String name = entry.getKey();
			if (name.startsWith(startsWith)) {
				UtilRedisCache<?, ?> cache = entry.getValue();
				cache.clear();
			}
		}
	}

	public static void clearCache(String cacheName) {
		UtilRedisCache<?, ?> cache = findCache(cacheName);
		if (cache == null)
			return;
		cache.clear();
	}

	@SuppressWarnings("unchecked")
	public static <K, V> UtilRedisCache<K, V> getOrCreateUtilRedisCache(String name, long expireTime, String... names) {
		UtilRedisCache<K, V> existingCache = (UtilRedisCache<K, V>) UtilRedisCacheTable.get(name);
		if (existingCache != null)
			return existingCache;
		String cacheName = name ;
		UtilRedisCache<K, V> newCache = new UtilRedisCache<K, V>(cacheName,  expireTime, name, names);
		UtilRedisCacheTable.putIfAbsent(name, newCache);
		return (UtilRedisCache<K, V>) UtilRedisCacheTable.get(name);
	}
 
	public static <K, V> UtilRedisCache<K, V> createUtilRedisCache(String name,long expireTime) {
		return storeCache(new UtilRedisCache<K, V>(name,expireTime,name));
	}

	 
	public static <K, V> UtilRedisCache<K, V> createUtilRedisCache (long expireTime) {
		String cacheName = "specified" ;
		return storeCache(new UtilRedisCache<K, V>(cacheName,expireTime,"specified"));
	}

	public static <K, V> UtilRedisCache<K, V> createUtilRedisCache(String name) {
		return storeCache(new UtilRedisCache<K, V>(name, 0, "default", name));
	}
	public static <K, V> UtilRedisCache<K, V> getOrCreateUtilRedisCache(String name) {
		return storeCache(new UtilRedisCache<K, V>(name, 0, "default", name));
	}

	public static <K, V> UtilRedisCache<K, V> createUtilRedisCache() {
		return storeCache(new UtilRedisCache<K, V>("default", 0,"default"));
	}

	private static <K, V> UtilRedisCache<K, V> storeCache(UtilRedisCache<K, V> cache) {
		UtilRedisCacheTable.put(cache.getName(), cache);
		return cache;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> UtilRedisCache<K, V> findCache(String cacheName) {
		return (UtilRedisCache<K, V>) UtilRedisCache.UtilRedisCacheTable.get(cacheName);
	}

//	@Override
//	public UtilCacheInf<K, V> create() {
//		UtilCacheInf<K, V>  cache = createUtilRedisCache();
//		if(jedisMgr == null || !enabledRedisSession){
//			cache = UtilCache.createUtilCache();
//		}
//		return  cache;
//	}
//
//	@Override
//	public UtilCacheInf<K, V> create(String name, long expire) {
//		UtilCacheInf<K, V>  cache = createUtilRedisCache(name,expire);
//		if(jedisMgr == null || !enabledRedisSession){
//			cache = UtilCache.createUtilCache(name,expire);
//		}
//		return  cache;
//	}
 
}
