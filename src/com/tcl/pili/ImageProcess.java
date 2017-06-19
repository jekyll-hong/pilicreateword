package com.tcl.pili;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import ij.process.ByteProcessor;
import ij.process.ImageConverter;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

final class ImageProcess {
	public static BufferedImage merge(BufferedImage[] src) {
		int totalHeight = 0;
		for (int i = 0; i < src.length; i++) {
			totalHeight += src[i].getHeight();
		}
		
		BufferedImage dst = new BufferedImage(src[0].getWidth(), totalHeight, src[0].getType());
		Graphics2D graphics = dst.createGraphics();
		
		int yOffset = 0;
		for (int i = 0; i < src.length; i++) {
			graphics.drawImage(src[i], null, 0, yOffset);
			yOffset += src[i].getHeight();
		}
		
		return dst;
	}
	
	public static BufferedImage convert(BufferedImage src) {
		ImagePlus img = new ImagePlus();
		img.setImage(src);
		
		ImageConverter convertor = new ImageConverter(img);
		convertor.convertToGray8();
		
		return img.getBufferedImage();
	}
	
	public static BufferedImage sharpen(BufferedImage src) {
		ByteProcessor processor = new ByteProcessor(src);
		processor.sharpen();
		
		return src;
	}
	
	public static BufferedImage enhanceContrast(BufferedImage src) {
		ImagePlus img = new ImagePlus();
		img.setImage(src);
		
		ContrastEnhancer enhancer = new ContrastEnhancer();
		enhancer.stretchHistogram(img, 15.0f);
		
		return img.getBufferedImage();
	}
}