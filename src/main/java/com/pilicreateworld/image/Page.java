package com.pilicreateworld.image;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Page {
    private BufferedImage mImage;

    public Page(int width, int height) {
        mImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

        Graphics graphics = mImage.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, mImage.getWidth(), mImage.getHeight());
        graphics.dispose();
    }

    public void writeWord(int x, int y, Word word) {
        Graphics graphics = mImage.getGraphics();
        graphics.drawImage(word.getImage(), x, y, null);
        graphics.dispose();
    }

    public BufferedImage getImage() {
        return mImage;
    }
}
