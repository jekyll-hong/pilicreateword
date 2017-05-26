package com.tcl.pili;

import java.io.File;

class WebsiteParseListenerImpl implements WebsiteParseListener {
	private MessageLooper mLooper;
	
	public WebsiteParseListenerImpl(MessageLooper looper) {
		mLooper = looper;
	}
	
	public void onDrama(File dir) {
		System.out.print(dir.getName() + "\r\n");
		
		Message msg = new Message(Message.MSG_PACK_PDF, dir);
		mLooper.post(msg);
	}
	
	public void onEpisode(File file) {
		System.out.print("  " + file.getName() + "\r\n");
		
		Message msg = new Message(Message.MSG_MAKE_PAGES, file);
		mLooper.post(msg);
	}
	
	public void onParseCompleted() {
		Message msg = new Message(Message.MSG_COMPLETE);
		mLooper.post(msg);
	}
}
