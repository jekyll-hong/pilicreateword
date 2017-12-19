package com.pilicreateworld.ebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.navigation.PdfDestination;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.pilicreateworld.image.Page;

public class PdfCreator {
	private String mOutputDir;

	private PdfDocument mPdfDocument;
	private Document mRoot;

	public PdfCreator(String outputDir) {
		mOutputDir = outputDir;
	}

    public void open(String fileName) throws FileNotFoundException {
        PdfWriter writer = new PdfWriter(mOutputDir + "/" + fileName + ".pdf");
        //writer settings

        mPdfDocument = new PdfDocument(writer);
        mPdfDocument.getCatalog().setPageMode(PdfName.UseOutlines);

        mRoot = new Document(mPdfDocument);
        mRoot.setMargins(0.0f, 0.0f, 0.0f, 0.0f);
    }

    public void writeChapter(String title, List<Page> pageList) throws IOException {
		String destination = String.format("title%d", mPdfDocument.getNumberOfPages());

        /**
         * 在大纲中添加章节目录
         */
		PdfOutline chapterTitle = mPdfDocument.getOutlines(false).addOutline(title);
		chapterTitle.addDestination(PdfDestination.makeDestination(new PdfString(destination)));

		boolean isFirstPage = true;
		for (Page page : pageList) {
			Image image = new Image(ImageDataFactory.create(page.getImage(), null));

			if (isFirstPage) {
                /**
                 * 关联目录中的跳转页码
                 */
				image.setDestination(destination);
                isFirstPage = false;
			}

            mPdfDocument.addNewPage(new PageSize(image.getImageWidth(), image.getImageHeight()));
            mRoot.add(image);
		}

        mRoot.flush();
    }

    public void close() {
	    if (mRoot != null) {
            mRoot.close();
        }

		if (mPdfDocument != null) {
            mPdfDocument.close();
        }
    }
}
