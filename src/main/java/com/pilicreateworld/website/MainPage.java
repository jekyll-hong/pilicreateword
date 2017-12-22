package com.pilicreateworld.website;

import com.pilicreateworld.common.Series;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

public class MainPage extends BasePage {
    public MainPage(String url) {
        super(url);
    }

    public List<Series> getSeries() throws IOException {
        List<Series> seriesList = new ArrayList<Series>(100);

        /**
         * 全系列
         */
        for (Element anchor : load().body().getElementsByTag("a")) {
            String value = anchor.attr("id");

            if (value != null && value.startsWith("info")) {
                String url = anchor.absUrl("href");
                String information = anchor.text();

                Series series = new Series(url, information);
                seriesList.add(series);
            }
        }

        if (seriesList.size() > 1) {
            Collections.sort(seriesList, new Comparator<Series>() {
                public int compare(Series series1, Series series2) {
                    return series1.getSerialNumber() - series2.getSerialNumber();
                }
            });
        }

        return seriesList;
    }
}
