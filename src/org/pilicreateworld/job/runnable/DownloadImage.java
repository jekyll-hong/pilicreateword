package org.pilicreateworld.job.runnable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.pilicreateworld.httpclient.SimpleHttpClient;

public class DownloadImage implements Runnable {
	private String url;
	private File file;
	private OnImageDownloadListener listener;
	
	public DownloadImage(String url, File file, OnImageDownloadListener listener) {
		this.url = url;
		this.file = file;
		this.listener = listener;
	}
	
	public void run() {
		InputStream in = null;
		OutputStream out = null;
		
		try {
			SimpleHttpClient module = new SimpleHttpClient();
			in = module.connect(url);
			out = new FileOutputStream(file);
			
			writeData(in, out);
			listener.onImageDownload(file);
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
		
		if (out != null) {
			try {
				out.close();
			}
			catch (IOException e) {
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