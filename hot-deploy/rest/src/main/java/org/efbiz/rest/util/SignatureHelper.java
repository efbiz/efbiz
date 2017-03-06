package org.efbiz.rest.util;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.DelegatorFactory;
import org.apache.ofbiz.entity.GenericDelegator;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceDispatcher;

public class SignatureHelper {

	public static final String module = SignatureHelper.class.getName();
	public static GenericDelegator delegator = (GenericDelegator) DelegatorFactory.getDelegator("default");
	public static LocalDispatcher dispatcher = ServiceDispatcher.getInstance(delegator).getLocalDispatcher("default");
	public static final String APPID_HEADER = "APPID";
	public static final String TIMESTAMP_HEADER = "TIMESTAMP";
	public static final String SIGNTYPE_HEADER = "SIGNTYPE";
	public static final String SIGNATURE_HEADER = "SIGNATURE";
	public static final List<String> SIGNATURE_KEYWORDS = Arrays.asList(APPID_HEADER, TIMESTAMP_HEADER,SIGNTYPE_HEADER);

	private static final String DSA = "DSA";
	private static final String MD5 = "MD5";

	public static String getPublicKey(String appId) {
		if(UtilValidate.isNotEmpty(appId)){
			try {
				GenericValue app = delegator.findOne("AppApiAuth", false,UtilMisc.toMap("appid", appId) );
				if(UtilValidate.isNotEmpty(app)){
					return app.getString("appPublicKey");
				}
			} catch (Exception e) {
				Debug.logWarning(e, module);
			}
		} 
		return null;
	}

	public static String createSignature(HttpServletRequest request, String url, String privateKey) throws Exception {

		TreeMap<String, String> sortedHeaders = new TreeMap<String, String>();
		// load header values we care about
		Enumeration e = request.getHeaderNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (SIGNATURE_KEYWORDS.contains(key)) {
				sortedHeaders.put(key, request.getHeader(key));
			}
		}
		String sortedUrl = createSortedUrl(url, sortedHeaders);

		KeyFactory keyFactory = KeyFactory.getInstance(DSA);
		byte[] privateKeyBytes = Base64.decodeBase64(privateKey);
		EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

		Signature sig = Signature.getInstance(DSA);
		sig.initSign(keyFactory.generatePrivate(privateKeySpec));
		sig.update(sortedUrl.getBytes());

		return Base64.encodeBase64URLSafeString(sig.sign());
	}

	private static PublicKey decodePublicKey(String publicKey) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance(DSA);
		byte[] publicKeyBytes = Base64.decodeBase64(publicKey);
		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
		return keyFactory.generatePublic(publicKeySpec);
	}

	public static boolean validateSignature(String url, String signatureString, String appId, String signType) throws InvalidKeyException, Exception {
		if (UtilValidate.isEmpty(signType)) {
			return false;
		} else if (signType.equalsIgnoreCase(MD5)) {
			String publicKey = SignatureHelper.getPublicKey(appId);
			String mysign = MD5Util.sign(url, publicKey, "utf-8");
			return signatureString.equals(mysign);
		} else if (signType.equalsIgnoreCase(DSA)) {
			
			String publicKey = SignatureHelper.getPublicKey(appId);
			if (publicKey == null)
				return false;
			Signature signature = Signature.getInstance(DSA);
			signature.initVerify(decodePublicKey(publicKey));
			signature.update(url.getBytes());
			try {
				return signature.verify(Base64.decodeBase64(signatureString));
			} catch (SignatureException e) {
				Debug.logWarning(e, module);
				return false;
			}
		}
		return false;
	}

	public static String createSortedUrl(HttpServletRequest request) {

		// use a TreeMap to sort the headers and parameters
		TreeMap<String, String> headersAndParams = getSortedParamsMap(request);

		return createSortedUrl(request.getContextPath() + request.getServletPath() + request.getPathInfo(), headersAndParams);
	}
	
	public static TreeMap<String, String> getSortedParamsMap(HttpServletRequest request) {
		// use a TreeMap to sort the headers and parameters
		TreeMap<String, String> headersAndParams = new TreeMap<String, String>();
		
		// load header values we care about
		Enumeration e = request.getHeaderNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (SIGNATURE_KEYWORDS.contains(key.toUpperCase())) {
				headersAndParams.put(key, request.getHeader(key));
			}
		}
		
		// load parameters
		for (Object key : request.getParameterMap().keySet()) {
			String[] o = (String[]) request.getParameterMap().get(key);
			headersAndParams.put((String) key, o[0]);
		}
		return headersAndParams;
	}
	public static TreeMap<String, String> getSortedHeaderParamsMap(HttpServletRequest request) {
		// use a TreeMap to sort the headers and parameters
		TreeMap<String, String> headersAndParams = new TreeMap<String, String>();
		// load header values we care about
		Enumeration e = request.getHeaderNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			if (SIGNATURE_KEYWORDS.contains(key.toUpperCase())) {
				headersAndParams.put(key, request.getHeader(key));
			}
		}
		return headersAndParams;
	}
	
	
	public static String createSortedParams(HttpServletRequest request) {
		
		// use a TreeMap to sort the headers and parameters
		TreeMap<String, String> headersAndParams = getSortedHeaderParamsMap(request);
		
		return createSortedParams(headersAndParams);
	}

	public static String createSortedUrl(String url, TreeMap<String, String> headersAndParams) {
		// build the url with headers and parms sorted
		String params = "";
		for (String key : headersAndParams.keySet()) {
			if (params.length() > 0) {
				params += "@";
			}
			params += key + "=" + headersAndParams.get(key).toString();
		}
		if (!url.endsWith("?"))
			url += "?";
		return url + params;
	}
	
	public static String createSortedParams(TreeMap<String, String> headersAndParams) {
		// build the url with headers and parms sorted
		String params = "";
		for (String key : headersAndParams.keySet()) {
			if (params.length() > 0) {
				params += "@";
			}
			params += key.toLowerCase() + "=" + headersAndParams.get(key).toString();
		}
		return  params;
	}

	public static void main(String[] args) throws Exception {

		// Generate a 1024-bit Digital Signature Algorithm (DSA) key pair
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(DSA);
		keyGen.initialize(1024);
		KeyPair keypair = keyGen.genKeyPair();
		PrivateKey privateKey = keypair.getPrivate();
		PublicKey publicKey = keypair.getPublic();

		// Get the bytes of the public and private keys (these go in the database with API Key)
		byte[] privateKeyEncoded = privateKey.getEncoded();
		byte[] publicKeyEncoded = publicKey.getEncoded();
		System.out.println("Private Key: " + Base64.encodeBase64URLSafeString(privateKeyEncoded));
		System.out.println("Public Key: " + Base64.encodeBase64URLSafeString(publicKeyEncoded));

	}

}
