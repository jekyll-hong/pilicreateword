package com.tcl.pili;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

final class Pili {
	private Context ctx;
	private String url;
	
	private ArrayList<Drama> dramaList;
	private int packCnt;
	
	public Pili(Context ctx, String url) {
		this.ctx = ctx;
		this.url = url;
		
		dramaList = new ArrayList<Drama>(100);
		packCnt = 0;
	}
	
	public File getDir() {
		File piliDir = Utils.getChildFile(ctx.getDir(), "霹靂系列");
		if (!piliDir.exists()) {
			piliDir.mkdir(); 
		}
		
		return piliDir;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void addDrama(Drama drama) {
		dramaList.add(drama);
	}
	
	public int getDramaCount() {
		return dramaList.size();
	}
	
	public Drama getDrama(int index) {
		if (index >= dramaList.size()) {
			return null;
		}
		
		return dramaList.get(index);
	}
	
	public void sortDramaBySerialNumber() {
		Collections.sort(dramaList, new Comparator<Drama>() {
			public int compare(Drama drama1, Drama drama2) {
				int serialNumber1 = Utils.getSerialNumber(drama1.getName());
				int serialNumber2 = Utils.getSerialNumber(drama2.getName());
				return serialNumber1 - serialNumber2;
			}
		});
	}
	
	public synchronized void notifyDramaDone() {
		if (++packCnt == dramaList.size()) {
			System.out.print("all dramas done!\r\n");
			
			Message msg = new Message(Message.MSG_COMPLETE);
			MessageLooper.getInstance().post(msg);
		}
	}
}