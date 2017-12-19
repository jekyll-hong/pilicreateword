package com.pilicreateworld.website;

import com.pilicreateworld.common.Episode;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

public class DramaPage extends Page {
    public DramaPage(String url) throws IOException {
        super(url);
    }

    public List<Episode> getEpisodeList() {
        List<Episode> episodeList = new ArrayList<Episode>(60);

        /**
         * 剧集介绍
         */
        Iterator<Element> it = mDocument.body().getElementsByTag("a").iterator();
        while (it.hasNext()) {
            Element anchor = it.next();

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
