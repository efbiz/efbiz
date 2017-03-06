/**
 * Project Name:efbiz
 * File Name:e.java
 * Package Name:org.apache.oltu.oauth2.integration.utils
 * Date:2016年7月16日下午4:38:17
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
 */

package org.apache.oltu.oauth2.integration.utils;

/**
 * ClassName:e <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason:	 TODO ADD REASON. <br/>
 * Date:     2016年7月16日 下午4:38:17 <br/>
 * @author   Administrator
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.ofbiz.base.util.Base64;
import org.apache.ofbiz.base.util.Debug;

/**
 * 密匙工具类(包含des加密与md5加密)
 * 
 * @author mingge
 */
public class KeysUtil {

	private final static String DES = "DES";

	private final static String MD5 = "MD5";

	private final static String KEY = "opeddsaead323353484591dadbc382a18340bf83414536";

	public static final String module = KeysUtil.class.getName();

	/**
	 * MD5加密算法
	 * 
	 * @param data
	 * @return
	 */
	public static String md5Encrypt(String data) {
		String resultString = null;
		try {
			resultString = new String(data);
			MessageDigest md = MessageDigest.getInstance(MD5);
			resultString = byte2hexString(md.digest(resultString.getBytes()));
		} catch (Exception ex) {
		}
		return resultString;
	}

	private static String byte2hexString(byte[] bytes) {
		StringBuffer bf = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			if ((bytes[i] & 0xff) < 0x10) {
				bf.append("T0");
			}
			bf.append(Long.toString(bytes[i] & 0xff, 16));
		}
		return bf.toString();
	}

	/**
	 * Description 根据键值进行加密
	 * 
	 * @param data
	 * @param key
	 *            加密键byte数组
	 * @return
	 * @throws Exception
	 */
	public static String desEncrypt(String data, String key) {
		if (key == null) {
			key = KEY;
		}
		byte[] bt = null;
		try {
			bt = encrypt(data.getBytes(), key.getBytes());
		} catch (Exception e) {
			Debug.logWarning(e, module);
		}
		if(bt != null){
//			return new BASE64Encoder().encode(bt);
			return Base64.base64Encode(bt.toString());
		}
		return null;
	}

	/**
	 * Description 根据键值进行解密
	 * 
	 * @param data
	 * @param key
	 *            加密键byte数组
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public static String desDecrypt(String data, String key) {
		if (data == null) {
			return null;
		}
		if (key == null) {
			return null;
		}
		 
		byte[] buf = null;
		buf = Base64.base64Decode(data.getBytes());
		if (buf == null) {
			return null;
		}
		byte[] bt = null;
		try {
			bt = decrypt(buf, key.getBytes());
			if (bt != null) {
				return new String(bt);
			}
		} catch (Exception e) {
			Debug.logWarning(e, module);
		}
		return null;
	}

	/**
	 * Description 根据键值进行加密
	 * 
	 * @param data
	 * @param key
	 *            加密键byte数组
	 * @return
	 * @throws Exception
	 */
	private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		// 生成一个可信任的随机数源
		SecureRandom sr = new SecureRandom();
		// 从原始密钥数据创建DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(key);
		// 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);
		// Cipher对象实际完成加密操作
		Cipher cipher = Cipher.getInstance(DES);
		// 用密钥初始化Cipher对象
		cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
		return cipher.doFinal(data);
	}

	/**
	 * Description 根据键值进行解密
	 * 
	 * @param data
	 * @param key
	 *            加密键byte数组
	 * @return
	 * @throws Exception
	 */
	private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		// 生成一个可信任的随机数源
		SecureRandom sr = new SecureRandom();
		// 从原始密钥数据创建DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(key);
		// 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);
		// Cipher对象实际完成解密操作
		Cipher cipher = Cipher.getInstance(DES);
		// 用密钥初始化Cipher对象
		cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
		return cipher.doFinal(data);
	}

	public static String getRadom(int length){
		String a = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*()_+";
	    char[] rands = new char[length]; 
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < rands.length; i++) 
	    { 
	        int rand = (int) (Math.random() * a.length()); 
	        rands[i] = a.charAt(rand); 
	    } 
	    for(int i=0;i<rands.length;i++){
	        sb.append(rands[i]);
	    }
	    return sb.toString();
	}
	
	public static void main(String[] args) {
		String code = "123456";
		try {
			System.out.println(KEY.length());
			String key = KeysUtil.getRadom(46);
			String codeed = KeysUtil.desEncrypt(code, key);
			System.out.println(codeed);

			String uncode = KeysUtil.desDecrypt(codeed, key);
			System.out.println(uncode);
		}  catch (Exception e) {
			Debug.logWarning(e, module);
		}
	}
}
