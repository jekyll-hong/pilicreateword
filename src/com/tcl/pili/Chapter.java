package com.tcl.pili;

import java.io.File;

final class Chapter {
	public String title;
	public File[] pages;
	
	public Chapter(String title, File[] pages) {
		this.title = title;
		this.pages = pages;
	}
}