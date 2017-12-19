package com.pilicreateworld.website;

import com.pilicreateworld.common.Drama;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

public class MainPage extends Page {
    public MainPage(String url) throws IOException {
        super(url);
    }

    public List<Drama> getDramaList() {
        List<Drama> dramaList = new ArrayList<Drama>(100);

        /**
         * 霹雳布袋戏全系列
         */
        Iterator<Element> it = mDocument.body().getElementsByTag("a").iterator();
        while (it.hasNext()) {
            Element anchor = it.next();

            String value = anchor.attr("id");
            if (value != null && value.startsWith("info")) {
                String url = anchor.absUrl("href");
                String information = anchor.text();

                Drama drama = new Drama(url, information);
                dramaList.add(drama);
            }
        }

        if (dramaList.size() > 1) {
            Collections.sort(dramaList, new Comparator<Drama>() {
                public int compare(Drama drama1, Drama drama2) {
                    return drama1.getSerialNumber() - drama2.getSerialNumber();
                }
            });
        }

        return dramaList;
    }
}
