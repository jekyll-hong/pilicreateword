package com.pilicreateworld;

import com.pilicreateworld.common.Series;
import com.pilicreateworld.website.HomePage;
import com.pilicreateworld.website.MainPage;

import java.io.IOException;
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
				System.out.print(seriesList.indexOf(series) + " ——《" + series.getName() + "》\n");
			}

			System.out.print("请输入序号：");

			int index = scanner.nextInt();
			if (index < 0 || index >= seriesList.size()) {
				System.err.print("无效的序号\n");
			}
			else {
                Series series = seriesList.get(index);
                series.export();

				System.out.print("《" + series.getName() + "》导出成功！\n");
			}
		}
		catch (IOException e) {
			System.err.print("失败，" + e.getMessage() + "\n");

			e.printStackTrace();
		}
		finally {
			scanner.close();
		}
	}

	private static List<Series> fetchSeriesInformation() throws IOException {
		HomePage homePage = new HomePage(sHomePageUrl);
		String mainPageUrl = homePage.getMainPageUrl();

		MainPage mainPage = new MainPage(mainPageUrl);
		return mainPage.getSeries();
	}
}
