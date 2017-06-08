package com.tcl.pili;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

final class Drama implements OnPDFPackListener {
	private Pili pili;
	private String url;
	private String name;
	
	private ArrayList<Episode> episodeList;
	private int typesettedCnt;
	
	private File pdf;
	
	public Drama(Pili pili, String url, String name) {
		this.pili = pili;
		this.url = url;
		this.name = name;
		
		episodeList = new ArrayList<Episode>(100);
		typesettedCnt = 0;
		
		pdf = Utils.getChildFile(getDir(), name + ".pdf");
	}
	
	public File getDir() {
		File dramaDir = Utils.getChildFile(pili.getDir(), name);
		if (!dramaDir.exists()) {
			dramaDir.mkdir(); 
		}
		
		return dramaDir;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getName() {
		return name;
	}
	
	public void addEpisode(Episode episode) {
		episodeList.add(episode);
	}
	
	public int getEpisodeCount() {
		return episodeList.size();
	}
	
	public Episode getEpisode(int index) {
		if (index >= episodeList.size()) {
			return null;
		}
		
		return episodeList.get(index);
	}
	
	public void sortEpisodeBySerialNumber() {
		Collections.sort(episodeList, new Comparator<Episode>() {
			public int compare(Episode episode1, Episode episode2) {
				int serialNumber1 = Utils.getSerialNumber(episode1.getName());
				int serialNumber2 = Utils.getSerialNumber(episode2.getName());
				return serialNumber1 - serialNumber2;
			}
		});
	}
	
	public synchronized void notifyEpisodeDone() {
		if (++typesettedCnt == episodeList.size()) {
			System.out.print("all episode in " + name + " are done!\r\n");
			
			Message msg = new Message(Message.MSG_PACK_PDF, new PackPDF(getChapter(), pdf, this));
			MessageLooper.getInstance().post(msg);
		}
	}
	
	private ArrayList<Chapter> getChapter() {
		ArrayList<Chapter> chapterList = new ArrayList<Chapter>();
		
		for (int i = 0; i < episodeList.size(); i++) {
			chapterList.add(episodeList.get(i).getChapter());
		}
		
		return chapterList;
	}
	
	public void onPDFPack() {
		pili.notifyDramaDone();
	}
}