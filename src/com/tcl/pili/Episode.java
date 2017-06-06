package com.tcl.pili;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

final class Episode implements Typesetter.OnTypesetCompleteListener, PDFPacker.OnPackCompleteListener {
	private Drama drama;
	
	public String url;
	public File dir;
	public ArrayList<Plot> plotList;
	public File pdf;
	
	public Episode(Drama drama) {
		this.drama = drama;
	}
	
	public File getDir() {
		return dir;
	}
	
	public void notify(File image) {
		
	}
	
	public void onTypesetComplete(ArrayList<BufferedImage> pageImageList) {
		// TODO Auto-generated method stub
		
	}
	
	public void onPackComplete(File pdf) {
		// TODO Auto-generated method stub
		
	}
}
