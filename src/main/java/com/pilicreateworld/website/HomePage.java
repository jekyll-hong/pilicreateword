package com.pilicreateworld.website;

import java.io.IOException;

import org.jsoup.nodes.Element;

public class HomePage extends BasePage {
    public HomePage(String url) {
        super(url);
    }

    public String getMainPageUrl() throws IOException {
        /**
         * 网站主页
         */
        for (Element frame : load().select("frame")) {
            String value = frame.attr("name");

            if (value != null && value.equals("rbottom2")) {
                return frame.absUrl("src");
            }
        }

        throw new IllegalStateException("find main page url fail!");
    }
}
