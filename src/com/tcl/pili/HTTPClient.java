package com.tcl.pili;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

final class HTTPClient {
	private static final boolean sUseProxy = true;
	
	private HttpURLConnection mConnection;
	
	public HTTPClient() {
		mConnection = null;
	}
	
	public InputStream connect(String url) throws IOException {
		URL httpURL = new URL(url);
		
		if (!sUseProxy) {
			mConnection = (HttpURLConnection)httpURL.openConnection();
		}
		else {
			Proxy httpProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 37689));
			mConnection = (HttpURLConnection)httpURL.openConnection(httpProxy);
		}
		
		mConnection.setConnectTimeout(2000);
		mConnection.connect();
		
		int status = mConnection.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			if (status == HttpURLConnection.HTTP_MOVED_PERM
				|| status == HttpURLConnection.HTTP_MOVED_TEMP
				|| status == HttpURLConnection.HTTP_SEE_OTHER) {
				String redirectURL = mConnection.getHeaderField("Location");
				System.out.print("redirect to " + redirectURL + "\r\n");
				
				disconnect();
				return connect(redirectURL);
			}
			else if (status == -1) {
				System.err.print("connect to " + url + " fail, retry later\r\n");
				
				disconnect();
				return null;
			}
			else {
				System.err.print("connect to " + url + " error, response code is " + status + "\r\n");
				
				disconnect();
				return null;
			}
		}
		else {
			return mConnection.getInputStream();
		}
	}
	
	public void disconnect() {
		if (mConnection != null) {
			mConnection.disconnect();
			mConnection = null;
		}
	}
}
