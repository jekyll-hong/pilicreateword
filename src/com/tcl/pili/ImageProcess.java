package com.tcl.pili;

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
		
		return dst;
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
}