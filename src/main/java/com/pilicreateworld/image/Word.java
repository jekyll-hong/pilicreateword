package com.pilicreateworld.image;

import java.awt.image.BufferedImage;

public class Word {
    private BufferedImage mImage;

    public Word(BufferedImage image) {
        mImage = image;
    }

    public boolean isSpace() {
        int blackPixelCnt = ImageProcess.getBlackPixels(mImage);

        return blackPixelCnt == 0;
    }

    public BufferedImage scale(int width, int height) {
        return ImageProcess.scale(mImage, width, height);
    }
}
