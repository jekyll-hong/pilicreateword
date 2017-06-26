package org.pilicreateworld.typeset;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class SimpleTypeset {
	public LinkedList<BufferedImage> extract(BufferedImage text) {
		LinkedList<BufferedImage> words = new LinkedList<BufferedImage>();
		
		LinkedList<Line> lines = Line.getLines(text);
		LinkedList<Column> columns = Column.getColumns(text);
		for (int i = 0; i < lines.size(); i++) {
			Line line = lines.get(i);
			
			if (i > 0) {
				Line prevLine = lines.get(i - 1);
				if (line.top() - prevLine.bottom() + 1 > 15) {
					words.add(new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY)); //CRLF
				}
			}
			
			for (int j = 0; j < columns.size(); j++) {
				Column column = columns.get(j);
				
				if (!isBlank(text, line, column)) {
					BufferedImage word = text.getSubimage(column.left(), line.top(), column.length(), line.length());
					words.add(word);
				}
			}
		}
		
		return words;
	}
	
	private boolean isBlank(BufferedImage image, Line line, Column column) {
		int blackPixelCnt = 0;
		
		for (int i = line.top(); i <= line.bottom(); i++) {
			for (int j = column.left(); j <= column.right(); j++) {
				int gray = image.getRGB(j, i) & 0xff;
				if (gray < 160) {
					blackPixelCnt++;
				}
			}
		}
		
		return blackPixelCnt == 0;
	}
	
	public LinkedList<BufferedImage> typeset(LinkedList<BufferedImage> words, TypesetParamter param) {
		int wordWidth = 0;
		int wordHeight = 0;
		
		for (int i = 0; i < words.size(); i++) {
			BufferedImage word = words.get(i);
			
			int width = word.getWidth();
			int height = word.getHeight();
			
			if ((width == 1) && (height == 1)) {
				continue;
			}
			
			if (width > wordWidth) {
				wordWidth = width;
			}
			
			if (height > wordHeight) {
				wordHeight = height;
			}
		}
		
		return paging(words, wordWidth, wordHeight, param);
	}
	
	private LinkedList<BufferedImage> paging(LinkedList<BufferedImage> words, int wordWidth, int wordHeight, TypesetParamter param) {
		LinkedList<BufferedImage> pages = new LinkedList<BufferedImage>();
		int pageWidth = calculatePageWidth(wordWidth, param);
		int pageHeight = calculatePageHeight(wordHeight, param);
		
		boolean isParagraphEnd = true;		
		do {
			BufferedImage page = createPageImage(pageWidth, pageHeight);
			
			int lineCnt = param.get(TypesetParamter.KEY_LINES_PER_PAGE);
			int yOffset = param.get(TypesetParamter.KEY_TOP_MARGIN) + 1;
			
			do {
				int wordCnt = param.get(TypesetParamter.KEY_WORDS_PER_LINE);
				int xOffset = param.get(TypesetParamter.KEY_LEFT_MARGIN) + 1;
				
				if (isParagraphEnd) {
					//indent 2 blank word
					wordCnt -= 2;
					xOffset += (wordWidth + param.get(TypesetParamter.KEY_WORD_SPACE)) * 2;
					
					isParagraphEnd = false;
				}
				
				do {
					BufferedImage word = words.remove(0);
					if ((word.getWidth() == 1) && (word.getHeight() == 1)) {
						isParagraphEnd = true;
						break;
					}
					
					drawOneWord(page, xOffset, yOffset, word);
					
					xOffset += (wordWidth + param.get(TypesetParamter.KEY_WORD_SPACE));
				}
				while ((--wordCnt > 0) && !words.isEmpty());
				
				if (wordCnt < param.get(TypesetParamter.KEY_WORDS_PER_LINE)) {
					yOffset += (wordHeight + param.get(TypesetParamter.KEY_LINE_SPACE));
				}
			}
			while ((--lineCnt > 0) && !words.isEmpty());
			
			pages.add(page);
		}
		while (!words.isEmpty());
		
		return pages;
	}
	
	private int calculatePageWidth(int wordWidth, TypesetParamter param) {
		int pageWidth = param.get(TypesetParamter.KEY_LEFT_MARGIN)
				+ wordWidth * param.get(TypesetParamter.KEY_WORDS_PER_LINE)
				+ param.get(TypesetParamter.KEY_WORD_SPACE) * (param.get(TypesetParamter.KEY_WORDS_PER_LINE) - 1)
				+ param.get(TypesetParamter.KEY_RIGHT_MARGIN);
		
		return pageWidth;
	}
	
	private int calculatePageHeight(int wordHeight, TypesetParamter param) {
		int pageHeight = param.get(TypesetParamter.KEY_TOP_MARGIN)
				+ wordHeight * param.get(TypesetParamter.KEY_LINES_PER_PAGE)
				+ param.get(TypesetParamter.KEY_LINE_SPACE) * (param.get(TypesetParamter.KEY_LINES_PER_PAGE) - 1)
				+ param.get(TypesetParamter.KEY_BOTTOM_MARGIN);
		
		return pageHeight;
	}
	
	private BufferedImage createPageImage(int pageWidth, int pageHeight) {
		BufferedImage page = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_BYTE_GRAY);
		
		for (int i = 0; i < pageHeight; i++) {
			for (int j = 0; j < pageWidth; j++) {
				page.setRGB(j, i, new Color(255, 255, 255).getRGB());
			}
		}
		
		return page;
	}
	
	private void drawOneWord(BufferedImage page, int xOffset, int yOffset, BufferedImage word) {
		for (int i = 0; i < word.getHeight(); i++) {
			for (int j = 0; j < word.getWidth(); j++) {
				int color = word.getRGB(j, i);
				page.setRGB(xOffset + j, yOffset + i, color);
			}
		}
	}
}