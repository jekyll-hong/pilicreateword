package org.pilicreateworld.job.runnable;

import java.awt.image.BufferedImage;

import org.pilicreateworld.imageprocess.SimpleImageProcess;

public class ProcessImage implements Runnable {
	private BufferedImage[] images;
	private OnImageProcessListener listener;
	
	public ProcessImage(BufferedImage[] images, OnImageProcessListener listener) {
		this.images = images;
		this.listener = listener;
	}
	
	public void run() {
		SimpleImageProcess module = new SimpleImageProcess();
		
		BufferedImage image = module.stitchImages(images);
		image = module.convertToGray8(image);
		image = module.sharpen(image);
		image = module.enhanceContrast(image);
		image = module.adjustBackgroundBrightness(image);
		image = module.crop(image, 30, 1);
		
		listener.onImageProcess(image);
	}
}