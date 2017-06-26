package org.pilicreateworld.job.runnable;

import java.io.IOException;
import java.io.InputStream;

import org.jsoup.Jsoup;
import org.pilicreateworld.httpclient.SimpleHttpClient;

public class LoadPage implements Runnable {
	private String url;
	private OnPageLoadListener listener;
	
	public LoadPage(String url, OnPageLoadListener listener) {
		this.url = url;
		this.listener = listener;
	}
	
	public void run() {
		InputStream in = null;
		
		try {
			SimpleHttpClient module = new SimpleHttpClient();
			in = module.connect(url);
			
			listener.onPageLoad(Jsoup.parse(in, null, url));
		}
		catch (IOException e) {
			listener.onError();
		}
		
		if (in != null) {
			try {
				in.close();
			}
			catch (IOException e) {
			}
		}
	}
}