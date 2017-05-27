package com.tcl.pili;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

final class WebsiteParser extends Thread {
	private static final String sWebsite = "https://pilicreateworld.tw-blog.com";
	
	private File mStorageDir;
	private File mDramaDir;
	private File mEpisodeDir;
	
	private HTTPClient mClient;
	private WebsiteParseListener mListener;
	
	public WebsiteParser(String storageDirPath) {
		mStorageDir = new File(storageDirPath);
		if (!mStorageDir.exists()) {
			mStorageDir.mkdirs();
		}
		
		mClient = new HTTPClient();
		mListener = null;
	}
	
	public void setProxy(String ip, int port) {
		Proxy httpProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
		mClient.setProxy(httpProxy);
	}
	
	public void setListener(WebsiteParseListener listener) {
		mListener = listener;
	}
	
	public void run() {
		try {
			parseWebsiteHTML(sWebsite);
		}
		catch (IOException e) {
			System.err.print("parse website exception, " + e.getMessage() + "\r\n");
		}
	}
	
	private void parseWebsiteHTML(String websiteURL) throws IOException  {
		mClient.connect(websiteURL);
		String mainPageURL = getMainURL(mClient.getInputStream());
		mClient.disconnect();
		
		if (!mainPageURL.isEmpty()) {
			parseMainHTML(mainPageURL);
			mListener.onParseCompleted();
		}
		else {
			System.err.print("parse website error, can not find main url!\r\n");
		}
	}
	
	private String getMainURL(InputStream stream) throws IOException {
		String mainURL = "";
		
		Document doc = Jsoup.parse(stream, null, sWebsite);
		stream.close();
		
		Elements frameList = doc.select("frame");
        for (int i = 0; i < frameList.size(); i++) {
			Element frame = frameList.get(i);
			
			String value = frame.attr("name");
			if (!value.isEmpty() && value.equals("rbottom2")) {
				mainURL = sWebsite + "/" + frame.attr("src");
				break;
			}
		}
        
		return mainURL;
	}
	
	private class DramaInfo {
		public String url;
		public String name;
	}
	
	private void parseMainHTML(String mainURL) throws IOException {
		mClient.connect(mainURL);
		ArrayList<DramaInfo> dramaList = getDramaList(mClient.getInputStream());
		mClient.disconnect();
		
		if (!dramaList.isEmpty()) {
			for (int i = 0; i < dramaList.size(); i++) {
				DramaInfo drama = dramaList.get(i);
				
				mDramaDir = Utils.getChildFile(mStorageDir, drama.name);
				if (!mDramaDir.exists()) {
					mDramaDir.mkdir();
				}
				
				File dramaPDF = Utils.getChildFile(mDramaDir, mDramaDir.getName() + ".pdf");
				if (!dramaPDF.exists() || Utils.OVERRIDE) {
					parseDramaHTML(drama.url);
					mListener.onDrama(mDramaDir);
				}
			}
		}
		else {
			System.err.print("parse website error, can not find any drama!\r\n");
		}
	}
	
	private ArrayList<DramaInfo> getDramaList(InputStream stream) throws IOException {
		ArrayList<DramaInfo> dramaList = new ArrayList<DramaInfo>(100);
		
		Document doc = Jsoup.parse(stream, null, sWebsite);
		stream.close();
		
		Elements anchorList = doc.body().getElementsByTag("a");
		for (int i = 0; i < anchorList.size(); i++) {
			Element anchor = anchorList.get(i);
			
			String value = anchor.attr("id");
			if (!value.isEmpty() && value.startsWith("info")) {
				DramaInfo drama = new DramaInfo();
				drama.url = sWebsite + "/" + anchor.attr("href");
				drama.name = value.substring(4) + "." + getDramaName(anchor.text());
				
				dramaList.add(drama);
			}
		}
		
		dramaList.trimToSize();
		
		Collections.sort(dramaList, new Comparator<DramaInfo>() {
			public int compare(DramaInfo drama1, DramaInfo drama2) {
				String name1 = drama1.name;
				String serialNumber1 = name1.substring(0, name1.indexOf("."));
				
				String name2 = drama2.name;
				String serialNumber2 = name2.substring(0, name2.indexOf("."));
				
				return Integer.parseInt(serialNumber1) - Integer.parseInt(serialNumber2);
			}
		});
		
		return dramaList;
	}
	
	private String getDramaName(String text) {
		text = Utils.convertToUTF16(text);
		
		int leftBracket = text.indexOf("【");
		int rightBracket = text.lastIndexOf("】");
		
		return text.substring(leftBracket + 1, rightBracket);
	}
	
	private class EpisodeInfo {
		public String url;
		public String name;
	}
	
	private void parseDramaHTML(String dramaURL) throws IOException {
		mClient.connect(dramaURL);
		ArrayList<EpisodeInfo> episodeList = getEpisodeList(mClient.getInputStream());
		mClient.disconnect();
		
		if (!episodeList.isEmpty()) {
			for (int i = 0; i < episodeList.size(); i++) {
				EpisodeInfo episode = episodeList.get(i);
				
				mEpisodeDir = Utils.getChildFile(mDramaDir, episode.name);
				if (!mEpisodeDir.exists()) {
					mEpisodeDir.mkdir();
				}
				
				File pageDir = Utils.getChildFile(mEpisodeDir, "page");
				if (!pageDir.exists() || Utils.OVERRIDE) {
					parseEpisodeHTML(episode.url);
					mListener.onEpisode(Utils.getChildFile(mEpisodeDir, mEpisodeDir.getName() + ".png"));
				}
			}
		}
		else {
			System.err.print("parse website error, can not find any episode!\r\n");
		}
	}
	
	private ArrayList<EpisodeInfo> getEpisodeList(InputStream stream) throws IOException {
		ArrayList<EpisodeInfo> episodeList = new ArrayList<EpisodeInfo>(100);
		
		Document doc = Jsoup.parse(stream, null, sWebsite);
		stream.close();
		
		Elements anchorList = doc.body().getElementsByTag("a");
		for (int i = 0; i < anchorList.size(); i++) {
			Element anchor = anchorList.get(i);
			
			String value = anchor.attr("target");
			if (!value.isEmpty() && value.equals("_blank")) {
				EpisodeInfo episode = new EpisodeInfo();
				episode.url = sWebsite + "/PILI/" + anchor.attr("href");
				episode.name = getEpisodeName(anchor.text());
				
				episodeList.add(episode);
			}
		}
		
		episodeList.trimToSize();
		
		Collections.sort(episodeList, new Comparator<EpisodeInfo>() {
			public int compare(EpisodeInfo episode1, EpisodeInfo episode2) {
				String name1 = episode1.name;
				String serialNumber1 = name1.substring(0, name1.indexOf("."));
				
				String name2 = episode2.name;
				String serialNumber2 = name2.substring(0, name2.indexOf("."));
				
				return Integer.parseInt(serialNumber1) - Integer.parseInt(serialNumber2);
			}
		});
		
		return episodeList;
	}
	
	private String getEpisodeName(String text) {
		text = text.replace("\u00a0","");
		return Utils.convertToUTF16(text);
	}
	
	private void parseEpisodeHTML(String episodeURL) throws IOException {
		mClient.connect(episodeURL);
		ArrayList<String> plotImageList = getPlotImageList(mClient.getInputStream());
		mClient.disconnect();
		
		if (!plotImageList.isEmpty()) {
			BufferedImage plotImage = merge(plotImageList);
			
			File plotImageFile = Utils.getChildFile(mEpisodeDir, mEpisodeDir.getName() + ".png");
			if (!plotImageFile.exists()) {
				ImageIO.write(plotImage, "png", plotImageFile);
			}
		}
		else {
			System.err.print("parse website error, can not find any plot image!\r\n");
		}
	}
	
	private ArrayList<String> getPlotImageList(InputStream stream) throws IOException {
		ArrayList<String> plotImageList = new ArrayList<String>(10);
		
		Document doc = Jsoup.parse(stream, null, sWebsite);
		stream.close();
		
		Elements imgList = doc.body().getElementsByTag("img");
		for (int i = 0; i < imgList.size(); i++) {
			Element img = imgList.get(i);
			
			String value = img.attr("width");
			if (!value.isEmpty() && value.equals("760")) {
				String plotURL = img.attr("src");
				plotImageList.add(plotURL);
			}
		}
		
		plotImageList.trimToSize();
		
		return plotImageList;
	}
	
	private BufferedImage merge(ArrayList<String> urlList) throws IOException {
		ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>(urlList.size());
		for (int i = 0; i < urlList.size(); i++) {
			File imageFile = Utils.getChildFile(mEpisodeDir, i + ".gif");
			if (!imageFile.exists()) {
				tryDownloadImage(urlList.get(i), imageFile);
			}
			
			BufferedImage image = ImageIO.read(imageFile);
			imageList.add(image);
		}
		
		int width = getPlotImageWidth(imageList);
		int height = getPlotImageHeight(imageList);
		BufferedImage fullImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		
		Graphics2D graphics = fullImage.createGraphics();
		int yOffset = 0;
		for (int i = 0; i < imageList.size(); i++) {
			BufferedImage image = imageList.get(i);
			graphics.drawImage(image, null, 0, yOffset);
			yOffset += image.getHeight();
		}
		
		return fullImage;
	}
	
	private void tryDownloadImage(String url, File file) throws IOException {
		final int maxRetryCnt = 10;
		int retryCnt = 0;
		
		do {
			if (retryCnt > 0) {
				try {
					Thread.sleep(2000);
				} 
				catch (InterruptedException e) {
				}
			}
			
			try {
				if (downloadImage(url, file)) {
					System.out.print(url + " downloaded\r\n");
					break;
				}
			}
			catch (IOException e) {
			}
		}
		while (++retryCnt < maxRetryCnt);
		
		if (retryCnt == maxRetryCnt) {
			throw new IOException();
		}
	}
	
	private boolean downloadImage(String url, File file) throws IOException {
		mClient.connect(url);
		
		InputStream in = mClient.getInputStream();
		if (in == null) {
			return false;
		}
		OutputStream out = new FileOutputStream(file);
		
		byte[] buf = new byte[1024];
		while (true) {
			int ret = in.read(buf);
			if (ret < 0) {
				break;
			}
			
			out.write(buf, 0, ret);
		}
		
		in.close();
		out.close();
		
		mClient.disconnect();
		
		return true;
	}
	
	private int getPlotImageWidth(ArrayList<BufferedImage> plotImageList) {
		return plotImageList.get(0).getWidth();
	}
	
	private int getPlotImageHeight(ArrayList<BufferedImage> plotImageList) {
		int height = 0;
		
		for (int i = 0; i < plotImageList.size(); i++) {
			height += plotImageList.get(i).getHeight();
		}
		
		return height;
	}
}
