package com.pilicreateworld.common;

import com.pilicreateworld.image.TextImage;
import com.pilicreateworld.website.EpisodePage;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Episode {
    private static Pattern sNamePattern = Pattern.compile("([\\u4e00-\\u9fa5]|‧|、)+");
    private static Pattern sSerialNamePattern = Pattern.compile("\\d{2}");

    private String mName;
    private int mSerialNumber;

    private String mUrl;

    public Episode(String url, String information) {
        mName = parseName(information);
        mSerialNumber = parseSerialNumber(information);

        mUrl = url;
    }

    public String getName() {
        return mName;
    }

    public int getSerialNumber() {
        return mSerialNumber;
    }

    private static String parseName(String information) {
        Matcher matcher = sNamePattern.matcher(information);
        if (matcher.find()) {
            return matcher.group();
        }

        throw new IllegalStateException("not can get the name of this episode");
    }

    private static int parseSerialNumber(String information) {
        Matcher matcher = sSerialNamePattern.matcher(information);
        if (matcher.find()) {
            String serialNumber = matcher.group();
            return Integer.parseInt(serialNumber);
        }

        /**
         * 序章
         */
        return -1;
    }

    public void export(File dir) throws IOException {
        FileWriter writer = new FileWriter(
                dir.getCanonicalPath() + "/" + getChapterTitle() + ".txt");

        /**
         * 内容
         */
        BufferedReader reader = new BufferedReader(new StringReader(getChapterText()));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            /**
             * 段落
             */
            writer.write(line);
            writer.write("\r\n");

            /**
             * 空一行
             */
            writer.write("\r\n");
        }

        writer.close();
    }

    private String getChapterTitle() {
        StringBuilder builder = new StringBuilder();

        if (mSerialNumber == -1) {
            builder.append("序章 ");
        }
        else {
            builder.append(String.format("第%02d集 ", mSerialNumber));
        }
        builder.append(getName());

        return builder.toString();
    }

    private String getChapterText() throws IOException {
        TextImage textImage = new TextImage();

        /**
         * 拼接图像
         */
        for (Story story : fetchStoriesInformation()) {
            textImage.append(story.getTextImage());
        }

        /**
         * 删除最上面的标题
         */
        textImage.deleteTitle();

        return textImage.getText();
    }

    private List<Story> fetchStoriesInformation() throws IOException {
        EpisodePage episodePage = new EpisodePage(mUrl);
        return episodePage.getStories();
    }
}
