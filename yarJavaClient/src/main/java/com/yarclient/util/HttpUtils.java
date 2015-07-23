package com.yarclient.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.CookieSpecs;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

	private static CloseableHttpClient client;
	private static PoolingHttpClientConnectionManager cm;
	private static Properties prop = new Properties();

	// TODO 未提取配置参数
	static {
		InputStream in = HttpUtils.class.getResourceAsStream("/yarclient.properties");
		try {
	        prop.load(in);
        } catch (IOException e) {
	        e.printStackTrace();
        }
		
		if(Boolean.valueOf(prop.getProperty("httpPool"))){
			logger.info("启用Http连接池");
			Integer maxTotal = Integer.valueOf(prop.getProperty("maxTotal", "500"));
			Integer reqTimeout = Integer.valueOf(prop.getProperty("requestTimeout", "30000"));
			Integer conTimeout = Integer.valueOf(prop.getProperty("connectTimeout", "40000"));
			Integer socketTimeout = Integer.valueOf(prop.getProperty("socketTimeout", "30000"));
			
			cm = new PoolingHttpClientConnectionManager();
			cm.setMaxTotal(maxTotal);
			cm.setDefaultMaxPerRoute(cm.getMaxTotal());
			RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.BROWSER_COMPATIBILITY).setConnectionRequestTimeout(reqTimeout).setConnectTimeout(conTimeout)
					.setSocketTimeout(socketTimeout).build();
			
			client = HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(config).setRetryHandler(new DefaultHttpRequestRetryHandler(5, true) {
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
		}else {
			logger.info("未启用Http连接池");
		}
	}
	
	private static CloseableHttpClient getClient(){
		if(client == null){
			return HttpClients.createDefault();
		}
		cm.closeExpiredConnections();
		cm.closeIdleConnections(30, TimeUnit.MINUTES);
		return client;
	}
	public static void close(CloseableHttpResponse resp){
		if(resp != null && client == null){
			try {
	            resp.close();
            } catch (IOException e) {
	            e.printStackTrace();
            }
		}
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
			CloseableHttpResponse resp = getResponse();
			if(resp == null){
				return null;
			}
			try {
				HttpEntity entity = resp.getEntity();
				return EntityUtils.toString(entity, "utf-8");
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} finally {
				close(resp);
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
				return getClient().execute(post);
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
				return getClient().execute(get);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			return null;
		}
	}

}
