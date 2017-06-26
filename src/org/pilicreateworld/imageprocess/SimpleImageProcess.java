package org.pilicreateworld.imageprocess;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import ij.process.ByteProcessor;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class SimpleImageProcess {
	public BufferedImage stitchImages(BufferedImage[] src) {
		BufferedImage dst = createFullImage(src);
		
		int yOffset = 0;
		for (int n = 0; n < src.length; n++) {
			BufferedImage part =  src[n];
			
			for (int i = 0; i < part.getHeight(); i++) {
				for (int j = 0; j < part.getWidth(); j++) {
					dst.setRGB(j, yOffset + i, part.getRGB(j, i));
				}
			}
			
			yOffset += part.getHeight();
		}
		
		return dst;
	}
	
	private BufferedImage createFullImage(BufferedImage[] src) {
		int height = 0;
		for (int i = 0; i < src.length; i++) {
			height += src[i].getHeight();
		}
		
		return new BufferedImage(src[0].getWidth(), height, src[0].getType());
	}
	
	public BufferedImage convertToGray8(BufferedImage src) {
		BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		
		for (int i = 0; i < src.getHeight(); i++) {
			for (int j = 0; j < src.getWidth(); j++) {
				int color = src.getRGB(j, i);
				int gray = rgb2gray(color);
				dst.setRGB(j, i, gray);
			}
		}
		
		return dst;
	}
	
	private int rgb2gray(int color) {
		int red = (color >> 16) & 0xff;
		int green = (color >> 8) & 0xff;
		int blue = color & 0xff;
		
		int gray = (int)(0.2989 * (double)red + 0.5870 * (double)green + 0.1140 * (double)blue);
		if (gray < 0) {
			gray = 0;
		}
		else if (gray > 255) {
			gray = 255;
		}
		
		return new Color(gray, gray, gray).getRGB();
	}
	
	public BufferedImage sharpen(BufferedImage src) {
		ByteProcessor processor = new ByteProcessor(src);
		processor.sharpen();
		
		return src;
	}
	
	public BufferedImage enhanceContrast(BufferedImage src) {
		ImagePlus img = new ImagePlus();
		img.setImage(src);
		
		ContrastEnhancer enhancer = new ContrastEnhancer();
		enhancer.stretchHistogram(img, 2.0f);
		
		return img.getBufferedImage();
	}
	
	public BufferedImage adjustBackgroundBrightness(BufferedImage src) {
		final int threshold = 220;
		
		for (int i = 0; i < src.getHeight(); i++) {
			for (int j = 0; j < src.getWidth(); j++) {
				int gray = src.getRGB(j, i) & 0xff;
				if (gray >= threshold) {
					gray = 255;
				}
				
				src.setRGB(j, i, new Color(gray, gray, gray).getRGB());
			}
		}
		
		return src;
	}
	
	public BufferedImage crop(BufferedImage src, int top, int bottom) {
		return src.getSubimage(0, top, src.getWidth(), src.getHeight() - top - bottom);
	}
}