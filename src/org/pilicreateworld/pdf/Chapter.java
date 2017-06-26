package org.pilicreateworld.pdf;

import java.io.File;

public class Chapter {
	private String title;
	private File[] pages;
	
	public Chapter(String title, File[] pages) {
		this.title = title;
		this.pages = pages;
	}
	
	public String title() {
		return title;
	}
	
	public int getPageCount() {
		return pages.length;
	}
	
	public String getPagePath(int index) {
		return pages[index].getPath();
	}
}