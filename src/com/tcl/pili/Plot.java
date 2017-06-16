package com.tcl.pili;

import java.io.File;

final class Plot implements OnImageDownloadListener {
	private Episode episode;
	private String url;
	
	private File image;
	
	public Plot(Episode episode, String url) {
		this.episode = episode;
		this.url = url;
		
		image = Utils.getChildFile(episode.getDir(), getFileName());
	}
	
	private String getFileName() {
		return url.substring(url.lastIndexOf("/") + 1);
	}
	
	public void maybeDownload() {
		if (!image.exists()) {
			Message msg = new Message(Message.MSG_DOWNLOAD_IMAGE, new DownloadImage(url, image, this));
			MessageLooper.getInstance().post(msg);
		}
		else {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
			}
			
			episode.notifyPlotDone();
		}
	}
	
	public void onImageDownload() {
		System.out.print(url + " is downloaded!\r\n");
		episode.notifyPlotDone();
	}
	
	public void onError() {
		System.err.print("download plot image error!\r\n");
	}
	
	public File getImage() {
		return image;
	}
}