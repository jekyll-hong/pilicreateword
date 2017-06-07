package com.tcl.pili;

import java.io.IOException;
import java.io.InputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

final class LoadPage implements Runnable {
	private String url;
	private String baseUri;
	private OnPageLoadListener listener;
	
	public LoadPage(String url, String baseUri, OnPageLoadListener listener) {
		this.url = url;
		this.baseUri = baseUri;
		this.listener = listener;
	}
	
	public void run() {
		HTTPClient client = new HTTPClient();
		Document doc = null;
		int retryCnt = 0;
		
		do {
			if (retryCnt > 0) {
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException e) {
				}
			}
			
			InputStream in = null;
			
			try {
				in = client.connect(url);
				doc = Jsoup.parse(in, null, baseUri);
			}
			catch (IOException e) {
				System.err.print("load " + url + " fail, retry!\r\n");
			}
			finally {
				if (in != null) {
					try {
						in.close();
					}
					catch (IOException e) {
					}
				}
				
				client.disconnect();
			}
		}
		while ((doc == null) && (++retryCnt < 10));
		
		if (doc == null) {
			listener.onError();
		}
		else {
			listener.onPageLoad(doc);
		}
	}
}