package com.tcl.pili;

import org.jsoup.nodes.Document;

interface OnPageLoadListener {
	public void onPageLoad(Document doc);
	public void onError();
}