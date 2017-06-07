package com.tcl.pili;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

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
		
		return dst.getSubimage(0, 30, dst.getWidth(), dst.getHeight() - 31);
	}
	
	public static BufferedImage grayScale(BufferedImage src) {
		BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		dst.createGraphics().drawImage(src, null, 0, 0);
		
		return dst;
	}
	
	public static BufferedImage sharpen(BufferedImage src) {
		float[] elements = {-1.0f, -1.0f, -1.0f,
                            -1.0f,  9.0f, -1.0f,
                            -1.0f, -1.0f, -1.0f};
		BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, elements), ConvolveOp.EDGE_NO_OP, null);
		
		return op.filter(src, null);
	}
	
	public static BufferedImage binarization(BufferedImage src) {
		int threshold = getThreshold(getHistogram(src));
		
		for (int i = 0; i < src.getHeight(); i++) {
			for (int j = 0; j < src.getWidth(); j++) {
				int gray = src.getRGB(j, i) & 0xff;
				
				if (gray < threshold) {
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
	
	private static int getThreshold(int[] histogram) {
		return 200;
	}
}
