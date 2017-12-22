package com.pilicreateworld.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class Text {
    private BufferedImage mImage = null;

    private int mIndex = 0;
    private List<Row> mRowList = new LinkedList<Row>();
    private List<Column> mColumnList = new LinkedList<Column>();

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

    public void detectWords() {
        findRows();
        findColumns();
    }

    public Word read() {
        if (mIndex == mRowList.size() * mColumnList.size()) {
            return null;
        }

        Row row = mRowList.get(mIndex / mColumnList.size());
        Column column = mColumnList.get(mIndex % mColumnList.size());
        mIndex++;

        return new Word(mImage.getSubimage(column.x, row.y, column.width, row.height));
    }

    public boolean isEof() {
        return mIndex == mRowList.size() * mColumnList.size();
    }

    private class Row {
        int y;
        int height;
    }

    private class Column {
        int x;
        int width;
    }

    private void findRows() {
        Row row = null;
        int prevCount = 0;
        int boundary = 0;

        int yOffset = 0;
        do {
            int count = ImageProcess.getBlackPixelsInRow(mImage, yOffset);
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
                        mRowList.add(row);

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

                    mRowList.add(row);
                    row = null;
                }
            }

            yOffset++;
        }
        while (yOffset < mImage.getHeight());
    }

    private void findColumns() {
        Column column = null;
        int prevCount = 0;
        int boundary = 0;

        int xOffset = 0;
        do {
            int count = ImageProcess.getBlackPixelsInColumn(mImage, xOffset);
            if (count > 0) {
                /**
                 * word
                 */
                if (column == null) {
                    /**
                     * left boundary
                     */
                    column = new Column();
                    column.x = xOffset;

                    boundary = column.x;
                }
                else {
                    /**
                     * 如果相邻两列的边界不明显，则根据变化趋势来判定是否达到边界
                     */
                    if ((calculateMagnitude(prevCount) - calculateMagnitude(count) > 0)
                            && (boundary == column.x)) {
                        /**
                         * 大幅下降，临近边界
                         */
                        boundary = xOffset;
                    }
                    else if ((boundary > column.x) && (prevCount >= count)) {
                        /**
                         * 临近边界时，要最小值
                         */
                        boundary = xOffset;
                    }
                    else if ((calculateMagnitude(count) - calculateMagnitude(prevCount) > 0)
                            && (boundary > column.x)) {
                        /**
                         * 大幅上升，远离边界
                         */
                        column.width = boundary - column.x + 1;
                        mColumnList.add(column);

                        column = new Column();
                        column.x = boundary + 1;

                        boundary = column.x;
                    }
                }

                prevCount = count;
            }
            else {
                /**
                 * background
                 */
                if (column != null) {
                    /**
                     * right boundary
                     */
                    column.width = xOffset - column.x;

                    mColumnList.add(column);
                    column = null;
                }
            }

            xOffset++;
        }
        while (xOffset < mImage.getWidth());
    }

    private static int calculateMagnitude(int number) {
        int n = 0;

        while (number / 10 > 0) {
            number = number / 10;
            n++;
        }

        return n;
    }
}
