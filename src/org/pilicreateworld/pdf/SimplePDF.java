package org.pilicreateworld.pdf;

import java.io.IOException;

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

public class SimplePDF {
	public void pack(Chapter[] chapters, String path) throws IOException {
		PdfDocument pdf = new PdfDocument(new PdfWriter(path));
		pdf.getCatalog().setPageMode(PdfName.UseOutlines);
		
		PdfOutline content = pdf.getOutlines(false).addOutline("目录");
		
		Document document = new Document(pdf);
		for (int i = 0; i < chapters.length; i++) {
			Chapter chapter = chapters[i];
			
			String destination = String.format("title%d", pdf.getNumberOfPages());
			PdfOutline chapterTitle = content.addOutline(chapter.title());
			chapterTitle.addDestination(PdfDestination.makeDestination(new PdfString(destination)));
			
			for (int j = 0; j < chapter.getPageCount(); j++) {
				Image image = new Image(ImageDataFactory.create(chapter.getPagePath(j)));
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