package org.pilicreateworld.parser;

import java.io.File;
import java.util.Iterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class WebsitePage extends WebPage {
	public WebsitePage(String url, File dir, Observer observer) {
		super(url, dir, observer);
	}
	
	public void onPageLoad(Document doc) {
		Iterator<Element> it = doc.select("frame").iterator();
		while (it.hasNext()) {
			Element frame = it.next();
			
			String value = frame.attr("name");
			if (value != null && value.equals("rbottom2")) {
				String mainPageUrl = getMainPageUrl(frame.attr("src"));
				
				MainPage mainPage = new MainPage(mainPageUrl, getDir(), observer);
				mainPage.load();
				return;
			}
		}
        
		System.err.print("can not find main page url!\r\n");
	}
	
	private String getMainPageUrl(String src) {
		return url + "/" + src;
	}
	
	private File getDir() {
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		return Utils.getChildFile(dir, "PILI");
	}
	
	public void onError() {
		System.err.print("load website page error!\r\n");
	}
}