package com.yarclient.httpclient;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yarclient.util.HttpUtils;

public class YarHttpClient {

	public static final Logger logger = LoggerFactory.getLogger(YarHttpClient.class);

	/** post请求 */
	public static CloseableHttpResponse post(String sUrl, byte byteArr[]) throws ClientProtocolException, IOException {

		// DefaultHttpClient client = new DefaultHttpClient();
		// HttpPost post = new HttpPost(sUrl);
		//
		// InputStream inputStream = new ByteArrayInputStream(byteArr);
		// InputStreamEntity entity = new InputStreamEntity(inputStream,
		// byteArr.length);
		// post.setEntity(entity);
		// HttpResponse res = client.execute(post);
		// post.releaseConnection();
		// inputStream.close();
		// return res;

		ByteArrayEntity entity = new ByteArrayEntity(byteArr, ContentType.APPLICATION_FORM_URLENCODED);
		long beginTime = System.currentTimeMillis();
		// HttpResponse response = HttpTools.post(sUrl, entity).getResponse();
		CloseableHttpResponse response = HttpUtils.post(sUrl, entity).getResponse();
		logger.info(" time:" + (System.currentTimeMillis() - beginTime) + "ms");

		return response;

		// InputStream inputStream = new ByteArrayInputStream(byteArr);
		// InputStreamEntity entity = new InputStreamEntity(inputStream,
		// byteArr.length);
		// HttpResponse response = HttpTools.post(sUrl, entity).getResponse();
		// inputStream.close();
		// return response;

	}

}
