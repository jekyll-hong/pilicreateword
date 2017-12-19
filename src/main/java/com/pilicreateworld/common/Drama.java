package com.pilicreateworld.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Drama {
    private static Pattern sNamePattern = Pattern.compile("[\\u4e00-\\u9fa5]+");
    private static Pattern sSerialNamePattern = Pattern.compile("\\d{2}");

    private String mUrl;
    private String mName;
    private int mSerialNumber;

    public Drama(String url, String information) {
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
         * playing now
         */
        return 0x7fffffff;
    }
}
