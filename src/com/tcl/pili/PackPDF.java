package com.tcl.pili;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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

final class PackPDF implements Runnable {
	private ArrayList<Chapter> chapterList;
	private File pdf;
	private OnPDFPackListener listener;
	
	public PackPDF(ArrayList<Chapter> chapterList, File pdf, OnPDFPackListener listener) {
		this.chapterList = chapterList;
		this.pdf = pdf;
		this.listener = listener;
	}
	
	public void run() {
		try {
			pack(chapterList, pdf);
		} 
		catch (IOException e) {
		}
		
		listener.onPDFPack();
	}
	
	private void pack(ArrayList<Chapter> chapterList, File file) throws IOException {
		PdfDocument pdf = new PdfDocument(new PdfWriter(file.getPath()));
		pdf.getCatalog().setPageMode(PdfName.UseOutlines);
		
		PdfOutline content = pdf.getOutlines(false).addOutline("目录");
		
		Document document = new Document(pdf);
		for (int i = 0; i < chapterList.size(); i++) {
			Chapter chapter = chapterList.get(i);
			
			String destination = String.format("title%d", pdf.getNumberOfPages());
			PdfOutline chapterTitle = content.addOutline(chapter.title);
			chapterTitle.addDestination(PdfDestination.makeDestination(new PdfString(destination)));
			
			for (int j = 0; j < chapter.pages.length; j++) {
				Image image = new Image(ImageDataFactory.create(chapter.pages[j].getPath()));
				
				if (j == 0) {
					image.setDestination(destination);
				}
				pdf.addNewPage(new PageSize(image.getImageWidth(), image.getImageHeight()));
				
				document.add(image);
			}
		}
		document.close();
		
		pdf.close();
	}
}