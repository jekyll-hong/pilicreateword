package com.tcl.pili;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

final class ImageDownloader extends MessageHandlerImpl {
	public ImageDownloader(Executor executor) {
		super(executor);
	}
	
	public interface OnImageLoadedListener {
		public void onImageDownload();
		public void onError();
	}
	
	static class Parameter {
		public String url;
		public File file;
		public OnImageLoadedListener listener;
		
		public Parameter(String url, File file, OnImageLoadedListener listener) {
			this.url = url;
			this.file = file;
			this.listener = listener;
		}
	}
	
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case Message.MSG_DOWNLOAD_IMAGE: {
				Parameter param = (Parameter)msg.obj;
				execute(new DownloadImage(param));
				return true;
			}
			default: {
				return false;
			}
		}
	}
	
	private class DownloadImage implements Runnable {
		private Parameter param;
		
		public DownloadImage(Parameter param) {
			this.param = param;
		}
		
		public void run() {
			HTTPClient client = new HTTPClient();
			boolean isFailed;
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
				FileOutputStream out = null;
				
				try {
					in = client.connect(param.url);
					out = new FileOutputStream(param.file);
					writeData(in, out);
					isFailed = false;
				}
				catch (IOException e) {
					System.err.print("download " + param.url + " fail, retry!\r\n");
					isFailed = true;
				}
				finally {
					if (out != null) {
						try {
							out.close();
						}
						catch (IOException e) {
						}
					}
					
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
			while (isFailed && (++retryCnt < 10));
			
			if (isFailed) {
				param.listener.onError();
			}
			else {
				param.listener.onImageDownload();
			}
		}
	}
	
	private void writeData(InputStream in, OutputStream out) throws IOException {
		byte[] buf = new byte[1024];
		
		while (true) {
			int ret = in.read(buf);
			if (ret < 0) {
				break;
			}
			
			out.write(buf, 0, ret);
		}
	}
}