package com.tcl.pili;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import ij.process.ByteProcessor;

import java.awt.image.BufferedImage;

final class ImageProcess {
	public static BufferedImage sharpen(BufferedImage src) {
		ByteProcessor processor = new ByteProcessor(src);
		processor.sharpen();
		
		return src;
	}
	
	public static BufferedImage enhanceContrast(BufferedImage src) {
		ImagePlus img = new ImagePlus();
		img.setImage(src);
		
		ContrastEnhancer enhancer = new ContrastEnhancer();
		enhancer.stretchHistogram(img, 2.0f);
		
		return img.getBufferedImage();
	}
}