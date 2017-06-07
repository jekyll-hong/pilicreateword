package com.tcl.pili;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

final class Episode implements OnTextTypesetListener {
	private Drama drama;
	private String url;
	private String name;
	
	private ArrayList<Plot> plotList;
	private int downloadedCnt;
	
	public File[] pages;
	
	public Episode(Drama drama, String url, String name) {
		this.drama = drama;
		this.url = url;
		this.name = name;
		
		plotList = new ArrayList<Plot>(10);
		downloadedCnt = 0;
	}
	
	public File getDir() {
		File episodeDir = Utils.getChildFile(drama.getDir(), name);
		if (!episodeDir.exists()) {
			episodeDir.mkdir();
		}
		
		return episodeDir;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getName() {
		return name;
	}
	
	public void addPlot(Plot plot) {
		plotList.add(plot);
	}
	
	public int getPlotCount() {
		return plotList.size();
	}
	
	public Plot getPlot(int index) {
		if (index >= plotList.size()) {
			return null;
		}
		
		return plotList.get(index);
	}
	
	public synchronized void notifyPlotDone() {
		if (++downloadedCnt == plotList.size()) {
			Message msg = new Message(Message.MSG_TYPESET, new TypesetText(merge(), this));
			MessageLooper.getInstance().post(msg);
		}
	}
	
	private BufferedImage merge(){
		BufferedImage[] plotImages = new BufferedImage[plotList.size()];
		for (int i = 0; i < plotList.size(); i++) {
			try {
				plotImages[i] = ImageIO.read(plotList.get(i).getImage());
			}
			catch (IOException e) {
			}
		}
		
		return ImageProcess.merge(plotImages);
	}
	
	public void onTextTypeset(ArrayList<BufferedImage> pageImageList) {
		File pageDir = Utils.getChildFile(getDir(), "page");
		if (!pageDir.exists()) {
			pageDir.mkdir();
		}
		
		pages = new File[pageImageList.size()];
		for (int i = 0; i < pageImageList.size(); i++) {
			pages[i] = Utils.getChildFile(pageDir, i + ".png");
			
			try {
				ImageIO.write(pageImageList.get(i), "png", pages[i]);
			}
			catch (IOException e) {
			}
		}
		
		drama.notifyEpisodeDone();
	}
	
	public Chapter getChapter() {
		return new Chapter(name, pages);
	}
}