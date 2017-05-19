package com.tcl.pili;

final class WebsiteParser implements Runnable {
	private static final String sWebsite = "https://pilicreateworld.tw-blog.com";

	private Proxy mHTTPProxy;
	private Observer mObserver;
	
	public WebsiteParser(Proxy proxy) {
		mHTTPProxy = proxy;
        }

        public interface Observer {
		void onGetDrama(String name);
                void onGetEpisode(String name);
                void onGetPlot(BufferedImage image);
		void onCompleted();
        }

	public void setObserver(Observer observer) {
		mObserver = observer;
	}

	public void run() {
		parseWebsite(sWebsite);
	}

	private void parseWebsiteHTML(String websiteURL) {
		String mainURL = "";
		
		HTTPClient client = new HTTPClient(websiteURL, Proxy);
                Document doc = Jsoup.parse(client.getInputStream, "UTF-16", sWebsite);
		Element	body = doc.body();
		Elements frameList = body.getElementsByTag("frame");
                for (int i = 0; i < frameList.size(); i++) {
			Element frame = frameList.get(i);

			String value = frame.attr("name");
			if (!value.isEmpty() && value.equals("rbottom2")) {
				mainURL = sWebsite + "/" + frame.attr("src");
				break;
			}
		}
		client.release();
		
		if (!mainURL.isEmpty()) {
			parseMainHTML(mainURL);
			mObserver.onCompleted();
		}
		else {
			System.err.print("not find main url!\r\n");
		}
	}

	private class DramaInfo {
		public String url;
		public String name;
	}

	private void parseMainHTML(String mainURL) {
		ArrayList<DramaInfo> dramaList = new ArrayList<DramaInfo>(100);

		HTTPClient client = new HTTPClient(mainURL, Proxy);
                Document doc = Jsoup.parse(client.getInputStream, "UTF-16", sWebsite);
		Element	body = doc.body();
		Elements anchorList = body.getElementsByTag("a");
		for (int i = 0; i < anchorList.size(); i++) {
			Element anchor = anchorList.get(i);

			String value = anchor.attr("id");
			if (!value.isEmpty() && value.startWith("info")) {
				DramaInfo drama = new DramaInfo();
				drama.url = sWebsite + "/" + anchor.attr("href");
				drama.name = value.substring(4) + "." + getDramaName(anchor.text());

				dramaList.add(drama);
			}
		}
		client.release();

		if (!dramaList.isEmpty()) {
			for (int i = 0; i < dramaList.size(); i++) {
				DramaInfo drama = dramaList.get(i);
				
				mObserver.onGetDrama(drama.name);
				parseDramaHTML(drama.url);
			}
		}
		else {
			System.err.print("not find drama!\r\n");
		}
	}

	private String getDramaName(String text) {
		int leftBracket = text.indexOf("【");
		int rightBracket = text.lastIndexOf("】");

		return text.substring(leftBracket + 1, rightBracket);
	}

	private class EpisodeInfo {
		public String url;
		public String name;
	}

	private void parseDramaHTML(String dramaURL) {
		ArrayList<EpisodeInfo> episodeList = new ArrayList<EpisodeInfo>(100);

		HTTPClient client = new HTTPClient(dramaURL, Proxy);
                Document doc = Jsoup.parse(client.getInputStream, "UTF-16", sWebsite);
		Element	body = doc.body();
		Elements anchorList = body.getElementsByTag("a");
		for (int i = 0; i < anchorList.size(); i++) {
			Element anchor = anchorList.get(i);

			String value = anchor.attr("target");
			if (!value.isEmpty() && value.equals("_blank")) {
				EpisodeInfo episode = new EpisodeInfo();
				episode.url = sWebsite + "/PILI/" + anchor.attr("href");
				episode.name = getEpisodeName(anchor.text());

				episodeList.add(episode);
			}
		}
		client.release();

		if (!episodeList.isEmpty()) {
			for (int i = 0; i < episodeList.size(); i++) {
				EpisodeInfo episode = episodeList.get(i);
				
				mObserver.onGetEpisode(episode.name);
				parseEpisodeHTML(episode.url);
			}
		}
		else {
			System.err.print("not find episode!\r\n");
		}
	}

	private String getEpisodeName(String text) {
		int start = text.indexOf(".") - 2;
		int end = text.indexOf("&nbsp;");

		if (end < 0) {
			return text.substring(start);
		}
		else {
			return text.substring(start, end);
		}
	}

	private void parseEpisodeHTML(String episodeURL) {
		ArrayList<BufferedImage> plotList = new ArrayList<BufferedImage>(10);

		HTTPClient client = new HTTPClient(episodeURL, Proxy);
                Document doc = Jsoup.parse(client.getInputStream, "UTF-16", sWebsite);
		Element	body = doc.body();
		Elements imgList = body.getElementsByTag("img");
		for (int i = 0; i < imgList.size(); i++) {
			Element img = imgList.get(i);

			String value = anchor.attr("width");
			if (!value.isEmpty() && value.equals("760")) {
				String plotURL = img.attr("src");
				BufferedImage plot = ImageIO.read(new URL(plotURL));
				
				plotList.add(plot);
			}
		}
		client.release();

		if (!plotList.isEmpty()) {
			int width = getPlotImageWidth(plotList);
			int height = getPlotImageHeight(plotList);
			int type = getPlotImageType(plotList);
			BufferedImage plot = new BufferedImage(width, height, type);
			
			Graphics2D graphics = plot.createGraphics()
			int yOffset = 0;
			for (int i = 0; i < plotList.size(); i++) {
				BufferedImage part = plotList.get(i);
				graphics.drawImage(part, null, 0, yOffset);
				yOffset += part.getHeight();
			}
			
			mObserver.onGetPlot(plot);
		}
		else {
			System.err.print("not find plot!\r\n");
		}
	}

	private int getPlotImageWidth(ArrayList<BufferedImage> plotList) {
		return plotList.get(0).getWidth();
	}

	private int getPlotImageHeight(ArrayList<BufferedImage> plotList) {
		int height = 0;
		for (int i = 0; i < images.length; i++) {
			height += plotList.get(i).getHeight();
		}
		return height;
	}

	private int getPlotImageType(ArrayList<BufferedImage> plotList) {
		return plotList.get(0).getType(0);
	}
}
