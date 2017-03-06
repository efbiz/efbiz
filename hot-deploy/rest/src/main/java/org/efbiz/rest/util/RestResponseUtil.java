/**
 * Project Name:CassEC
 * File Name:RestErrorCodeContant.java
 * Package Name:org.ofbiz.rest.util
 * Date:2016年5月18日上午10:33:43
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package org.efbiz.rest.util;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.ofbiz.base.lang.JSON;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilValidate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;

/**
 * ClassName:RestErrorCodeContant <br/>
 * Function: Rest接口错误码返回常量类 <br/>
 * Date: 2016年5月18日 上午10:33:43 <br/>
 * 
 * @author thanos
 * @version
 * @since JDK 1.6
 * @see
 */
public class RestResponseUtil {

	public static final String module = RestResponseUtil.class.getName();

	public static final Integer HTTP_CODE_0 = 0;
	public static final Integer HTTP_CODE_100 = 100;
	public static final Integer HTTP_CODE_101 = 101;
	public static final Integer HTTP_CODE_200 = 200;
	public static final Integer HTTP_CODE_201 = 201;
	public static final Integer HTTP_CODE_202 = 202;
	public static final Integer HTTP_CODE_203 = 203;
	public static final Integer HTTP_CODE_204 = 204;
	public static final Integer HTTP_CODE_205 = 205;
	public static final Integer HTTP_CODE_206 = 206;
	public static final Integer HTTP_CODE_300 = 300;
	public static final Integer HTTP_CODE_301 = 301;
	public static final Integer HTTP_CODE_302 = 302;
	public static final Integer HTTP_CODE_303 = 303;
	public static final Integer HTTP_CODE_304 = 304;
	public static final Integer HTTP_CODE_305 = 305;
	public static final Integer HTTP_CODE_306 = 306;// 已经被废弃的HTTP状态码
	public static final Integer HTTP_CODE_307 = 307;
	public static final Integer HTTP_CODE_400 = 400;
	public static final Integer HTTP_CODE_401 = 401;
	public static final Integer HTTP_CODE_402 = 402;
	public static final Integer HTTP_CODE_403 = 403;
	public static final Integer HTTP_CODE_404 = 404;
	public static final Integer HTTP_CODE_405 = 405;
	public static final Integer HTTP_CODE_406 = 406;
	public static final Integer HTTP_CODE_407 = 407;
	public static final Integer HTTP_CODE_408 = 408;
	public static final Integer HTTP_CODE_409 = 409;
	public static final Integer HTTP_CODE_410 = 410;
	public static final Integer HTTP_CODE_411 = 411;
	public static final Integer HTTP_CODE_412 = 412;
	public static final Integer HTTP_CODE_413 = 413;
	public static final Integer HTTP_CODE_414 = 414;
	public static final Integer HTTP_CODE_415 = 415;
	public static final Integer HTTP_CODE_416 = 416;
	public static final Integer HTTP_CODE_417 = 417;
	public static final Integer HTTP_CODE_500 = 500;
	public static final Integer HTTP_CODE_501 = 501;
	public static final Integer HTTP_CODE_502 = 502;
	public static final Integer HTTP_CODE_503 = 503;
	public static final Integer HTTP_CODE_504 = 504;
	public static final Integer HTTP_CODE_505 = 505;

	public static final String INVALIDATE_CODE_PRODUCT_PRICE_ISEMPTY = "productPriceIsEmpty";
	public static final String INVALIDATE_CODE_PRODUCT_QUANTITY_ISEMPTY = "productQuantityIsEmpty";
	public static final String INVALIDATE_CODE_PRODUCT_QUANTITY_TOOLARGE = "productQuantityTooLarge";
	public static final String INVALIDATE_CODE_PRODUCTQUANTITYISNEGATIVE = "productQuantityIsNegative";
	public static final String INVALIDATE_CODE_PRODUCT_PRICE_ISNEGATIVE = "productPriceIsNegative";
	public static final String INVALIDATE_CODE_SHIPPINGMETHOD_ISEMPTY = "shippingMethodIsEmpty";
	public static final String INVALIDATE_CODE_ORDERFROM_ISEMPTY = "orderFromIsEmpty";
	public static final String INVALIDATE_CODE_USERLOGINID_ISEMPTY = "userLoginIdIsEmpty";
	public static final String INVALIDATE_CODE_PRODUCTSTORECTX_ISEMPTY = "productStoreCtxIsEmpty";
	public static final String INVALIDATE_CODE_PRODUCTITEM_ISEMPTY = "productItemIsEmpty";
	public static final String INVALIDATE_CODE_PAYMENTMETHODTYPEID_ISEMPTY = "paymentMethodTypeIdIsEmpty";
	public static final String INVALIDATE_CODE_SHIPPINGCONTACTMECHID_ISEMPTY = "shippingContactMechIdIsEmpty";
	public static final String INVALIDATE_CODE_FORMAT = "shippingContactMechIdIsEmpty";

	public static final String FAILURE_CODE_addToCartFailure = "addToCartFailure";

	public static final String ERROR_CODE_CART_ISEMPTY = "cartIsEmpty";
	public static final String ERROR_CODE_SERVICEERROR = "serviceError";
	public static final String ERROR_CODE_OTHERERROR = "otherError";

	/**
	 * ObjectMapper是JSON操作的核心，Jackson的所有JSON操作都是在ObjectMapper中实现。
	 * ObjectMapper有多个JSON序列化的方法，可以把JSON字符串保存File、OutputStream等不同的介质中。
	 * writeValue(File arg0, Object arg1)把arg1转成json序列，并保存到arg0文件中。
	 * writeValue(OutputStream arg0, Object arg1)把arg1转成json序列，并保存到arg0输出流中。
	 * writeValueAsBytes(Object arg0)把arg0转成json序列，并把结果输出成字节数组。
	 * writeValueAsString(Object arg0)把arg0转成json序列，并把结果输出成字符串。
	 */
	ObjectMapper mapper = new ObjectMapper();

	public static String getJsonResult(String enum_key) {
		String enum_value = ResponseCodeEnum.getValue(enum_key);
		return "{\"" + enum_key + "\":\"" + enum_value + "\"}";
	}

	public static String getJsonResult(Integer enum_key) {
		String emString = String.valueOf(enum_key);
		String enum_value = ResponseCodeEnum.getValue(emString);
		return "{\"" + enum_key + "\":\"" + enum_value + "\"}";
	}

	public static Response getResponse(Integer enum_key) {
		Map<Object, Object> responseMap = Maps.newHashMap();
		String emString = String.valueOf(enum_key);
		String enum_value = ResponseCodeEnum.getValue(emString);
		responseMap.put(emString, enum_value);
		JSON json = null;
		try {
			json = JSON.from(responseMap);
		} catch (IOException e) {
			Debug.logError(e, "转换成JSON格式时出错", module);
		}
		if (json != null) {
			return Response.ok(json.toString(), MediaType.APPLICATION_JSON_TYPE)
					.status(enum_key)
					.build();
		} else {
			return getResponse(HTTP_CODE_500);
		}

	}

	public static Response getResponse(String enum_key) {
		if (UtilValidate.isEmpty(enum_key)) {
			return getResponse(ERROR_CODE_OTHERERROR);
		}

		String enum_value = ResponseCodeEnum.getValue(enum_key);
		if (UtilValidate.isEmpty(enum_value)) {
			enum_value = "未知错误";
		}
		Map<Object, Object> responseMap = Maps.newHashMap();
		String emString = String.valueOf(enum_key);
		responseMap.put(emString, enum_value);
		JSON json = null;
		try {
			json = JSON.from(responseMap);
		} catch (IOException e) {
			Debug.logError(e, "转换成JSON格式时出错", module);
		}
		if (json == null) {
			return getResponse(HTTP_CODE_500);
		} else if (enum_key.startsWith("INVALIDATE")) {
			return Response.ok(json.toString(), MediaType.APPLICATION_JSON_TYPE)
					.status(RestResponseUtil.HTTP_CODE_417)
					.build();
		} else if (enum_key.startsWith("ERROR")) {
			return Response.ok(json.toString(), MediaType.APPLICATION_JSON_TYPE)
					.status(RestResponseUtil.HTTP_CODE_503)
					.build();
		} else if (enum_key.startsWith("FAILURE")) {
			return Response.ok(json.toString(), MediaType.APPLICATION_JSON_TYPE)
					.status(RestResponseUtil.HTTP_CODE_400)
					.build();
		} else {
			return getResponse(HTTP_CODE_500);
		}
	}

}