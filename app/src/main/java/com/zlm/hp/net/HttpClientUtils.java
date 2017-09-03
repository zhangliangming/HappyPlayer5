package com.zlm.hp.net;

import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 网络请求处理
 * 
 * @author zhangliangming
 * 
 */
public class HttpClientUtils {

	/**
	 * 设置连接超时时间 
	 */
	public final static int CONNECT_TIMEOUT = 60;
	/**
	 * 设置读取超时时间 
	 */
	public final static int READ_TIMEOUT = 100;
	/**
	 * 设置写的超时时间 
	 */
	public final static int WRITE_TIMEOUT = 60;

	class HttpMethod {

		static final int POST = 0;
		static final int GET = 1;

	}

	/**
	 * 处理http的get请求
	 * 
	 * @param url
	 * @param params
	 *            参数
	 * @return
	 * @throws Exception
	 */
	public static String httpGetRequest(String url, Map<String, Object> params)
			throws Exception {
		return httpRequest(url, null, params, HttpMethod.GET);
	}

	/**
	 * 处理http的get请求
	 * 
	 * @param url
	 * @param headers
	 *            头数据
	 * @param params
	 *            参数
	 * @return
	 * @throws Exception
	 */
	public static String httpGetRequest(String url,
			Map<String, String> headers, Map<String, Object> params)
			throws Exception {
		return httpRequest(url, headers, params, HttpMethod.GET);
	}

	/**
	 * 处理http的post请求
	 * 
	 * @param url
	 * @param params
	 *            参数
	 * @return
	 * @throws Exception
	 */
	public static String httpPostRequest(String url, Map<String, Object> params)
			throws Exception {
		return httpRequest(url, null, params, HttpMethod.POST);
	}

	/**
	 * 处理http的post请求
	 * 
	 * @param url
	 * @param headers
	 *            头数据
	 * @param params
	 *            参数
	 * @return
	 * @throws Exception
	 */
	public static String httpPostRequest(String url,
			Map<String, String> headers, Map<String, Object> params)
			throws Exception {
		return httpRequest(url, headers, params, HttpMethod.POST);
	}

	/**
	 * 处理http请求
	 * 
	 * @param url
	 * @param headers
	 *            头数据
	 * @param params
	 *            参数
	 * @param httpMethod
	 *            请求方式
	 * @return
	 */
	private static String httpRequest(String url, Map<String, String> headers,
			Map<String, Object> params, int httpMethod) throws Exception {
		
		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)// 设置读取超时时间
				.writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)// 设置写的超时时间
				.connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)// 设置连接超时时间
				.build();

		//
		Request request = null;

		Request.Builder builder = new Request.Builder();
		// 添加头数据
		if (headers != null && !headers.isEmpty()) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				builder.addHeader(header.getKey(), header.getValue());
			}
		}
		if (httpMethod == HttpMethod.POST) {
			FormBody.Builder formBodyBuilder = new FormBody.Builder();
			// 添加参数
			if (params != null && !params.isEmpty()) {

				for (Map.Entry<String, Object> param : params.entrySet()) {
					formBodyBuilder.add(param.getKey(), param.getValue() + "");
				}
			}
			request = builder.post(formBodyBuilder.build()).url(url).build();
		} else if (httpMethod == HttpMethod.GET) {

			StringBuilder tempParams = new StringBuilder();
			// 添加参数
			if (params != null && !params.isEmpty()) {

				boolean flag = false;

				for (Map.Entry<String, Object> param : params.entrySet()) {

					if (!flag) {
						flag = true;
					} else {
						tempParams.append("&");
					}

					// 对参数进行URLEncoder
					tempParams.append(String.format("%s=%s", param.getKey(),
							URLEncoder.encode(param.getValue() + "", "utf-8")));

				}
			}
			String requestUrl = String.format("%s?%s", url,
					tempParams.toString());
			request = builder.get().url(requestUrl).build();
		}

		Call call = client.newCall(request);
		// 执行请求
		Response response = call.execute();
		if (response.isSuccessful()) {
			return response.body().string();
		}
		return null;

	}

}
