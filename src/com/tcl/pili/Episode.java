package com.tcl.pili;

import java.awt.Color;
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
	
	private File[] pages;
	private OCRInterface ocr;
	
	public Episode(Drama drama, String url, String name) {
		this.drama = drama;
		this.url = url;
		this.name = name;
		
		plotList = new ArrayList<Plot>(10);
		downloadedCnt = 0;
		
		ocr = OCRFactory.create(OCRFactory.OCR_BAIDU);
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
			System.out.print("all plots in " + name + " done, next step is typeset!\r\n");
			Message msg = new Message(Message.MSG_TYPESET_TEXT, new TypesetText(getTypesetImage(), this));
			MessageLooper.getInstance().post(msg);
		}
	}
	
	private BufferedImage getTypesetImage() {
		BufferedImage typesetImage = preprocess(getPlotImage());
		
		return typesetImage.getSubimage(0, 30, typesetImage.getWidth(), typesetImage.getHeight() - 31);
	}
	
	private BufferedImage getPlotImage(){
		BufferedImage[] plotImages = new BufferedImage[plotList.size()];
		
		for (int i = 0; i < plotList.size(); i++) {
			try {
				plotImages[i] = plotList.get(i).getImage();
			}
			catch (IOException e) {
			}
		}
		
		return mergeIntoGray(plotImages);
	}
	
	private BufferedImage mergeIntoGray(BufferedImage[] src) {
		BufferedImage dst = createMergedGrayImage(src);
		
		int yOffset = 0;
		for (int n = 0; n < src.length; n++) {
			BufferedImage part =  src[n];
			
			for (int i = 0; i < part.getHeight(); i++) {
				for (int j = 0; j < part.getWidth(); j++) {
					int gray = rgb2gray(part.getRGB(j, i));
					dst.setRGB(j, yOffset + i, new Color(gray, gray, gray).getRGB());
				}
			}
			
			yOffset += part.getHeight();
		}
		
		return dst;
	}
	
	private BufferedImage createMergedGrayImage(BufferedImage[] src) {
		int height = 0;
		for (int i = 0; i < src.length; i++) {
			height += src[i].getHeight();
		}
		
		return new BufferedImage(src[0].getWidth(), height, BufferedImage.TYPE_BYTE_GRAY);
	}
	
	private int rgb2gray(int color) {
		int red = (color >> 16) & 0xff;
		int green = (color >> 8) & 0xff;
		int blue = color & 0xff;
		
		int gray = (int)(0.2989 * (double)red + 0.5870 * (double)green + 0.1140 * (double)blue);
		if (gray < 0) {
			gray = 0;
		}
		else if (gray > 255) {
			gray = 255;
		}
		
		return gray;
	}
	
	private BufferedImage preprocess(BufferedImage src) {
		BufferedImage tmp = src;
		/*
		try {
			ImageIO.write(tmp, "png", Utils.getChildFile(getDir(), "ori.png"));
		}
		catch (IOException e) {
		}
		*/
		
		tmp = ImageProcess.sharpen(tmp);
		/*
		try {
			ImageIO.write(tmp, "png", Utils.getChildFile(getDir(), "sharpen.png"));
		}
		catch (IOException e) {
		}
		*/
		
		tmp = ImageProcess.enhanceContrast(tmp);
		/*
		try {
			ImageIO.write(tmp, "png", Utils.getChildFile(getDir(), "enhance.png"));
		}
		catch (IOException e) {
		}
		*/
		
		return whiteBackground(tmp);
	}
	
	private BufferedImage whiteBackground(BufferedImage src) {
		for (int i = 0; i < src.getHeight(); i++) {
			for (int j = 0; j < src.getWidth(); j++) {
				int gray = src.getRGB(j, i) & 0xff;
				if (gray >= 220) {
					gray = 255;
				}
				
				src.setRGB(j, i, new Color(gray, gray, gray).getRGB());
			}
		}
		
		return src;
	}
	
	public void onTextTypeset(ArrayList<BufferedImage> pageImageList) {
		File pageDir = Utils.getChildFile(getDir(), "page");
		if (!pageDir.exists()) {
			pageDir.mkdir();
		}
		
		pages = new File[pageImageList.size()];
		for (int i = 0; i < pageImageList.size(); i++) {
			pages[i] = Utils.getChildFile(pageDir, i + ".bmp");
			
			try {
				ImageIO.write(pageImageList.get(i), "BMP", pages[i]);
			}
			catch (IOException e) {
			}
			
			if (Utils.ENABLE_OCR) {
				File text = Utils.getChildFile(pageDir, i + ".txt");
				ocr.process(pages[i], text);
			}
		}
		
		System.out.print("all pages in " + name + " done!\r\n");
		drama.notifyEpisodeDone();
	}
	
	public Chapter getChapter() {
		return new Chapter(name, pages);
	}
}