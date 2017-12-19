package com.pilicreateworld.image;

import ij.plugin.ContrastEnhancer;
import ij.process.ByteProcessor;

import java.awt.*;
import java.awt.image.BufferedImage;

class ImageProcess {
    private static final int THRESHOLD_BINARY = 240;

    public static BufferedImage convert(BufferedImage src) {
        BufferedImage dst = new BufferedImage(src.getWidth(),
                src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        Graphics graphics = dst.getGraphics();
        graphics.drawImage(src, 0, 0, null);
        graphics.dispose();

        return dst;
    }

    public static BufferedImage stitch(BufferedImage src1, BufferedImage src2) {
        BufferedImage dst = new BufferedImage(src1.getWidth(),
                src1.getHeight() + src2.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        Graphics graphics = dst.getGraphics();
        graphics.drawImage(src1, 0, 0, null);
        graphics.drawImage(src2, 0, src1.getHeight(), null);
        graphics.dispose();

        return dst;
    }

    public static BufferedImage scale(BufferedImage src, int dstWidth, int dstHeight) {
        BufferedImage dst = new BufferedImage(dstWidth, dstHeight, BufferedImage.TYPE_BYTE_GRAY);

        Graphics graphics = dst.getGraphics();
        graphics.drawImage(src, 0, 0, dstWidth, dstHeight, null);
        graphics.dispose();

        return dst;
    }

    public static BufferedImage sharpen(BufferedImage src) {
        ByteProcessor processor = new ByteProcessor(src);

        processor.sharpen();

        return processor.getBufferedImage();
    }

    public static BufferedImage enhanceContrast(BufferedImage src, double saturated) {
        ByteProcessor processor = new ByteProcessor(src);

        ContrastEnhancer enhancer = new ContrastEnhancer();
        enhancer.stretchHistogram(processor, saturated);

        return processor.getBufferedImage();
    }

    public static int getBlackPixelsInRow(BufferedImage img, int y) {
        int count = 0;

        for (int i = 0; i < img.getWidth(); i++) {
            int gray = getGray(img, i, y);
            if (gray < THRESHOLD_BINARY) {
                count++;
            }
        }

        return count;
    }

    public static int getBlackPixelsInColumn(BufferedImage img, int x) {
        int count = 0;

        for (int i = 0; i < img.getHeight(); i++) {
            int gray = getGray(img, x, i);
            if (gray < THRESHOLD_BINARY) {
                count++;
            }
        }

        return count;
    }

    public static int getBlackPixels(BufferedImage img) {
        int count = 0;

        for (int i = 0; i < img.getHeight(); i++) {
            for (int j = 0; j < img.getWidth(); j++) {
                int gray = getGray(img, j, i);
                if (gray < THRESHOLD_BINARY) {
                    count++;
                }
            }
        }

        return count;
    }

    private static int getGray(BufferedImage img, int x, int y) {
        int color = img.getRGB(x, y);

        /**
         * 灰度图像的R、G、B三个通道的颜色值都是同一灰度值
         */
        return color & 0xff;
    }
}
