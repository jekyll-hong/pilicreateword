package org.pilicreateworld.parser;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pilicreateworld.job.JobDispatcher;
import org.pilicreateworld.job.JobType;
import org.pilicreateworld.job.runnable.OnImageProcessListener;
import org.pilicreateworld.job.runnable.OnTextTypesetListener;
import org.pilicreateworld.job.runnable.ProcessImage;
import org.pilicreateworld.job.runnable.TypesetText;

class EpisodePage extends WebPage implements Observer, OnImageProcessListener, OnTextTypesetListener {
	private String serialNumber;
	private String name;
	
	private ArrayList<PlotImage> plotList;
	private int downloadCnt;
	
	private File[] pages;
	
	public EpisodePage(String url, File dir, Observer observer, String serialNumber, String name) {
		super(url, dir, observer);
		
		this.serialNumber = serialNumber;
		this.name = name;
		
		plotList = new ArrayList<PlotImage>(10);
		downloadCnt = 0;
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
		Iterator<Element> it = doc.body().getElementsByTag("img").iterator();
		while (it.hasNext()) {
			Element img = it.next();
			
			String value = img.attr("width");
			if (!value.isEmpty() && value.equals("760")) {
				String imageUrl = img.attr("src");
				File imageFile = getImageFile(imageUrl);
				
				plotList.add(new PlotImage(imageUrl, imageFile, this));
			}
		}
		
		if (!plotList.isEmpty()) {
			for (int i = 0; i < plotList.size(); i++) {
				plotList.get(i).download();
			}
		}
		else {
			System.err.print("can not find plot image!\r\n");
		}
	}
	
	private File getImageFile(String url) {
		if (!dir.exists()) {
			dir.mkdir(); 
		}
		
		return Utils.getChildFile(dir, url.substring(url.lastIndexOf("/") + 1));
	}
	
	public void onError() {
		System.err.print("load episode page error!\r\n");
	}
	
	public synchronized void onComplete() {
		if (++downloadCnt < plotList.size()) {
			return;
		}
		
		System.out.print("all plot images of episode " + serialNumber + "." + name + " done!\r\n");
		
		BufferedImage[] images = new BufferedImage[plotList.size()];
		for (int i = 0; i < plotList.size(); i++) {
			images[i] = plotList.get(i).getImage();
		}
		
		JobDispatcher.getInstance().dispatch(JobType.JOB_PROCESS_IMAGE, new ProcessImage(images, this));
	}
	
	public void onImageProcess(BufferedImage image) {
		System.out.print("text image of episode " + serialNumber + "." + name + " done!\r\n");
		
		JobDispatcher.getInstance().dispatch(JobType.JOB_TYPESET_TEXT, new TypesetText(image, "phone", this));
	}
	
	public void onTextTypeset(LinkedList<BufferedImage> pages) {
		File pageDir = Utils.getChildFile(dir, "page");
		if (!pageDir.exists()) {
			pageDir.mkdir();
		}
		
		System.out.print("page images of episode " + serialNumber + "." + name + " done!\r\n");
		savePages(pageDir, pages);
		
		observer.onComplete();
	}
	
	private File[] savePages(File dir, LinkedList<BufferedImage> images) {
		pages = new File[images.size()];
		
		int index = 0;
		while (!images.isEmpty()) {
			pages[index] = Utils.getChildFile(dir, index + ".bmp");
			
			try {
				ImageIO.write(images.remove(), "BMP", pages[index]);
			}
			catch (IOException e) {
			}
			
			index++;
		}
		
		return pages;
	}
	
	public String getTitle() {
		return serialNumber + "." + name;
	}
	
	public File[] getPages() {
		return pages;
	}
}