package com.server.httpclient;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.server.util.HttpTools;

public class YarHttpClient {
	
	private static Logger logger = LogManager.getLogger(YarHttpClient.class);
	
	/** post请求 */
	public static HttpResponse post(String sUrl, byte byteArr[]) throws ClientProtocolException, IOException {

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
		HttpResponse response = HttpTools.post(sUrl, entity).getResponse();
		logger.info(" time:"+ (System.currentTimeMillis() - beginTime) + "ms");
//		HttpResponse response = HttpUtils.post(sUrl, entity).getResponse();

		return response;

		// InputStream inputStream = new ByteArrayInputStream(byteArr);
		// InputStreamEntity entity = new InputStreamEntity(inputStream,
		// byteArr.length);
		// HttpResponse response = HttpTools.post(sUrl, entity).getResponse();
		// inputStream.close();
		// return response;

	}

}
