package com.tcl.pili;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

final class ImageProcess {
	public static BufferedImage crop(BufferedImage src, int top, int bottom) {
		return src.getSubimage(0, top, src.getWidth(), src.getHeight() - top - bottom);
	}
	
	public static BufferedImage sharpen(BufferedImage src) {
		float[] elements = {-1.0f, -1.0f, -1.0f,
                                    -1.0f,  9.0f, -1.0f,
                                    -1.0f, -1.0f, -1.0f};
		Kernel kernel = new Kernel(3, 3, elements);

		BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		return op.filter(src, null);
	}
	
	public static BufferedImage binarization(BufferedImage src) {
		int[] histogram = getHistogram(src);
		int threshold = 200; //TODO: calculate threshold by histogram
		
		for (int i = 0; i < src.getHeight(); i++) {
			for (int j = 0; j < src.getWidth(); j++) {
				int gray = src.getRGB(j, i) & 0xff;
				
				if (gray <= threshold) {
					gray = 0;
				}
				else {
					gray = 255;
				}
				
				src.setRGB(j, i, new Color(gray, gray, gray).getRGB());
			}
		}
		
		return src;
	}
	
	private static int[] getHistogram(BufferedImage src) {
		int[] histogram = new int[256];

		for (int y = 0; y < src.getHeight(); y++) {
			for (int x = 0; x < src.getWidth(); x++) {
				int gray = src.getRGB(x, y) & 0xff;
				histogram[gray]++;
			}
		}

		return histogram;
	}
}
