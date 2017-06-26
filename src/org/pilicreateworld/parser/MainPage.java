package org.pilicreateworld.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class MainPage extends WebPage implements Observer {
	private ArrayList<DramaPage> dramaList;
	private int packCnt;
	
	public MainPage(String url, File dir, Observer observer) {
		super(url, dir, observer);
		
		dramaList = new ArrayList<DramaPage>(100);
		packCnt = 0;
	}
	
	public void onPageLoad(Document doc) {
		Iterator<Element> it = doc.body().getElementsByTag("a").iterator();
		while (it.hasNext()) {
			Element anchor = it.next();
			
			String value = anchor.attr("id");
			if (value != null && value.startsWith("info")) {
				String text = anchor.text().replace("\u00a0", "");
				text = Utils.convertToUTF16(text);
				
				String dramaPageUrl = getDramaPageUrl(anchor.attr("href"));
				String dramaSerialNumber = getDramaSerialNumber(text, value);
				String dramaName = getDramaName(text);
				File dramaDir = getDramaDir(dramaSerialNumber, dramaName);
				
				dramaList.add(new DramaPage(dramaPageUrl, dramaDir, this, dramaSerialNumber, dramaName));
			}
		}
		
		if (!dramaList.isEmpty()) {
			sortDramaBySerialNumber();
			
			for (int i = 0; i < dramaList.size(); i++) {
				dramaList.get(i).load();
			}
		}
		else {
			System.err.print("can not find any drama!\r\n");
		}
	}
	
	private String getDramaPageUrl(String href) {
		return url.substring(0, url.lastIndexOf("/")) + "/" + href;
	}
	
	private String getDramaSerialNumber(String text, String id) {
		int pos = text.indexOf(".");
		if (pos > 0) {
			return text.substring(0, pos);
		}
		
		return id.substring(4);
	}
	
	private String getDramaName(String text) {
		int leftBracket = text.indexOf("【");
		int rightBracket = text.lastIndexOf("】");
		
		return text.substring(leftBracket + 1, rightBracket);
	}
	
	private File getDramaDir(String derialNumber, String name) {
		if (!dir.exists()) {
			dir.mkdir(); 
		}
		
		return Utils.getChildFile(dir, derialNumber + "." + name);
	}
	
	private void sortDramaBySerialNumber() {
		Collections.sort(dramaList, new Comparator<DramaPage>() {
			public int compare(DramaPage drama1, DramaPage drama2) {
				return drama1.getSerialNumber() - drama2.getSerialNumber();
			}
		});
	}
	
	public void onError() {
		System.err.print("load main page error!\r\n");
	}
	
	public synchronized void onComplete() {
		if (++packCnt < dramaList.size()) {
			return;
		}
		
		System.out.print("all dramas of PILI done!\r\n");
		
		observer.onComplete();
	}
}