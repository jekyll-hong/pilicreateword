package com.pilicreateworld.image;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Page {
    private Builder mBuilder;

    private BufferedImage mImage;
    private int mOffsetX;
    private int mOffsetY;

    private boolean mIsParagraphEnd = false;

    private Page(Builder builder) {
        mBuilder = builder;

        init();
    }

    private void init() {
        mImage = new BufferedImage(
                getPageWidth(), getPageHeight(), BufferedImage.TYPE_3BYTE_BGR);

        /**
         * 背景色：白色
         */
        Graphics graphics = mImage.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, mImage.getWidth(), mImage.getHeight());
        graphics.dispose();

        /**
         * 光标位置
         */
        mOffsetX = getLeftMargin();
        mOffsetY = getTopMargin();
    }

    public int getPageWidth() {
        return mBuilder.getPageWidth();
    }

    public int getPageHeight() {
        return mBuilder.getPageHeight();
    }

    public int getTopMargin() {
        return mBuilder.getTopMargin();
    }

    public int getLeftMargin() {
        return mBuilder.getLeftMargin();
    }

    public int getBottomMargin() {
        return mBuilder.getBottomMargin();
    }

    public int getRightMargin() {
        return mBuilder.getRightMargin();
    }

    public int getIndent() {
        return mBuilder.getIndent();
    }

    public int getLineSpacing() {
        return mBuilder.getLineSpacing();
    }

    public int getWordSpacing() {
        return mBuilder.getWordSpacing();
    }

    public int getWordWidth() {
        return mBuilder.getWordWidth();
    }

    public int getWordHeight() {
        return mBuilder.getWordHeight();
    }

    public boolean writeSpace() {
        if (mIsParagraphEnd ||
                mOffsetX + getWordWidth() > getPageWidth() - getRightMargin()) {
            /**
             * 换行
             */
            mOffsetY += (getWordHeight() + getLineSpacing());

            if (mOffsetY + getWordHeight()
                    > getPageHeight() - getBottomMargin()) {
                /**
                 * 写不下新的一行
                 */
                return false;
            }

            mOffsetX = getLeftMargin();

            if (mIsParagraphEnd) {
                /**
                 * 首行缩进
                 */
                mOffsetX += ((getWordWidth() + getWordSpacing()) * getIndent());

                mIsParagraphEnd = false;
            }
        }

        mOffsetX += (getWordWidth() + getWordSpacing());

        return true;
    }

    public boolean write(Word word) {
        if (word.isSpace()) {
            /**
             * 空格
             */
            if (!mIsParagraphEnd) {
                /**
                 * 上一段结束
                 */
                mIsParagraphEnd = true;
            }
        }
        else {
            if (mIsParagraphEnd ||
                    mOffsetX + getWordWidth() > getPageWidth() - getRightMargin()) {
                /**
                 * 换行
                 */
                mOffsetY += (getWordHeight() + getLineSpacing());

                if (mOffsetY + getWordHeight()
                        > getPageHeight() - getBottomMargin()) {
                    /**
                     * 容不下新的一行，写满了
                     */
                    return false;
                }

                mOffsetX = getLeftMargin();

                if (mIsParagraphEnd) {
                    /**
                     * 首行缩进
                     */
                    mOffsetX += ((getWordWidth() + getWordSpacing()) * getIndent());

                    mIsParagraphEnd = false;
                }
            }

            Graphics graphics = mImage.getGraphics();
            graphics.drawImage(
                    word.scale(getWordWidth(), getWordHeight()),
                    mOffsetX, mOffsetY, null);
            graphics.dispose();

            mOffsetX += (getWordWidth() + getWordSpacing());
        }

        return true;
    }

    public boolean isParagraphEnd() {
        return mIsParagraphEnd;
    }

    public ImageData getImageData() throws IOException {
        /**
         * 锐化
         */
        BufferedImage temp = ImageProcess.sharpen(mImage);

        return ImageDataFactory.create(temp, null);
    }

    public static class Builder {
        private int mPageWidth;
        private int mPageHeight;

        private int mTopMargin;
        private int mLeftMargin;
        private int mBottomMargin;
        private int mRightMargin;

        private int mIndent;

        private int mLineSpacing;
        private int mWordSpacing;

        private int mWordWidth;
        private int mWordHeight;

        public Builder() {
            //nothing
        }

        public Builder setPageSize(int width, int height) {
            mPageWidth = width;
            mPageHeight = height;

            return this;
        }

        public Builder setMargins(int top, int left, int bottom, int right) {
            mTopMargin = top;
            mLeftMargin = left;
            mBottomMargin = bottom;
            mRightMargin = right;

            return this;
        }

        public Builder setIndent(int indent) {
            mIndent = indent;

            return this;
        }

        public Builder setLineSpacing(int spacing) {
            mLineSpacing = spacing;

            return this;
        }

        public Builder setWordSpacing(int spacing) {
            mWordSpacing = spacing;

            return this;
        }

        public Builder setWordSize(int width, int height) {
            mWordWidth = width;
            mWordHeight = height;

            return this;
        }

        public int getPageWidth() {
            return mPageWidth;
        }

        public int getPageHeight() {
            return mPageHeight;
        }

        public int getTopMargin() {
            return mTopMargin;
        }

        public int getLeftMargin() {
            return mLeftMargin;
        }

        public int getBottomMargin() {
            return mBottomMargin;
        }

        public int getRightMargin() {
            return mRightMargin;
        }

        public int getIndent() {
            return mIndent;
        }

        public int getLineSpacing() {
            return mLineSpacing;
        }

        public int getWordSpacing() {
            return mWordSpacing;
        }

        public int getWordWidth() {
            return mWordWidth;
        }

        public int getWordHeight() {
            return mWordHeight;
        }

        public Page build() {
            return new Page(this);
        }
    }
}
