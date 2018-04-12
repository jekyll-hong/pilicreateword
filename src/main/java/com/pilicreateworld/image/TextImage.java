package com.pilicreateworld.image;

import com.pilicreateworld.ocr.OcrServiceFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

public class TextImage {
    private static final int BINARY_THRESHOLD = 240;
    private static final int MIN_PARAGRAPH_SPACING = 15;

    private BufferedImage mImage = null;

    public TextImage() {
        //nothing
    }

    public TextImage(InputStream in) throws IOException {
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

    public void append(TextImage textImage) {
        if (mImage == null) {
            mImage = textImage.mImage;
        }
        else {
            mImage = ImageProcess.stitch(mImage, textImage.mImage);
        }
    }

    public void deleteTitle() {
        mImage = mImage.getSubimage(0, 30, mImage.getWidth(), mImage.getHeight() - 30);
    }

    private class Paragraph {
        int start;
        int end;

        int getHeight() {
            return end - start + 1;
        }
    }

    public String getText() throws IOException {
        StringBuffer buffer = new StringBuffer();

        for (Paragraph paragraph : findParagraph()) {
            BufferedImage paragraphImage = mImage.getSubimage(
                    0, paragraph.start, mImage.getWidth(), paragraph.getHeight());

            String text = OcrServiceFactory.getService().process(paragraphImage);
            if (!text.isEmpty()) {
                buffer.append(text);
                buffer.append('\n');
            }
        }

        return buffer.toString();
    }

    private class Row {
        int y;
        int height;
    }

    private List<Paragraph> findParagraph() {
        List<Paragraph> paragraphList = new LinkedList<>();

        List<Row> rowList = findRows();
        if (!rowList.isEmpty()) {
            Row row = rowList.remove(0);

            /**
             * 段落起始
             */
            Paragraph paragraph = new Paragraph();
            paragraph.start = row.y;
            paragraph.end = row.y + row.height;

            Row prevRow = row;
            while (!rowList.isEmpty()) {
                row = rowList.remove(0);

                if (row.y - (prevRow.y + prevRow.height) < MIN_PARAGRAPH_SPACING) {
                    /**
                     * 属于当前段落
                     */
                    paragraph.end = row.y + row.height;
                }
                else {
                    /**
                     * 是新的段落
                     */
                    paragraphList.add(paragraph);

                    paragraph = new Paragraph();
                    paragraph.start = row.y;
                    paragraph.end = row.y + row.height;
                }

                prevRow = row;
            }
        }

        return paragraphList;
    }

    private List<Row> findRows() {
        List<Row> rowList = new LinkedList<>();

        Row row = null;
        int prevCount = 0;
        int boundary = 0;

        int yOffset = 0;
        do {
            int count = getBlackPixels(yOffset, BINARY_THRESHOLD);
            if (count > 0) {
                /**
                 * word
                 */
                if (row == null) {
                    /**
                     * upper boundary
                     */
                    row = new Row();
                    row.y = yOffset;

                    boundary = row.y;
                }
                else {
                    /**
                     * 如果相邻两行的边界不明显，则根据变化趋势来判定是否达到边界
                     */
                    if ((calculateMagnitude(prevCount) - calculateMagnitude(count) > 1)
                            && (boundary == row.y)) {
                        /**
                         * 大幅下降，临近边界
                         */
                        boundary = yOffset;
                    }
                    else if ((boundary > row.y) && (prevCount >= count)) {
                        /**
                         * 临近边界时，要最小值
                         */
                        boundary = yOffset;
                    }
                    else if ((calculateMagnitude(count) - calculateMagnitude(prevCount) > 1)
                            && (boundary > row.y)) {
                        /**
                         * 大幅上升，远离边界
                         */
                        row.height = boundary - row.y + 1;
                        rowList.add(row);

                        row = new Row();
                        row.y = boundary + 1;

                        boundary = row.y;
                    }
                }

                prevCount = count;
            }
            else {
                /**
                 * background
                 */
                if (row != null) {
                    /**
                     * lower boundary
                     */
                    row.height = yOffset - row.y;

                    rowList.add(row);
                    row = null;
                }
            }

            yOffset++;
        }
        while (yOffset < mImage.getHeight());

        return rowList;
    }

    /**
     * 数量级
     */
    private static int calculateMagnitude(int number) {
        int n = 0;

        while (number / 10 > 0) {
            number = number / 10;
            n++;
        }

        return n;
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
