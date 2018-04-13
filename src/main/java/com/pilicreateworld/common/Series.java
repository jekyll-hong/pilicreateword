package com.pilicreateworld.common;

import com.pilicreateworld.website.SeriesPage;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Series {
    private static Pattern sNamePattern = Pattern.compile("([\\u4e00-\\u9fa5]|I)+");
    private static Pattern sSerialNamePattern = Pattern.compile("\\d{2}");

    private String mName;
    private int mSerialNumber;

    private String mUrl;

    public Series(String url, String information) {
        mName = parseName(information);
        mSerialNumber = parseSerialNumber(information);

        mUrl = url;
    }

    public int getSerialNumber() {
        return mSerialNumber;
    }

    public String getName() {
        if (mSerialNumber == 0x7fffffff) {
            /**
             * 剧集连载中，未完
             */
            return "【play now】" + mName;
        }
        else {
            return "【" + String.format("%02d", mSerialNumber) + "】" + mName;
        }
    }

    public List<Episode> fetchEpisodesInformation() throws IOException {
        SeriesPage seriesPage = new SeriesPage(mUrl);
        return seriesPage.getEpisodes();
    }

    private static String parseName(String information) {
        Matcher matcher = sNamePattern.matcher(information);
        if (matcher.find()) {
            return matcher.group();
        }

        throw new IllegalStateException("not can get the name of this series");
    }

    private static int parseSerialNumber(String information) {
        Matcher matcher = sSerialNamePattern.matcher(information);
        if (matcher.find()) {
            String serialNumber = matcher.group();
            return Integer.parseInt(serialNumber);
        }

        /**
         * playing now
         */
        return 0x7fffffff;
    }
}
