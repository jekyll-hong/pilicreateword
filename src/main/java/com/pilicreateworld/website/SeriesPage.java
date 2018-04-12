package com.pilicreateworld.website;

import com.pilicreateworld.common.Episode;

import java.io.IOException;
import java.util.*;

import org.jsoup.nodes.Element;

public class SeriesPage extends BasePage {
    private static final int MAX_EPISODE_CNT = 60;
    
    public SeriesPage(String url) {
        super(url);
    }

    public List<Episode> getEpisodes() throws IOException {
        List<Episode> episodeList = new ArrayList<Episode>(MAX_EPISODE_CNT);

        /**
         * 全剧集
         */
        for (Element anchor : load().body().getElementsByTag("a")) {
            String value = anchor.attr("target");

            if (value != null && value.equals("_blank")) {
                String url = anchor.absUrl("href");
                String information = anchor.text();

                Episode episode = new Episode(url, information);
                episodeList.add(episode);
            }
        }

        if (episodeList.size() > 1) {
            Collections.sort(episodeList, new Comparator<Episode>() {
                public int compare(Episode episode1, Episode episode2) {
                    return episode1.getSerialNumber() - episode2.getSerialNumber();
                }
            });
        }

        return episodeList;
    }
}
