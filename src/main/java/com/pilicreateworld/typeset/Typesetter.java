package com.pilicreateworld.typeset;

import com.pilicreateworld.Settings;
import com.pilicreateworld.image.Page;
import com.pilicreateworld.image.Word;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Typesetter {
    private static final String sDumpDir = "/home/hongyu/share/pilicreateworld/debug/pages";

    private static final int TEXT_INDENT = 2;

    private Parameter mParameter;
    private int mLinesInPage;
    private int mWordsInLine;

    public Typesetter(Parameter parameter) {
        mParameter = parameter;
        mLinesInPage = calculateLinesInPage();
        mWordsInLine = calculateWordsInLine();
    }

    private int calculateLinesInPage() {
        int pageHeight = mParameter.getPageHeight();
        int topMargin = mParameter.getTopMargin();
        int bottomMargin = mParameter.getBottomMargin();
        int wordHeight = mParameter.getWordHeight();
        int lineSpacing = mParameter.getLineSpacing();

        /**
         * topMargin + lines * wordHeight + (lines - 1) * lineSpacing  + bottomMargin = pageHeight
         */
        return (pageHeight - (topMargin + bottomMargin - lineSpacing)) / (wordHeight + lineSpacing);
    }

    private int calculateWordsInLine() {
        int pageWidth = mParameter.getPageWidth();
        int leftMargin = mParameter.getLeftMargin();
        int rightMargin = mParameter.getRightMargin();
        int wordWidth = mParameter.getWordWidth();
        int wordSpacing = mParameter.getWordSpacing();

        /**
         * leftMargin + words * wordWidth + (words - 1) * wordSpacing  + rightMargin = pageWidth
         */
        return (pageWidth - (leftMargin + rightMargin - wordSpacing)) / (wordWidth + wordSpacing);
    }

    public List<Page> typeset(List<Word> wordList) {
        List<Page> pageList = new ArrayList<Page>();

        do {
            Page page = new Page(mParameter.getPageWidth(), mParameter.getPageHeight());

            writePage(page, wordList, pageList.isEmpty());

            if (Settings.getInstance().isDebuggable()) {
                /*
                try {
                    String fileName = String.format("%03d.png", pageList.size());
                    ImageIO.write(page.getImage(), "png", new File(sDumpDir + "/" + fileName));
                }
                catch (IOException e) {
                    //ignore
                }
                */
            }

            pageList.add(page);
        }
        while (!wordList.isEmpty());

        return pageList;
    }

    private void writePage(Page page, List<Word> wordList, boolean isFirstPage) {
        int lineCnt = 0;
        int yOffset = mParameter.getTopMargin();

        boolean isParagraphStart = isFirstPage;
        do {
            /**
             * 上一行是段落结束即说明下一行是段落开始
             */
            isParagraphStart = writeLine(page, yOffset, wordList, isParagraphStart);

            lineCnt += 1;
            yOffset += (mParameter.getWordHeight() + mParameter.getLineSpacing());
        }
        while ((lineCnt < mLinesInPage) && !wordList.isEmpty());
    }

    private boolean writeLine(Page page, int y, List<Word> wordList, boolean isParagraphStart) {
        int wordCnt = 0;
        int xOffset = mParameter.getLeftMargin();

        if (isParagraphStart) {
            /**
             * 已知这一行是段落起始，首行缩进
             */
            wordCnt += TEXT_INDENT;
            xOffset += (mParameter.getWordWidth() + mParameter.getWordSpacing()) * TEXT_INDENT;
        }

        boolean isParagraphEnd = false;
        do {
            Word word = wordList.remove(0);
            if (word.isBlank()) {
                /**
                 * 段落之间的空白
                 */
                if (isParagraphStart && (wordCnt == TEXT_INDENT)) {
                    /**
                     * 什么都不做
                     */
                }
                else if (!isParagraphStart && (wordCnt == 0)) {
                    /**
                     * 发现这一行是段落起始，首行缩进
                     */
                    isParagraphStart = true;

                    wordCnt += TEXT_INDENT;
                    xOffset += (mParameter.getWordWidth() + mParameter.getWordSpacing()) * TEXT_INDENT;
                }
                else {
                    /**
                     * 段落结束，终止这一行
                     */
                    isParagraphEnd = true;
                    break;
                }
            }
            else {
                word.changeSize(mParameter.getWordWidth(), mParameter.getWordHeight());
                page.writeWord(xOffset, y, word);

                wordCnt += 1;
                xOffset += (mParameter.getWordWidth() + mParameter.getWordSpacing());
            }
        }
        while ((wordCnt < mWordsInLine) && !wordList.isEmpty());

        return isParagraphEnd;
    }

    public static class Parameter {
        public static final String KEY_PAGE_WIDTH = "page_width";
        public static final String KEY_PAGE_HEIGHT = "page_height";

        public static final String KEY_TOP_MARGIN = "top_margin";
        public static final String KEY_BOTTOM_MARGIN = "bottom_margin";
        public static final String KEY_LEFT_MARGIN = "left_margin";
        public static final String KEY_RIGHT_MARGIN = "right_margin";

        public static final String KEY_WORD_WIDTH = "word_width";
        public static final String KEY_WORD_HEIGHT = "word_height";

        public static final String KEY_WORD_SPACING = "word_spacing";
        public static final String KEY_LINE_SPACING = "line_spacing";

        private Map<String, Integer> mTable;

        public Parameter() {
            mTable = new HashMap<String, Integer>();
        }

        public void setPageWidth(int width) {
            put(KEY_PAGE_WIDTH, width);
        }

        public void setPageHeight(int height) {
            put(KEY_PAGE_HEIGHT, height);
        }

        public void setTopMargin(int margin) {
            put(KEY_TOP_MARGIN, margin);
        }

        public void setBottomMargin(int margin) {
            put(KEY_BOTTOM_MARGIN, margin);
        }

        public void setLeftMargin(int margin) {
            put(KEY_LEFT_MARGIN, margin);
        }

        public void setRightMargin(int margin) {
            put(KEY_RIGHT_MARGIN, margin);
        }

        public void setWordWidth(int width) {
            put(KEY_WORD_WIDTH, width);
        }

        public void setWordHeight(int height) {
            put(KEY_WORD_HEIGHT, height);
        }

        public void setWordSpacing(int spacing) {
            put(KEY_WORD_SPACING, spacing);
        }

        public void setLineSpacing(int spacing) {
            put(KEY_LINE_SPACING, spacing);
        }

        public int getPageWidth() {
            return get(KEY_PAGE_WIDTH, -1);
        }

        public int getPageHeight() {
            return get(KEY_PAGE_HEIGHT, -1);
        }

        public int getTopMargin() {
            return get(KEY_TOP_MARGIN, -1);
        }

        public int getBottomMargin() {
            return get(KEY_BOTTOM_MARGIN, -1);
        }

        public int getLeftMargin() {
            return get(KEY_LEFT_MARGIN, -1);
        }

        public int getRightMargin() {
            return get(KEY_RIGHT_MARGIN, -1);
        }

        public int getWordWidth() {
            return get(KEY_WORD_WIDTH, -1);
        }

        public int getWordHeight() {
            return get(KEY_WORD_HEIGHT, -1);
        }

        public int getWordSpacing() {
            return get(KEY_WORD_SPACING, -1);
        }

        public int getLineSpacing() {
            return get(KEY_LINE_SPACING, -1);
        }

        public void put(String key, int value) {
            mTable.put(key, value);
        }

        public int get(String key, int defaultValue) {
            int value = defaultValue;

            if (mTable.containsKey(key)) {
                value = mTable.get(key);
            }

            return value;
        }
    }
}
