package com.tcl.pili;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;

import javax.imageio.ImageIO;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfOutline;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.navigation.PdfDestination;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;

final class PDFPacker extends MessageHandlerImpl {
	public PDFPacker(Executor executor) {
		super(executor);
	}
	
	public interface OnPackCompleteListener {
		public void onPackComplete(File pdf);
	}
	
	static public class Parameter {
		public ArrayList<BufferedImage> pageImageList;
		public File pdf;
		public OnPackCompleteListener listener;
		
		public Parameter(ArrayList<BufferedImage> pageImageList, File pdf, OnPackCompleteListener listener) {
			this.pageImageList = pageImageList;
			this.pdf = pdf;
			this.listener = listener;
		}
	}
	
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case Message.MSG_PACK_PDF: {
				Parameter param = (Parameter)msg.obj;
				execute(new PackJob(param));
				break;
			}
			default: {
				return false;
			}
		}
		
		return true;
	}
	
	private class PackJob implements Runnable {
		private Parameter param;
		
		public PackJob(Parameter param) {
			this.param = param;
		}
		
		public void run() {
			try {
				PdfDocument pdf = new PdfDocument(new PdfWriter(param.pdf.getPath()));
				
				Document document = new Document(pdf);
				for (int i = 0; i < param.pageImageList.size(); i++) {
					byte[] imageData = getBMPData(param.pageImageList.get(i));
					Image image = new Image(ImageDataFactory.create(imageData));
					
					pdf.addNewPage(new PageSize(image.getImageWidth(), image.getImageHeight()));
					document.add(image);
				}
				document.close();
				
				pdf.close();
			}
			catch (IOException e) {
			}
			
			param.listener.onPackComplete(param.pdf);
		}
	}
	
	private byte[] getBMPData(BufferedImage image) throws IOException {
		byte[] data;
		
		ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
		ImageIO.write(image, "BMP", imageOut);
		data = imageOut.toByteArray();
		imageOut.close();
		
		return data;
	}
	
	private void onPackPDF(File dramaDir, File dramaPDF) throws IOException {
		pack(dramaPDF.getPath(), prepareChapterIndex(dramaDir));
	}
	
	private class ChapterIndex {
		public String name;
		public int number;
		
		public File[] pages;
	}
	
	private ArrayList<ChapterIndex> prepareChapterIndex(File dramaDir) {
		ArrayList<ChapterIndex> indexList = new ArrayList<ChapterIndex>();
		int pageNumber = 0;
		
		List<File> episodeList = enumerateEpisode(dramaDir);
		for (int i = 0; i < episodeList.size(); i++) {
			File episode = episodeList.get(i);
			List<File> pageList = enumeratePage(Utils.getChildFile(episode, "page"));
			
			ChapterIndex index = new ChapterIndex();
			index.name = episode.getName();
			index.number = pageNumber;
			index.pages = pageList.toArray(new File[pageList.size()]);
			indexList.add(index);

			pageNumber += pageList.size();
		}
		
		return indexList;
	}
	
	private List<File> enumerateEpisode(File dramaDir) {
		File[] episodes = dramaDir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		
		List<File> episodeList = Arrays.asList(episodes);
		
		Collections.sort(episodeList, new Comparator<File>() {
			public int compare(File episode1, File episode2) {
				String name1 = episode1.getName();
				String serialNumber1 = name1.substring(0, name1.indexOf("."));
				
				String name2 = episode2.getName();
				String serialNumber2 = name2.substring(0, name2.indexOf("."));
				
				int n1, n2;
				try {
					try {
						n1 = Integer.parseInt(serialNumber1);
					}
					catch (NumberFormatException e) {
						return -1;
					}
					
					n2 = Integer.parseInt(serialNumber2);
				}
				catch (NumberFormatException e) {
					return 1;
				}
				
				return n1 - n2;
			}
		});
		
		return episodeList;
	}
	
	private List<File> enumeratePage(File pageDir) {
		File[] pages = pageDir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".png");
			}
		});
		
		List<File> pageList = Arrays.asList(pages);
		
		Collections.sort(pageList, new Comparator<File>() {
			public int compare(File page1, File page2) {
				String name1 = page1.getName();
				String serialNumber1 = name1.substring(0, name1.indexOf("."));
				
				String name2 = page2.getName();
				String serialNumber2 = name2.substring(0, name2.indexOf("."));
				
				return Integer.parseInt(serialNumber1) - Integer.parseInt(serialNumber2);
			}
		});
		
		return pageList;
	}
	
	private void pack(String path, ArrayList<ChapterIndex> indexList) throws IOException {
		PdfDocument pdf = new PdfDocument(new PdfWriter(path));
		pdf.getCatalog().setPageMode(PdfName.UseOutlines);
		
		Document document = new Document(pdf);
		
		PdfOutline root = pdf.getOutlines(false);
		PdfOutline content = root.addOutline("目录");
		
		for (int i = 0; i < indexList.size(); i++) {
			ChapterIndex index = indexList.get(i);
			
			String destination = String.format("title%d", index.number);
			PdfOutline episode = content.addOutline(index.name);
			episode.addDestination(PdfDestination.makeDestination(new PdfString(destination)));
			
			File[] pageOfChapter = index.pages;
			for (int j = 0; j < pageOfChapter.length; j++) {
				String pageImagePath = pageOfChapter[j].getPath();
				Image image = new Image(ImageDataFactory.create(pageImagePath));
				
				if (j == 0) {
					image.setDestination(destination);
				}
				
				pdf.addNewPage(new PageSize(image.getImageWidth(), image.getImageHeight()));
				document.add(image);
			}
		}
		
		document.close();			
	}
}
