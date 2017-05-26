package com.tcl.pili;

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
}
