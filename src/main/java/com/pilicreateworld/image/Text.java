package com.pilicreateworld.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class Text {
    private static final int BINARY_THRESHOLD = 240;
    private static final int MIN_PARAGRAPH_SPACING = 15;

    private BufferedImage mImage = null;

    public Text() {
        //nothing
    }

    public Text(InputStream in) throws IOException {
        mImage = ImageIO.read(in);
        if (mImage == null) {
            throw new IllegalStateException("read image fail!");
        }

        /**
         * 转换为灰度图像并调整对比度
         */
        mImage = ImageProcess.convert(mImage);
        mImage = ImageProcess.enhanceContrast(mImage, 2.0f);
    }

    public void append(Text text) {
        if (mImage == null) {
            mImage = text.mImage;
        }
        else {
            mImage = ImageProcess.stitch(mImage, text.mImage);
        }
    }

    public void deleteTitle() {
        mImage = mImage.getSubimage(0, 30, mImage.getWidth(), mImage.getHeight() - 30);
    }

    public List<Paragraph> getParagraph() {
        List<Paragraph> paragraphList = new LinkedList<Paragraph>();

        /**
         * 连续的空行视为段落间隔
         */
        int pos = -1;
        int blankLines = 0;

        int yOffset = 0;
        do {
            int cnt = getBlackPixels(yOffset, BINARY_THRESHOLD);
            if (cnt > 0) {
                if (pos > 0 && bankLines > MIN_PARAGRAPH_SPACING) {
                    paragraphList.add(new Paragraph(
                        mImage.getSubimage(0, pos, mImage.getWidth(), yOffset - bankLines)));

                    pos = -1;
                }

                blankLines = 0;

                if (pos < 0) {
                    pos = yOffset - 1;
                }
            }
            else {
                blankLines++;
            }

            yOffset++;
        }
        while (yOffset < mImage.getHeight());

        return paragraphList;
    }

    private int getBlackPixels(int y, int binaryThreshold) {
        int count = 0;

        for (int i = 0; i < mImage.getWidth(); i++) {
            /**
             * 灰度图像的R、G、B三个通道的颜色值都是同一灰度值
             */
            int gray = mImage.getRGB(i, y) & 0xff;
            if (gray < binaryThreshold) {
                /**
                 * 二值化后对应像素是0
                 */
                count++;
            }
        }

        return count;
    }
}
