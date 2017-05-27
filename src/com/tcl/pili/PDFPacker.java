package com.tcl.pili;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

final class PDFPacker implements MessageHandler {
	public boolean handleMessage(Message msg) {
		if (msg.what == Message.MSG_PACK_PDF) {
			File dramaDir = (File)msg.obj;
			
			File dramaPDF = Utils.getChildFile(dramaDir, dramaDir.getName() + ".pdf");
			
			try {
				onPackPDF(dramaDir, dramaPDF);
			}
			catch (IOException e) {
				System.err.print("pack pdf error, exception " + e.getMessage() + "\r\n");
			}

			return true;
		}
		else {
			return false;
		}
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
				
				return Integer.parseInt(serialNumber1) - Integer.parseInt(serialNumber2);
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
