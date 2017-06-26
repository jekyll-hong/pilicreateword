package org.pilicreateworld.job.runnable;

import org.jsoup.nodes.Document;

public interface OnPageLoadListener {
	public void onPageLoad(Document doc);
	public void onError();
}