package com.pilicreateworld.ebook;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.navigation.PdfDestination;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.pilicreateworld.Settings;
import com.pilicreateworld.image.Page;
import com.pilicreateworld.image.Text;
import com.pilicreateworld.image.Word;

public class PdfCreator {
	private PdfDocument mPdfDocument;
    private Document mDocument;

    private Page.Builder mPageBuilder;

    public PdfCreator(String path) throws FileNotFoundException {
        mPdfDocument = new PdfDocument(new PdfWriter(path));
        mPdfDocument.getCatalog().setPageMode(PdfName.UseOutlines);

        if (Settings.getInstance().getTargetDevice().equals("nexus5")) {
            /**
             * 参数是调出的，没有任何依据
             */
            mPdfDocument.setDefaultPageSize(new PageSize(360.0f, 480.0f));

            mPageBuilder = new Page.Builder();
            mPageBuilder.setPageSize(360, 480);
            mPageBuilder.setMargins(16, 32, 16, 32);
            mPageBuilder.setIndent(2);
            mPageBuilder.setLineSpacing(5);
            mPageBuilder.setWordSpacing(3);
            mPageBuilder.setWordSize(16, 16);
        }
        else {
            throw new IllegalArgumentException("unsupported device");
        }

        mDocument = new Document(mPdfDocument);
        mDocument.setMargins(0.0f, 0.0f, 0.0f, 0.0f);
	}

    public void writeChapter(String title, Text text) throws IOException {
        /**
         * 大纲中添加章节信息
         */
        String destination = String.format("title%d", mPdfDocument.getNumberOfPages());
        writeOutline(title, destination);

        /**
         * 章节的第一页，起始缩进两字符
         */
        Page page = mPageBuilder.build();
        page.writeSpace();
        page.writeSpace();

        while (!text.isEof()) {
            Word word = text.read();

            if (!page.write(word)) {
                boolean isParagraphEnd = page.isParagraphEnd();
                writePage(page, destination);
                destination = "";

                page = mPageBuilder.build();
                if (isParagraphEnd) {
                    /**
                     * 上一页的末尾恰好是段落结束，这一页起始缩进两字符
                     */
                    page.writeSpace();
                    page.writeSpace();
                }
                page.write(word);
            }
        }

        /**
         * 最后一页，结束
         */
        writePage(page, destination);
    }

    private void writeOutline(String title, String destination) {
        PdfOutline root = mPdfDocument.getOutlines(false);

        PdfOutline leaf = root.addOutline(title);
        leaf.addDestination(
                PdfDestination.makeDestination(new PdfString(destination)));
    }

    private void writePage(Page page, String destination) throws IOException {
        mPdfDocument.addNewPage();

        Image imgElement = new Image(page.getImageData());
        if (!destination.isEmpty()) {
            /**
             * 关联大纲中的目录
             */
            imgElement.setDestination(destination);
        }

        mDocument.add(imgElement);
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
