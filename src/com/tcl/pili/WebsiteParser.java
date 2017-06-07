package com.tcl.pili;

import java.io.File;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

final class WebsiteParser {
	private static final String WEBSITE = "https://pilicreateworld.tw-blog.com";
	
	private File storageDir;
	private Pili pili;
	
	public WebsiteParser(File storageDir) {
		this.storageDir = storageDir;
	}
	
	public void parse() {
		loadPage(WEBSITE, new OnWebsitePageLoadedListener(storageDir));
	}
	
	private class OnWebsitePageLoadedListener implements OnPageLoadListener {
		private File storageDir;
		
		public OnWebsitePageLoadedListener(File storageDir) {
			this.storageDir = storageDir;
		}
		
		public void onPageLoad(Document doc) {
			parseWebsitePage(doc, storageDir);
		}
		
		public void onError() {
			System.err.print("load website page error!\r\n");
		}
	}
	
	private void parseWebsitePage(Document doc, File storageDir) {
		Elements frameList = doc.select("frame");
        for (int i = 0; i < frameList.size(); i++) {
			Element frame = frameList.get(i);
			
			String value = frame.attr("name");
			if (!value.isEmpty() && value.equals("rbottom2")) {
				String url = WEBSITE + "/" + frame.attr("src");
				pili = new Pili(storageDir, url);
				break;
			}
		}
        
        if (pili == null) {
			loadPage(pili.getUrl(), new OnMainPageLoadedListener(pili));
		}
		else {
			System.err.print("can not find main page url!\r\n");
		}
	}
	
	private class OnMainPageLoadedListener implements OnPageLoadListener {
		private Pili pili;
		
		public OnMainPageLoadedListener(Pili pili) {
			this.pili = pili;
		}
		
		public void onPageLoad(Document doc) {
			parseMainPage(doc, pili);
		}
		
		public void onError() {
			System.err.print("load main page error!\r\n");
		}
	}
	
	private void parseMainPage(Document doc, Pili pili) {
		Elements anchorList = doc.body().getElementsByTag("a");
		for (int i = 0; i < anchorList.size(); i++) {
			Element anchor = anchorList.get(i);
			
			String value = anchor.attr("id");
			if (!value.isEmpty() && value.startsWith("info")) {
				String url = WEBSITE + "/" + anchor.attr("href");
				String name = value.substring(4) + "." + getDramaName(anchor.text());
				pili.addDrama(new Drama(pili, url, name));
			}
		}
		
		if (pili.getDramaCount() > 0) {
			pili.sortDramaBySerialNumber();
			
			for (int i = 0; i < pili.getDramaCount(); i++) {
				Drama drama = pili.getDrama(i);
				loadPage(drama.getUrl(), new OnDramaPageLoadedListener(drama));
			}
		}
		else {
			System.err.print("can not find any drama!\r\n");
		}
	}
	
	private String getDramaName(String text) {
		text = Utils.convertToUTF16(text);
		
		int leftBracket = text.indexOf("【");
		int rightBracket = text.lastIndexOf("】");
		
		return text.substring(leftBracket + 1, rightBracket);
	}
	
	private class OnDramaPageLoadedListener implements OnPageLoadListener {
		private Drama drama;
		
		public OnDramaPageLoadedListener(Drama drama) {
			this.drama = drama;
		}
		
		public void onPageLoad(Document doc) {
			parseDramaPage(doc, drama);
		}
		
		public void onError() {
			System.err.print("load drama page error!\r\n");
		}
	}
	
	private void parseDramaPage(Document doc, Drama drama) {
		Elements anchorList = doc.body().getElementsByTag("a");
		for (int i = 0; i < anchorList.size(); i++) {
			Element anchor = anchorList.get(i);
			
			String value = anchor.attr("target");
			if (!value.isEmpty() && value.equals("_blank")) {
				String url = WEBSITE + "/PILI/" + anchor.attr("href");
				String name = getEpisodeName(anchor.text());
				drama.addEpisode(new Episode(drama, url, name));
			}
		}
		
		if (drama.getEpisodeCount() > 0) {
			drama.sortEpisodeBySerialNumber();
			
			for (int i = 0; i < drama.getEpisodeCount(); i++) {
				Episode episode = drama.getEpisode(i);
				loadPage(episode.getUrl(), new OnEpisodePageLoadedListener(episode));
			}
		}
		else {
			System.err.print("can not find episode information!\r\n");
		}
	}
	
	private String getEpisodeName(String text) {
		text = text.replace("\u00a0", "");
		return Utils.convertToUTF16(text);
	}
	
	private class OnEpisodePageLoadedListener implements OnPageLoadListener {
		private Episode episode;
		
		public OnEpisodePageLoadedListener(Episode episode) {
			this.episode = episode;
		}
		
		public void onPageLoad(Document doc) {
			parseEpisodePage(doc, episode);
		}
		
		public void onError() {
			System.err.print("load episode page error!\r\n");
		}
	}
	
	private void parseEpisodePage(Document doc, Episode episode) {
		Elements imgList = doc.body().getElementsByTag("img");
		for (int i = 0; i < imgList.size(); i++) {
			Element img = imgList.get(i);
			
			String value = img.attr("width");
			if (!value.isEmpty() && value.equals("760")) {
				String url = img.attr("src");
				episode.addPlot(new Plot(episode, url));
			}
		}
		
		if (episode.getPlotCount() > 0) {
			for (int i = 0; i < episode.getPlotCount(); i++) {
				Plot plot = episode.getPlot(i);
				plot.maybeDownload();
			}
		}
		else {
			System.err.print("can not find plot image!\r\n");
		}
	}
	
	private void loadPage(String url, OnPageLoadListener listener) {
		Message msg = new Message(Message.MSG_LOAD_WEBPAGE, new LoadPage(url, WEBSITE, listener));
		MessageLooper.getInstance().post(msg);
	}
}