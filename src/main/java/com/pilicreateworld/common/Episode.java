package com.pilicreateworld.common;

import com.pilicreateworld.image.Text;
import com.pilicreateworld.website.EpisodePage;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Episode {
    private static Pattern sNamePattern = Pattern.compile("[\\u4e00-\\u9fa5]+");
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

    public Text getFullText() throws IOException {
        Text fullText = new Text();

        /**
         * 合并剧情口白
         */
        for (Story story : fetchStoriesInformation()) {
            Text text = story.getText();
            fullText.append(text);
        }

        /**
         * 删除最上面的标题
         * “创世小组：xx 网站：霹雳创世录：pilicreateworld.tw-blog.com 霹雳创世录”
         */
        fullText.deleteTitle();
        /**
         * 检测汉字
         */
        fullText.detectWords();

        return fullText;
    }

    private List<Story> fetchStoriesInformation() throws IOException {
        EpisodePage episodePage = new EpisodePage(mUrl);
        return episodePage.getStories();
    }
}
