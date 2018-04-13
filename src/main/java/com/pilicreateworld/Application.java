package com.pilicreateworld;

import com.pilicreateworld.common.Episode;
import com.pilicreateworld.common.Series;
import com.pilicreateworld.website.HomePage;
import com.pilicreateworld.website.MainPage;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class Application {
	private static final String sHomePageUrl = "https://pilicreateworld.tw-blog.com";
	
	public static void main(String[] args) {
		/**
		 * 传入参数
		 */
		for (String arg : args) {
			if (arg.startsWith("--output=")) {
				String path = arg.substring(9);

				Settings.getInstance().setOutputDirectory(path);
			}
			else if (arg.startsWith("--proxy=")) {
				String proxy = arg.substring(8);

				Settings.getInstance().setProxy(proxy);
			}
			else {
				//TODO: add more here
			}
		}
		
		Application app = new Application();
		app.execute();
	}

	private void execute() {
		Scanner scanner = new Scanner(System.in);

		try {
			List<Series> seriesList = fetchSeriesInformation();
			for (Series series : seriesList) {
				System.out.print(seriesList.indexOf(series) + "." + series.getName() + "\n");
			}

			System.out.print("请输入序号：");
			Series series = seriesList.get(scanner.nextInt());

			File seriesDir = new File(Settings.getInstance().getOutputDirectory()
					+ "/" + series.getName());
			if (!seriesDir.exists()) {
				seriesDir.mkdir();
			}

			List<Episode> episodeList = series.fetchEpisodesInformation();
			for (Episode episode : episodeList) {
				System.out.print(episodeList.indexOf(episode) + "." + episode.getName() + "\n");
			}

			String exitOrNot;
			do {
				System.out.print("请输入序号：");
				Episode episode = episodeList.get(scanner.nextInt());

				export(seriesDir, episode);

				System.out.print("是否继续（Y or N）：");
				exitOrNot = scanner.next();
			}
			while (exitOrNot.equalsIgnoreCase("y"));
		}
		catch (IOException e) {
			System.err.print("失败，" + e.getMessage() + "\n");

			e.printStackTrace();
		}
		finally {
			scanner.close();
		}
	}

	private void export(File dir, Episode episode) throws IOException {
		FileWriter writer = new FileWriter(
				dir.getCanonicalPath() + "/" + episode.getChapterTitle() + ".txt");

		/**
		 * 内容
		 */
		BufferedReader reader = new BufferedReader(
				new StringReader(episode.getChapterText()));
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}

			/**
			 * 段落
			 */
			writer.write(line);
			writer.write("\r\n");

			/**
			 * 空一行
			 */
			writer.write("\r\n");
		}

		writer.close();
	}

	private static List<Series> fetchSeriesInformation() throws IOException {
		HomePage homePage = new HomePage(sHomePageUrl);
		String mainPageUrl = homePage.getMainPageUrl();

		MainPage mainPage = new MainPage(mainPageUrl);
		return mainPage.getSeries();
	}
}
