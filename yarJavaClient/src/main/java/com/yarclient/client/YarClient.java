package com.yarclient.client;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.msgpack.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.yarclient.config.ClientConfig;
import com.yarclient.httpclient.YarHttpClient;
import com.yarclient.util.HttpUtils;
import com.yarclient.util.YarUtil;

public class YarClient {
	public static final Logger logger = LoggerFactory.getLogger(YarClient.class);

	private Random ranDom = new Random(1000);

	/** client请求
	 * 
	 * @param clazz
	 *            返回类的class
	 * @param url
	 * @param method
	 * @param param
	 *            入参
	 * @param template
	 *            返回参数的模板
	 * @return
	 * @throws Exception */
	public <T> T clientRequest(Class<T> clazz, String url, String method, Object param, Template template)
	        throws Exception {
		String jsonStr = this.clientRequestToJson(url, method, param, template);
		if (jsonStr == null || jsonStr.length() == 0) {
			return null;
		}
		return JSON.parseObject(jsonStr, clazz);

	}

	/** map里有obj情况的请求（内部会手动将map里的obj变成map）
	 * 
	 * @param url
	 * @param method
	 * @param param
	 * @param template
	 * @return
	 * @throws Exception */
	public String clientRequestHaveObj(String url, String method, Map param, Template template) throws Exception {
		param = YarUtil.convertMapBean((Map) param);
		String jsonStr = this.clientRequestToJson(url, method, param, template);
		if (jsonStr == null || jsonStr.length() == 0) {
			return null;
		}
		return jsonStr;

	}

	/** client请求
	 * 
	 * @param url
	 * @param method
	 * @param param
	 * @param template
	 *            返回参数的模板
	 * @return
	 * @throws Exception */
	public String clientRequestToJson(String url, String method, Object param, Template template) throws Exception {

		YarHttpClient client = new YarHttpClient();
		int id = ranDom.nextInt();// transaction id 随机数

		StructMy struct = new StructMy();
		struct.setId(id);
		struct.setMagic_number(ClientConfig.magicNumber);
		struct.setPack(ClientConfig.pack);
		struct.setProvider(ClientConfig.provider == null ? null : ClientConfig.provider.toCharArray());
		struct.setToken(ClientConfig.token == null ? null : ClientConfig.token.toCharArray());
		struct.setVersion(ClientConfig.version);
		struct.setReserved(ClientConfig.reserved);

		Map bodyMap = new HashMap();
		bodyMap.put("i", id); // transaction id
		bodyMap.put("m", method);
		if (param instanceof Map) {
			bodyMap.put("p", new Map[] { (Map) param });
		} else if (param instanceof List) {
			bodyMap.put("p", new List[] { (List) param });
		} else if (param instanceof String) {
			bodyMap.put("p", new String[] { (String) param });
		} /*
		 * else if(YarUtil.isObject(param)){ bodyMap.put("p", new Map[] {
		 * YarUtil.convertBeanToMap(param) }); }
		 */

		struct.setBody(bodyMap);
		CloseableHttpResponse response = null;
		try {
			response = client.post(url, struct.transByteArr());
			
			if (response == null) {
				throw new Exception("{message=response is null!}");
			}
			
			String content = null;
			InputStream inputStrean = response.getEntity().getContent();
			if (inputStrean != null) {
				StructMy returnStructMy = YarUtil.deTransToStructMy(inputStrean, template);
				if(returnStructMy==null){
					logger.error("returnStructMy is null , may be input error ,may be input is null");
					return null;
				}
				if (returnStructMy.getResBody().get("e") != null) {
					String exceptionStr = (((Map) returnStructMy.getResBody()).get("e")).toString();
					if (exceptionStr != null && exceptionStr.length() > 0) {
						throw new Exception(exceptionStr);
					}
				}
				if (returnStructMy.getId() != id) {
					throw new Exception("id error requestId=" + id + " responseId=" + returnStructMy.getId());
				}
				
				// json工具会把null为value的key-value抛弃
				content = JSON.toJSONString(returnStructMy.getResBody().get("r"));
			}
			
			if (response.getStatusLine().getStatusCode() == 200) {
				return content;
			} else {
				throw new Exception("responseCode=" + response.getStatusLine().getStatusCode() + ",message=" + content);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			HttpUtils.close(response);
		}
	}

}
