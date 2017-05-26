package com.tcl.pili;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

final class HTTPClient {
	private Proxy mProxy;
	private HttpURLConnection mConnection;
	
	public HTTPClient() {
		mProxy = null;
		mConnection = null;
	}
	
	public void setProxy(Proxy proxy) {
		mProxy = proxy;
	}
	
	public void connect(String url) throws IOException {
		URL httpURL = new URL(url);
		
		if (mProxy == null) {
			mConnection = (HttpURLConnection)httpURL.openConnection();
		}
		else {
			mConnection = (HttpURLConnection)httpURL.openConnection(mProxy);
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
				connect(redirectURL);
			}
			else if (status == -1) {
				disconnect();
				System.err.print("connect to " + url + " fail, retry later\r\n");
			}
			else {
				disconnect();
				System.err.print("connect to " + url + " error, response code is " + status + "\r\n");
			}
		}
	}
	
	public void disconnect() {
		if (mConnection != null) {
			mConnection.disconnect();
			mConnection = null;
		}
	}
	
	public InputStream getInputStream() throws IOException {
		if (mConnection != null) {
			return mConnection.getInputStream();
		}
		else {
			return null;
		}
	}
}
