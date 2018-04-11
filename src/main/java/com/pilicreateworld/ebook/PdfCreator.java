package com.pilicreateworld.ebook;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.navigation.PdfDestination;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

public class PdfCreator {
	private PdfDocument mPdfDocument;
    private Document mDocument;

    public PdfCreator(String path) throws FileNotFoundException {
        mPdfDocument = new PdfDocument(new PdfWriter(path));
        mPdfDocument.getCatalog().setPageMode(PdfName.UseOutlines);

        /**
         * TODO: 页面
         */
        mDocument = new Document(mPdfDocument, PageSize.A4);
        mDocument.setMargins(10.0f, 20.0f, 20.0f, 10.0f);

        /**
         * TODO: 字体
         */
	}

    public void writeChapter(String title, String text) throws IOException {
        /**
         * 大纲中添加章节信息
         */
        String destination = String.format("title%d", mPdfDocument.getNumberOfPages());
        writeOutline(title, destination);

        /**
         *
         */
        Paragraph paragraph = new Paragraph();
        paragraph.setFontSize(24);
        paragraph.setDestination(destination);

        BufferedReader reader = new BufferedReader(new StringReader(text));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            paragraph.add(line);
        }

        mDocument.add(paragraph);
        /*
        mPdfDocument.addNewPage();
        */
    }

    private void writeOutline(String title, String destination) {
        PdfOutline root = mPdfDocument.getOutlines(false);

        PdfOutline leaf = root.addOutline(title);
        leaf.addDestination(
                PdfDestination.makeDestination(new PdfString(destination)));
    }

    public void close() {
	    if (mDocument != null) {
            mDocument.close();
        }

		if (mPdfDocument != null) {
            mPdfDocument.close();
        }
    }
}
