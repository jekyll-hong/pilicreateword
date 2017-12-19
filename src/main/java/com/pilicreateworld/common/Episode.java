package com.pilicreateworld.common;

import com.pilicreateworld.image.Text;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Episode {
    private static Pattern sNamePattern = Pattern.compile("[\\u4e00-\\u9fa5]+");
    private static Pattern sSerialNamePattern = Pattern.compile("\\d{2}");

    private String mUrl;
    private String mName;
    private int mSerialNumber;

    private Text mFullStoryText = null;

    public Episode(String url, String information) {
        mUrl = url;
        mName = parseName(information);
        mSerialNumber = parseSerialNumber(information);
    }

    public String getUrl() {
        return mUrl;
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

        return "";
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

    public void addStory(Story story) throws IOException {
        Text storyText = story.getText();

        if (mFullStoryText == null) {
            mFullStoryText = storyText;

            /**
             * 删除第一段最上面的标题
             * “创世小组：xx 网站：霹雳创世录：pilicreateworld.tw-blog.com 霹雳创世录”
             */
            mFullStoryText.deleteTitle();
        }
        else {
            mFullStoryText.append(storyText);
        }
    }

    public Text getFullStoryText() {
        return mFullStoryText;
    }
}
