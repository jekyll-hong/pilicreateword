package com.pilicreateworld.image;

import ij.plugin.ContrastEnhancer;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;

import java.awt.*;
import java.awt.image.BufferedImage;

class ImageProcess {
    public static BufferedImage convert(BufferedImage src) {
        BufferedImage dst = new BufferedImage(src.getWidth(),
                src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D graphics = dst.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(src, 0, 0, null);
        graphics.dispose();

        return dst;
    }

    public static BufferedImage stitch(BufferedImage src1, BufferedImage src2) {
        BufferedImage dst = new BufferedImage(src1.getWidth(),
                src1.getHeight() + src2.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D graphics = dst.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(src1, 0, 0, null);
        graphics.drawImage(src2, 0, src1.getHeight(), null);
        graphics.dispose();

        return dst;
    }

    public static BufferedImage sharpen(BufferedImage src) {
        ColorProcessor processor = new ColorProcessor(src);

        processor.sharpen();

        return processor.getBufferedImage();
    }

    public static BufferedImage enhanceContrast(BufferedImage src, double saturated) {
        ByteProcessor processor = new ByteProcessor(src);

        ContrastEnhancer enhancer = new ContrastEnhancer();
        enhancer.stretchHistogram(processor, saturated);

        return processor.getBufferedImage();
    }
}
