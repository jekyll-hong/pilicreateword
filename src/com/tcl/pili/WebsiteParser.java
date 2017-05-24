package com.tcl.pili;

final class WebsiteParser extends Thread {
	private static final String sWebsite = "https://pilicreateworld.tw-blog.com";
	
	private File mStorageDir;
	private File mDramaDir;
	private File mEpisodeDir;
	
	private HTTPClient mClient;
	
	public WebsiteParser(String storageDirPath) {
		mStorageDir = new File(storagePath);
		if (!mStorageDir.exist()) {
			mStorageDir.mkdirs();
		}
		
		mClient = new HTTPClient();
        }
	
	public void setProxy(String ip, int port) {
		Proxy httpProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
		mClient.setProxy(httpProxy);
	}
	
	public void run() {
		parseWebsiteHTML(sWebsite);
	}
	
	private void parseWebsiteHTML(String websiteURL) {
		mClient.connect(websiteURL);
                String mainPageURL = getMainURL(mClient.getInputStream());
		mClient.disconnect();
		
		if (!mainURL.isEmpty()) {
			parseMainHTML(mainPageURL);
			
			Message msg = new Message(Message.MSG_COMPLETE);
			MessageLooper.getInstance().post(msg);
		}
		else {
			System.err.print("parse website error, can not find main url!\r\n");
		}
	}
	
	private String getMainURL(InputStream stream) {
		String mainURL = "";
		
		Document doc = Jsoup.parse(stream, "UTF-16", sWebsite);
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
		
		return mainURL;
	}
	
	private class DramaInfo {
		public String url;
		public String name;
	}
	
	private void parseMainHTML(String mainURL) {
		mClient.connect(mainURL);
                ArrayList<DramaInfo> dramaList = getDramaList(mClient.getInputStream());
		mClient.disconnect();
		
		if (!dramaList.isEmpty()) {
			for (int i = 0; i < dramaList.size(); i++) {
				DramaInfo drama = dramaList.get(i);
				
				mDramaDir = Utils.getChildFile(mStorageDir, drama.name);
				if (!mDramaDir.exist()) {
					mDramaDir.mkdir();
				}
				
				System.out.print(drama.name + "\r\n");
				parseDramaHTML(drama.url);
				
				Message msg = new Message(Message.MSG_PACK_PDF, mDramaDir);
				MessageLooper.getInstance().post(msg);
			}
		}
		else {
			System.err.print("parse website error, can not find any drama!\r\n");
		}
	}
	
	private ArrayList<DramaInfo> getDramaList(InputStream stream) {
		ArrayList<DramaInfo> dramaList = new ArrayList<DramaInfo>(100);
		
		Document doc = Jsoup.parse(stream, "UTF-16", sWebsite);
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
		
		dramaList.trimToSize();
		Collections.sort(dramaList, new Comparator<DramaInfo>() {
			public int compare(DramaInfo drama1, DramaInfo drama2) {
				String name1 = drama1.name;
				String serialNumber1 = name1.substring(0, name1.indexOf("."));
				
				String name2 = drama2.name;
				String serialNumber2 = name2.substring(0, name2.indexOf("."));
				
				return Integer.parseInt(serialNumber1) - Integer.parseInt(serialNumber2);
			}
		});
		
		return dramaList;
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
		mClient.connect(dramaURL);
                ArrayList<EpisodeInfo> episodeList = getEpisodeList(mClient.getInputStream());
		mClient.disconnect();
		
		if (!episodeList.isEmpty()) {
			for (int i = 0; i < episodeList.size(); i++) {
				EpisodeInfo episode = episodeList.get(i);
				
				mEpisodeDir = Utils.getChildFile(mDramaDir, episode.name);
				if (!mEpisodeDir.exist()) {
					mEpisodeDir.mkdir();
				}
				
				System.out.print(episode.name + "\r\n");
				parseEpisodeHTML(episode.url);
			}
		}
		else {
			System.err.print("parse website error, can not find any episode!\r\n");
		}
	}
	
	private ArrayList<EpisodeInfo> getEpisodeList(InputStream stream) {
		ArrayList<EpisodeInfo> episodeList = new ArrayList<EpisodeInfo>(100);
		
		Document doc = Jsoup.parse(stream, "UTF-16", sWebsite);
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
		
		episodeList.trimToSize();
		Collections.sort(episodeList, new Comparator<EpisodeInfo>() {
			public int compare(EpisodeInfo episode1, EpisodeInfo episode2) {
				String name1 = episode1.name;
				String serialNumber1 = name1.substring(0, name1.indexOf("."));
				
				String name2 = episode2.name;
				String serialNumber2 = name2.substring(0, name2.indexOf("."));
				
				return Integer.parseInt(serialNumber1) - Integer.parseInt(serialNumber2);
			}
		});
		
		return episodeList;
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
		mClient.connect(episodeURL);
                ArrayList<BufferedImage> plotImageList = getPlotImageList(mClient.getInputStream());
		mClient.disconnect();
		
		if (!plotImageList.isEmpty()) {
			BufferedImage plotImage = merge(plotImageList);
			
			File plotImageFile = Utils.getChildFile(mEpisodeDir, "剧情口白.png");
			if (!plotImageFile.exist()) {
				ImageIO.write(plotImage, "png", plotImageFile);
			}
			
			Message msg = new Message(Message.MSG_MAKE_PAGES, plotImage);
			MessageLooper.getInstance().post(msg);
		}
		else {
			System.err.print("parse website error, can not find any plot image!\r\n");
		}
	}
	
	private ArrayList<BufferedImage> getPlotImageList(InputStream stream) {
		ArrayList<BufferedImage> plotImageList = new ArrayList<BufferedImage>(10);
		
		Document doc = Jsoup.parse(stream, "UTF-16", sWebsite);
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
		
		plotImageList.trimToSize();
		
		return plotImageList;
	}
	
	private BufferedImage merge(ArrayList<BufferedImage> srcList) {
		int width = getPlotImageWidth(srcList);
		int height = getPlotImageHeight(srcList);
		int type = getPlotImageType(srcList);
		
		BufferedImage dst = new BufferedImage(width, height, type);
		Graphics2D graphics = dst.createGraphics();
		
		int yOffset = 0;
		for (int i = 0; i < plotList.size(); i++) {
			BufferedImage src = srcList.get(i);
			if (Utils.DEBUG) {
				ImageIO.write(src, "jpeg", Utils.getChildFile(mEpisodeDir, i + ".jpg"));
			}
			
			graphics.drawImage(src, null, 0, yOffset);
			yOffset += src.getHeight();
		}
		
		return dst;
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
		return plotList.get(0).getType();
	}
}
