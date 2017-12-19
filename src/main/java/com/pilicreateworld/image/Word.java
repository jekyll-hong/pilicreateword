package com.pilicreateworld.image;

import java.awt.image.BufferedImage;

public class Word {
    private BufferedImage mImage;

    public Word(BufferedImage image) {
        mImage = image;
    }

    public boolean isBlank() {
        int blackPixelCnt = ImageProcess.getBlackPixels(mImage);

        return blackPixelCnt == 0;
    }

    public void changeSize(int width, int height) {
        mImage = ImageProcess.scale(mImage, width, height);
        mImage = ImageProcess.sharpen(mImage);
    }

    public BufferedImage getImage() {
        return mImage;
    }
}
