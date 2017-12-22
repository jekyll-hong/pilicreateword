package com.pilicreateworld.website;

import com.pilicreateworld.common.Story;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

public class EpisodePage extends BasePage {
    public EpisodePage(String url) {
        super(url);
    }

    public List<Story> getStories() throws IOException {
        List<Story> storyList = new ArrayList<Story>(10);

        /**
         * 剧情口白
         */
        for (Element img : load().body().getElementsByTag("img")) {
            String value = img.attr("width");

            if (!value.isEmpty() && value.equals("760")) {
                String url = img.absUrl("src");

                if (!url.isEmpty()) {
                    Story story = new Story(url);
                    storyList.add(story);
                }
            }
        }

        return storyList;
    }
}
