package org.pilicreateworld.parser;

import java.io.File;

import org.jsoup.nodes.Document;
import org.pilicreateworld.job.JobDispatcher;
import org.pilicreateworld.job.JobType;
import org.pilicreateworld.job.runnable.LoadPage;
import org.pilicreateworld.job.runnable.OnPageLoadListener;

abstract class WebPage implements OnPageLoadListener {
	protected String url;
	protected File dir;
	protected Observer observer;
	
	public WebPage(String url, File dir, Observer observer) {
		this.url = url;
		this.dir = dir;
		this.observer = observer;
	}
	
	public void load() {
		JobDispatcher.getInstance().dispatch(JobType.JOB_LOAD_WEBPAGE, new LoadPage(url, this));
	}
	
	public abstract void onPageLoad(Document doc);
	
	public abstract void onError();
}