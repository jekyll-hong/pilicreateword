package org.pilicreateworld.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SimpleHttpClient {
	private OkHttpClient client;
	
	public SimpleHttpClient() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(5, TimeUnit.SECONDS);
		builder.readTimeout(5, TimeUnit.SECONDS);
		
		Proxy httpProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 37689));
		builder.proxy(httpProxy);
		
		client = builder.build();
	}
	
	public InputStream connect(String url) throws IOException {
		Call call = client.newCall(makeRequest(url));
		if (call == null) {
			return null;
		}
		
		Response response = call.execute();
		if (response == null) {
			return null;
		}
		
		byte[] content = response.body().bytes();
		return new ByteArrayInputStream(content);
	}
	
	private Request makeRequest(String url) {
		Request.Builder builder = new Request.Builder();
		builder.url(url);
		
		return builder.build();
	}
}