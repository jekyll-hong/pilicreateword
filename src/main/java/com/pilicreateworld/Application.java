package com.pilicreateworld;

import com.pilicreateworld.common.Drama;
import com.pilicreateworld.common.Episode;
import com.pilicreateworld.common.Story;
import com.pilicreateworld.ebook.PdfCreator;
import com.pilicreateworld.typeset.Typesetter;
import com.pilicreateworld.website.DramaPage;
import com.pilicreateworld.website.EpisodePage;
import com.pilicreateworld.website.HomePage;
import com.pilicreateworld.website.MainPage;

import java.io.IOException;
import java.util.List;

public class Application {
	private static final String sHomePageUrl = "https://pilicreateworld.tw-blog.com";
	
	public static void main(String[] args) {
		Argument argument = Argument.parse(args);

		if (argument.isInvalid()) {
			System.err.printf("invalid arguments!\n");
			return;
		}
		
		Application app = new Application();
		app.execute(argument);
	}

	private void execute(Argument argument) {
		Typesetter mTypesetter = new Typesetter(argument.getTypesetParameter());
		PdfCreator mPdfCreator = new PdfCreator(argument.getOutputDir());

		try {
			HomePage homePage = new HomePage(sHomePageUrl);
			String mainPageUrl = homePage.getMainPageUrl();

			MainPage mainPage = new MainPage(mainPageUrl);
            List<Drama> dramaList = mainPage.getDramaList();

			for (int i = 0; i < dramaList.size(); i++) {
                Drama drama = dramaList.get(i);

                String fileName = String.format("%2d.%s", i + 1, drama.getName());
                mPdfCreator.open(fileName);

                DramaPage dramaPage = new DramaPage(drama.getUrl());
                List<Episode> episodeList = dramaPage.getEpisodeList();

				for (int j = 0; j < episodeList.size(); j++) {
                    Episode episode = episodeList.get(j);

					EpisodePage episodePage = new EpisodePage(episode.getUrl());
					List<Story> storyList = episodePage.getStoryList();

					for (int k = 0; k < storyList.size(); k++) {
                        Story story = storyList.get(k);

						episode.addStory(story);
					}

                    String chapterTitle = episode.getName();
					mPdfCreator.writeChapter(chapterTitle,
							mTypesetter.typeset(episode.getFullStoryText().findWords()));
				}

				mPdfCreator.close();
			}
		}
		catch (IOException e) {
			System.err.printf("network error!\n");
		}
	}
}