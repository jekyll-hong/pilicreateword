package org.pilicreateworld.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pilicreateworld.job.JobDispatcher;
import org.pilicreateworld.job.JobType;
import org.pilicreateworld.job.runnable.OnPDFPackListener;
import org.pilicreateworld.job.runnable.PackPDF;
import org.pilicreateworld.pdf.Chapter;

class DramaPage extends WebPage implements Observer, OnPDFPackListener {
	private String serialNumber;
	private String name;
	
	private ArrayList<EpisodePage> episodeList;
	private int typesetCnt;
	
	public DramaPage(String url, File dir, Observer observer, String serialNumber, String name) {
		super(url, dir, observer);
		
		this.serialNumber = serialNumber;
		this.name = name;
		
		episodeList = new ArrayList<EpisodePage>(60);
		typesetCnt = 0;
	}
	
	public int getSerialNumber() {
		try {
			return Integer.parseInt(serialNumber);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}
	
	public void onPageLoad(Document doc) {
		Iterator<Element> it = doc.body().getElementsByTag("a").iterator();
		while (it.hasNext()) {
			Element anchor = it.next();
			
			String value = anchor.attr("target");
			if (value != null && value.equals("_blank")) {
				String text = anchor.text().replace("\u00a0", "");
				text = Utils.convertToUTF16(text);
				
				String episodePageUrl = getEpisodePageUrl(anchor.attr("href"));
				String episodeSerialNumber = getEpisodeSerialNumber(text);
				String episodeName = getEpisodeName(text);
				File episodeDir = getEpisodeDir(text);
				
				episodeList.add(new EpisodePage(episodePageUrl, episodeDir, this, episodeSerialNumber, episodeName));
			}
		}
		
		if (!episodeList.isEmpty()) {
			sortEpisodeBySerialNumber();
			
			for (int i = 0; i < episodeList.size(); i++) {
				episodeList.get(i).load();
			}
		}
		else {
			System.err.print("can not find episode information!\r\n");
		}
	}
	
	private String getEpisodePageUrl(String href) {
		return url.substring(0, url.lastIndexOf("/")) + "/" + href;
	}
	
	private String getEpisodeSerialNumber(String text) {
		return text.substring(0, text.indexOf("."));
	}
	
	private String getEpisodeName(String text) {
		return text.substring(text.indexOf(".") + 1);
	}
	
	private File getEpisodeDir(String text) {
		if (!dir.exists()) {
			dir.mkdir(); 
		}
		
		return Utils.getChildFile(dir, text);
	}
	
	private void sortEpisodeBySerialNumber() {
		Collections.sort(episodeList, new Comparator<EpisodePage>() {
			public int compare(EpisodePage episode1, EpisodePage episode2) {
				return episode1.getSerialNumber() - episode2.getSerialNumber();
			}
		});
	}
	
	public void onError() {
		System.err.print("load drama page error!\r\n");
	}
	
	public synchronized void onComplete() {
		if (++typesetCnt < episodeList.size()) {
			return;
		}
		
		System.out.print("all episodes of " + serialNumber + "." + name + " done!\r\n");
		
		Chapter[] chapters = new Chapter[episodeList.size()];
		for (int i = 0; i < episodeList.size(); i++) {
			EpisodePage episode = episodeList.get(i);
			chapters[i] = new Chapter(episode.getTitle(), episode.getPages());
		}
		
		JobDispatcher.getInstance().dispatch(JobType.JOB_PACK_PDF, new PackPDF(chapters, getPDFFilePath(), this));
	}
	
	private String getPDFFilePath() {
		File pdf = Utils.getChildFile(dir, serialNumber + "." + name + ".pdf");
		return pdf.getPath();
	}
	
	public void onPDFPack(File file) {
		System.out.print(serialNumber + "." + name + ".pdf done!\r\n");
		
		observer.onComplete();
	}
}