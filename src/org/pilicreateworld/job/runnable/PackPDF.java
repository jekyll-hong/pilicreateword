package org.pilicreateworld.job.runnable;

import java.io.File;
import java.io.IOException;

import org.pilicreateworld.pdf.Chapter;
import org.pilicreateworld.pdf.SimplePDF;

public class PackPDF implements Runnable {
	private Chapter[] chapters;
	private String path;
	private OnPDFPackListener listener;
	
	public PackPDF(Chapter[] chapters, String path, OnPDFPackListener listener) {
		this.chapters = chapters;
		this.path = path;
		this.listener = listener;
	}
	
	public void run() {
		SimplePDF module = new SimplePDF();
		
		try {
			module.pack(chapters, path);
		}
		catch (IOException e) {
			System.err.print("SimplePDF pack throw exception!\r\n");
		}
		
		listener.onPDFPack(new File(path));
	}
}