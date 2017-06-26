package org.pilicreateworld.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.pilicreateworld.job.JobDispatcher;
import org.pilicreateworld.job.JobType;
import org.pilicreateworld.job.runnable.DownloadImage;
import org.pilicreateworld.job.runnable.OnImageDownloadListener;

class PlotImage implements OnImageDownloadListener {
	private String url;
	private File file;
	private Observer observer;
	
	public PlotImage(String url, File file, Observer observer) {
		this.url = url;
		this.file = file;
		this.observer = observer;
	}
	
	public void download() {
		if (!isCached()) {
			JobDispatcher.getInstance().dispatch(JobType.JOB_DOWNLOAD_IMAGE, new DownloadImage(url, file, this));
		}
		else {
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException e) {
			}
			
			observer.onComplete();
		}
	}
	
	private boolean isCached() {
		return file.exists();
	}
	
	public void onImageDownload(File file) {
		System.out.print("image " + url + "download done!\r\n");
		
		observer.onComplete();
	}
	
	public void onError() {
		System.err.print("download plot image error!\r\n");
	}
	
	public BufferedImage getImage() {
		try {
			return ImageIO.read(file);
		}
		catch (IOException e) {
			return null;
		}
	}
}