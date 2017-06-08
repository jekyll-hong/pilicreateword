package com.tcl.pili;

class Message {
	public static final int MSG_COMPLETE = 0;
	public static final int MSG_LOAD_WEBPAGE = 1;
	public static final int MSG_DOWNLOAD_IMAGE = 2;
	public static final int MSG_TYPESET_TEXT = 3;
	public static final int MSG_PACK_PDF = 4;
	
	public int what;
	public Object obj;
	
	public Message(int what) {
		this.what = what;
		this.obj = null;
	}
	
	public Message(int what, Object obj) {
		this.what = what;
		this.obj = obj;
	}
}