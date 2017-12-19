package com.pilicreateworld.website;

import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Iterator;

public class HomePage extends Page {
    public HomePage(String url) throws IOException {
        super(url);
    }

    public String getMainPageUrl() {
        /**
         * 网站主页
         */
        Iterator<Element> it = mDocument.select("frame").iterator();
        while (it.hasNext()) {
            Element frame = it.next();

            String value = frame.attr("name");
            if (value != null && value.equals("rbottom2")) {
                return frame.absUrl("src");
            }
        }

        throw new IllegalStateException("find main page url fail!");
    }
}
