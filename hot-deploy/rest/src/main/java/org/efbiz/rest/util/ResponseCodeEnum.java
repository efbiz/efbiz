/**
 * Project Name:CassEC
 * File Name:RestErrorCodeContant.java
 * Package Name:org.ofbiz.rest.util
 * Date:2016年5月18日上午10:33:43
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package org.efbiz.rest.util;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;


/**
 * ClassName:RestErrorCodeContant <br/>
 * Function: Rest接口错误码返回常量类 <br/>
 * Date:     2016年5月18日 上午10:33:43 <br/>
 * @author   thanos
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */

public enum ResponseCodeEnum{
	HTTP_CODE_0("0",""), 
	HTTP_CODE_100("100","继续"),  
	HTTP_CODE_101("101","切换协议"),  
 
	HTTP_CODE_200("200","成功"),
	HTTP_CODE_201("201","已创建"),  
	HTTP_CODE_202("202","已接受"),  
	HTTP_CODE_203("203","非授权信息"),  
	HTTP_CODE_204("204","无内容"),  
	HTTP_CODE_205("205","重置内容"),  
	HTTP_CODE_206("206","部分内容"),  
  
	HTTP_CODE_300("300","多种选择"),  
	HTTP_CODE_301("301","永久移动"),  
	HTTP_CODE_302("302","临时移动"),  
	HTTP_CODE_303("303","查看其他位置"),  
	HTTP_CODE_304("304","未修改"),  
	HTTP_CODE_305("305","使用代理"),  
	HTTP_CODE_306("306","已经被废弃的HTTP状态码"),  
	HTTP_CODE_307("307","临时重定向"),  
	
	HTTP_CODE_400("400","错误请求"),  
	HTTP_CODE_401("401","未授权"),  
	HTTP_CODE_402("402",""),  //402	Payment Required 保留，将来使用
	HTTP_CODE_403("403","禁止"),  
	HTTP_CODE_404("404","未找到"),  
	HTTP_CODE_405("405","方法禁用"),  
	HTTP_CODE_406("406","不接受"),  
	HTTP_CODE_407("407","需要代理授权"),  
	HTTP_CODE_408("408","请求超时"),  
	HTTP_CODE_409("409","冲突"),  
	HTTP_CODE_410("410","已删除"),  
	HTTP_CODE_411("411","需要有效长度"),  
	HTTP_CODE_412("412","未满足前提条件"),  
	HTTP_CODE_413("413","请求实体过大"),  
	HTTP_CODE_414("414","请求的 URI过长"),  
	HTTP_CODE_415("415","不支持的媒体类型"),  
	HTTP_CODE_416("416","请求范围不符合要求"),  
	HTTP_CODE_417("417","未满足期望值"),  
	
	HTTP_CODE_500("500","服务器内部错误"),  
	HTTP_CODE_501("501","尚未实施"),  
	HTTP_CODE_502("502","错误网关"),  
	HTTP_CODE_503("503","服务不可用"),  
	HTTP_CODE_504("504","网关超时"),  
	HTTP_CODE_505("505","HTTP版本不受支持") ;
	 
    /** 键 */
    private String key;
    /** 值 */
    private String value;

    private ResponseCodeEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public String getValue() {
        return this.value;
    }

    /** 保存key value对的map */
    public static Map<String, String> map = new HashMap<String, String>();

    static {
    	ResponseCodeEnum[] enums = ResponseCodeEnum.values();
        for (ResponseCodeEnum objEnum : enums) {
            map.put(objEnum.getKey(), objEnum.getValue());
        }

    }

    /**
     * 根据key获得name
     *
     * @param key
     * @return
     */
    public static String getValue(String strKey) {
        if (StringUtils.isNotBlank(strKey)) {
            String strName = map.get(strKey);
            return strName;
        }
        return "";
    } 
}