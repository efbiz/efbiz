package org.efbiz.rest.util;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;

import org.apache.commons.codec.digest.DigestUtils;


/** 
* 功能：富有MD5签名处理核心文件，不需要修改
* 版本：3.3
* 修改日期：2012-08-17
* */

public class MD5Util {

    /**
     * 签名字符串
     * @param text 需要签名的字符串
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static String sign(String text, String key, String input_charset) {
    	text = text + "|" + key;
        return DigestUtils.md5Hex(getContentBytes(text, input_charset));
    }
    
    /**
     * 签名字符串
     * @param text 需要签名的字符串
     * @param sign 签名结果
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static boolean verify(String text, String sign, String key, String input_charset) {
    	text = text + "|"  + key;
    	String mysign = DigestUtils.md5Hex(getContentBytes(text, input_charset));
    	if(mysign.equals(sign)) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    /**
     * @param content
     * @param charset
     * @return
     * @throws SignatureException
     * @throws UnsupportedEncodingException 
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }

    
    public static void main(String[] args) {
    	String xml ="<?xml version='1.0' encoding='UTF-8' ?><AP><body><rspCode>0000</rspCode><rspDesc>成功</rspDesc><notifyType>01</notifyType><notify01><eicSsn>P16071200441151468307888462000</eicSsn><fuiouTransNo>201607121513232713640543618297</fuiouTransNo><amt>8100</amt><requestSet/></notify01></body><sign>fda88ccff387a85a54574b2577a53c5b</sign></AP>";
    	int body_start = xml.indexOf("<body>"); 
		int body_end = xml.indexOf("</body>") +7; 
    	String bodyStr = xml.substring(body_start,body_end);
    	System.out.println(bodyStr);
    	String sign = "fda88ccff387a85a54574b2577a53c5b";
    	String mysign =  MD5Util.sign(bodyStr,"key", "utf-8");
    	System.out.println(sign.equals(mysign));
    	
    	String signTest = MD5Util.sign("helll=world@signtype=MD5", "efbiz", "utf-8");
    	System.out.println(signTest);
	}
}