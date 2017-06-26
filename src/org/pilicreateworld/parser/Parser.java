package org.pilicreateworld.parser;

import java.io.File;

public class Parser {
	private File dir;
	
	public Parser(String storageDirPath) {
		dir = new File(storageDirPath);
	}
	
	public void parse(Observer observer) {
		WebsitePage websitePage = new WebsitePage("https://pilicreateworld.tw-blog.com", dir, observer);
		websitePage.load();
	}
}