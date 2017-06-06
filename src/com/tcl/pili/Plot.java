package com.tcl.pili;

import java.io.File;

final class Plot implements ImageDownloader.OnImageLoadedListener {
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
			Message msg = new Message(Message.MSG_DOWNLOAD_IMAGE, new ImageDownloader.Parameter(url, image, this));
			MessageLooper.getInstance().post(msg);
		}
		else {
			episode.notify();
		}
	}
	
	public void onImageDownload() {
		episode.notify();
	}
	
	public void onError() {
		System.err.print("download plot image error!\r\n");
	}
}