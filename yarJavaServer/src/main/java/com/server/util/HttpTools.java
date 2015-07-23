package com.server.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class HttpTools {
	
	private static Logger logger = LogManager.getLogger(HttpTools.class); 

	private static CloseableHttpClient client;

	// TODO 未提取配置参数
	static {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
		RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(30000).setConnectTimeout(30000)
		        .setSocketTimeout(30000).build();

		client = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(config)
		        .setRetryHandler(new DefaultHttpRequestRetryHandler(5, true) {
			        @Override
			        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
				        if (executionCount > getRetryCount()) {

					        logger.warn(
					                "HttpClient connection retry limit reached for exception <"
					                        + exception.getMessage() + ">", exception);
					        return false;
				        }
				        if (exception instanceof NoHttpResponseException) {

					        logger.info("HttpClient NoHttpResponseException received at execution count <"
					                + executionCount + ">");
					        return true;
				        }
				        return super.retryRequest(exception, executionCount, context);
			        }
		        }).build();
	}

	public static BuilderGet get(String url) {
		return new BuilderGet(url);
	}

	public static BuilderPost post(String url) {
		return new BuilderPost(url);
	}

	public static BuilderPost post(String url, HttpEntity entity) {
		return new BuilderPost(url, entity);
	}

	private static List<NameValuePair> getParams(Map<String, String> param) {
		if (param == null) return null;

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Iterator<String> iterator = param.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			String value = param.get(key);
			params.add(new BasicNameValuePair(key, value));
		}
		return params;
	}

	private static HttpEntity postParams(Map<String, String> param) {
		List<NameValuePair> params = getParams(param);
		try {
			return param == null ? null : new UrlEncodedFormEntity(params);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static abstract class Builder {
		String url;
		Map<String, String> param = new HashMap<String, String>();
		Map<String, String> header = new HashMap<String, String>();

		public Builder(String url) {
			this.url = url;
		}

		public Builder addParam(String key, String val) {
			this.param.put(key, val);
			return this;
		}

		public Builder addParam(Map<String, String> param) {
			this.param.putAll(param);
			return this;
		}

		public Builder addHeader(String key, String val) {
			this.header.put(key, val);
			return this;
		}

		public Builder addHeader(Map<String, String> param) {
			this.header.putAll(param);
			return this;
		}

		public abstract CloseableHttpResponse getResponse();

		public String getBody() {
			HttpEntity entity = getResponse().getEntity();
			try {
				return EntityUtils.toString(entity, "utf-8");
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			return null;
		}

		protected void setHeader(HttpUriRequest request) {
			Iterator<String> iterator = header.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				request.addHeader(key, header.get(key));
			}
		}
	}

	public static class BuilderPost extends Builder {
		private HttpEntity entity = null;

		public BuilderPost(String url) {
			super(url);
		}

		public BuilderPost(String url, HttpEntity entity) {
			super(url);
			this.entity = entity;
		}

		public CloseableHttpResponse getResponse() {
			HttpPost post = new HttpPost(url);
			if (entity != null) {
				post.setEntity(entity);
			} else {
				post.setEntity(postParams(param));
			}
			setHeader(post);

			try {
				return client.execute(post);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			return null;
		}
	}

	public static class BuilderGet extends Builder {
		public BuilderGet(String url) {
			super(url);
		}

		public CloseableHttpResponse getResponse() {
			try {
				URIBuilder uri = new URIBuilder(url);
				uri.addParameters(getParams(param));
				HttpGet get = new HttpGet(uri.build());
				setHeader(get);
				return client.execute(get);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			return null;
		}
	}

}
