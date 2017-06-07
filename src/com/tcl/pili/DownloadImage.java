package com.tcl.pili;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final class DownloadImage implements Runnable {
	private String url;
	private File file;
	private OnImageDownloadListener listener;
	
	public DownloadImage(String url, File file, OnImageDownloadListener listener) {
		this.url = url;
		this.file = file;
		this.listener = listener;
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
				in = client.connect(url);
				out = new FileOutputStream(file);
				writeData(in, out);
				isFailed = false;
			}
			catch (IOException e) {
				System.err.print("download " + url + " fail, retry!\r\n");
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
			listener.onError();
		}
		else {
			listener.onImageDownload();
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