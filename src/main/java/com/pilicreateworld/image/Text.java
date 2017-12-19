package com.pilicreateworld.image;

import com.pilicreateworld.Settings;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class Text {
    private static final String sDumpDir = "/home/hongyu/share/pilicreateworld/debug/words";

    private BufferedImage mImage;

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

    public void deleteTitle() {
        mImage = mImage.getSubimage(0, 30, mImage.getWidth(), mImage.getHeight() - 30);
    }

    public void append(Text text) {
        mImage = ImageProcess.stitch(mImage, text.mImage);
    }

    public List<Word> findWords() {
        List<Word> wordList = new LinkedList<Word>();

        List<Row> rowList = findRows();
        List<Column> columnList = findColumns();
        for (Row row : rowList) {
            for (Column column : columnList) {
                Word word = new Word(mImage.getSubimage(column.x, row.y, column.width, row.height));

                if (Settings.getInstance().isDebuggable()) {
                    /*
                    try {
                        String fileName = String.format("%05d.png", wordList.size());
                        ImageIO.write(word.getImage(), "png", new File(sDumpDir + "/" + fileName));
                    }
                    catch (IOException e) {
                        //ignore
                    }
                    */
                }

                wordList.add(word);
            }
        }

        return wordList;
    }

    private class Row {
        int y;
        int height;
    }

    private class Column {
        int x;
        int width;
    }

    private List<Row> findRows() {
        List<Row> rowList = new LinkedList<Row>();
        Row row = null;

        int yOffset = 0;
        do {
            int count = ImageProcess.getBlackPixelsInRow(mImage, yOffset);
            if (count > 0) {
                //word region

                if (row == null) {
                    //upper boundary
                    row = new Row();
                    row.y = yOffset;
                }
            }
            else {
                //background region

                if (row != null) {
                    //lower boundary
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

    private List<Column> findColumns() {
        List<Column> columnList = new LinkedList<Column>();
        Column column = null;

        int prevCount = 0;
        int boundary = 0;

        int xOffset = 0;
        do {
            int count = ImageProcess.getBlackPixelsInColumn(mImage, xOffset);
            if (count > 0) {
                //word region

                if (column == null) {
                    //left boundary
                    column = new Column();
                    column.x = xOffset;

                    boundary = column.x;
                }
                else {
                    /**
                     * 字间距不明显，检测相邻两列的边界
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
                        columnList.add(column);

                        column = new Column();
                        column.x = boundary + 1;

                        boundary = column.x;
                    }
                }

                prevCount = count;
            }
            else {
                //background region

                if (column != null) {
                    //right boundary
                    column.width = xOffset - column.x;

                    columnList.add(column);
                    column = null;
                }
            }

            xOffset++;
        }
        while (xOffset < mImage.getWidth());

        return columnList;
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
