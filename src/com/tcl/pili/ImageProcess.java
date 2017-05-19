package com.tcl.pili;

final class ImageProcess {
	public static BufferedImage crop(BufferedImage src, int top, int bottom) {
		return src.getSubimage(0, top, src.getWidth(), src.getHeight() - top - bottom);
	}

	public static BufferedImage gray(BufferedImage src) {
		BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		dst.createGraphics().drawImage(src, null, 0, 0);
		return dst;
	}

	public static BufferedImage enhance(BufferedImage src) {
		BufferedImage dst = sharpen(src);

		int[] histogram = getHistogram(dst); //双峰
	}

	private static BufferedImage sharpen(BufferedImage src) {
		float[] elements = {-1.0f, -1.0f, -1.0f,
                                    -1.0f,  9.0f, -1.0f,
                                    -1.0f, -1.0f, -1.0f};
		Kernel kernel = new Kernel(3, 3, elements);

		BufferedImageOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		return op.filter(src, null);
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

	//TODO
	private static int analyseHistogram(int[] histogram) {
		int[] threshold = new int[2];

		return threshold;
	}
}
