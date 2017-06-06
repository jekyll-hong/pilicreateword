package com.tcl.pili;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

final class PageLoader extends MessageHandlerImpl {
	public PageLoader(Executor executor) {
		super(executor);
	}
	
	public interface OnPageLoadListener {
		public void onPageLoad(Document doc);
		public void onError();
	}
	
	static public class Parameter {
		public String url;
		public String baseUri;
		public OnPageLoadListener listener;
		
		public Parameter(String url, String baseUri, OnPageLoadListener listener) {
			this.url = url;
			this.baseUri = baseUri;
			this.listener = listener;
		}
	}
	
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case Message.MSG_LOAD_WEBPAGE: {
				Parameter param = (Parameter)msg.obj;
				execute(new LoadWebPage(param));
				return true;
			}
			default: {
				return false;
			}
		}	
	}
	
	private class LoadWebPage implements Runnable {
		private Parameter param;
		
		public LoadWebPage(Parameter param) {
			this.param = param;
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
					in = client.connect(param.url);
					doc = Jsoup.parse(in, null, param.baseUri);
				}
				catch (IOException e) {
					System.err.print("load " + param.url + " fail, retry!\r\n");
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
				param.listener.onError();
			}
			else {
				param.listener.onPageLoad(doc);
			}
		}
	}
}