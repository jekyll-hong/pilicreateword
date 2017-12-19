package com.pilicreateworld.website;

import com.pilicreateworld.common.Story;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;

public class EpisodePage extends Page {
    public EpisodePage(String url) throws IOException {
        super(url);
    }

    public List<Story> getStoryList() {
        List<Story> storyList = new ArrayList<Story>(10);

        /**
         * 剧情口白
         */
        Iterator<Element> it = mDocument.body().getElementsByTag("img").iterator();
        while (it.hasNext()) {
            Element img = it.next();

            String value = img.attr("width");
            if (!value.isEmpty() && value.equals("760")) {
                String url = img.absUrl("src");

                if (url.endsWith(".gif")) {
                    Story story = new Story(url);
                    storyList.add(story);
                }
                else {
                    //empty src
                }
            }
        }

        return storyList;
    }
}
