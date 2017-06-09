package com.tcl.pili;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

final class TypesetMethod implements TypesetMethodInterface {
	private static final int sBackgroundGrayThreshold = 160;
	
	private TypesetParamter param;
	
	public void setParameter(TypesetParamter param) {
		this.param = param;
	}
	
	public ArrayList<BufferedImage> typeset(BufferedImage src) {
		ArrayList<BufferedImage> wordList = getWordInImage(src);
		
		int[] avgWordSize = getAverageWordSize(wordList);
		return paging(avgWordSize[0], avgWordSize[1], wordList);
	}
	
	private int[] getAverageWordSize(ArrayList<BufferedImage> wordList) {
		int totalWidth = 0;
		int totalHeight = 0;
		
		for (int i = 0; i < wordList.size(); i++) {
			BufferedImage word = wordList.get(i);
			
			totalWidth += word.getWidth();
			totalHeight += word.getHeight();
		}
		
		return new int[] {totalWidth / wordList.size(), totalHeight / wordList.size()};
	}
	
	private class Line {
		public int top;
		public int bottom;
		
		public Line(int top, int bottom) {
			this.top = top;
			this.bottom = bottom;
		}
		
		public int length() {
			return bottom - top + 1;
		}
	}
	
	private class Column {
		public int left;
		public int right;
		
		public Column(int left, int right) {
			this.left = left;
			this.right = right;
		}
		
		public int length() {
			return right - left + 1;
		}
	}
	
	private ArrayList<BufferedImage> getWordInImage(BufferedImage image) {
		ArrayList<BufferedImage> wordList = new ArrayList<BufferedImage>();
		
		ArrayList<Line> lines = getLines(image);
		ArrayList<Column> columns = getColumns(image);
		for (int i = 0; i < lines.size(); i++) {
			Line line = lines.get(i);
			
			if (i > 0) {
				Line prevLine = lines.get(i - 1);
				if (line.top - prevLine.bottom + 1 > 10) {
					//CRLF
					wordList.add(new BufferedImage(1, 1, image.getType())); 
				}
			}
			
			for (int j = 0; j < columns.size(); j++) {
				Column column = columns.get(j);
				
				if (!isBlank(image, line, column)) {
					BufferedImage word = image.getSubimage(column.left, line.top, column.length(), line.length());
					wordList.add(word);
				}
			}
		}
		
		return wordList;
	}
	
	private boolean isBlank(BufferedImage image, Line line, Column column) {
		int wordPixels = 0;
		
		for (int i = line.top; i <= line.bottom; i++) {
			for (int j = column.left; j <= column.right; j++) {
				int gray = image.getRGB(j, i) & 0xff;
				if (gray < sBackgroundGrayThreshold) {
					wordPixels++;
				}
			}
		}
		
		return wordPixels == 0;
	}
	
	private ArrayList<Line> getLines(BufferedImage image) {
		ArrayList<Line> lines = new ArrayList<Line>();
		
		int yOffset = 0;
		while (yOffset < image.getHeight()) {
			Line line = getOneLine(image, yOffset);
			if (line == null) {
				break;
			}
			
			lines.add(line);
			yOffset = line.bottom + 1;
		}
		
		return lines;
	}
	
	private Line getOneLine(BufferedImage image, int pos) {
		int top = -1;
		int bottom = -1;
		
		for (int i = pos; i < image.getHeight(); i++) {
			int wordPixels = 0;
			for (int j = 0; j < image.getWidth(); j++) {
				int gray = image.getRGB(j, i) & 0xff;
				if (gray < sBackgroundGrayThreshold) {
					wordPixels++;
				}
			}
			
			if (wordPixels < 3) {
				if (top != -1) {
					return new Line(top, bottom);
				}
			}
			else {
				if (top == -1) {
					top = i;
					bottom = top;
				}
				else {
					bottom++;
				}
			}
		}
		
		return null;
	}
	
	private ArrayList<Column> getColumns(BufferedImage image) {
		ArrayList<Column> columns = new ArrayList<Column>();
		
		int xOffset = 0;
		while (xOffset < image.getWidth()) {
			Column column = getOneColumn(image, xOffset);
			if (column == null) {
				break;
			}
			
			columns.add(column);	
			xOffset = column.right + 1;
		}
		
		return columns;
	}
	
	private Column getOneColumn(BufferedImage image, int pos) {
		int left = -1;
		int right = -1;
		
		for (int i = pos; i < image.getWidth(); i++) {
			int wordPixels = 0;
			for (int j = 0; j < image.getHeight(); j++) {
				int gray = image.getRGB(i, j) & 0xff;
				if (gray < sBackgroundGrayThreshold) {
					wordPixels++;
				}
			}
			
			if (wordPixels < 100) {
				if (left != -1) {
					return new Column(left, right);
				}
			}
			else {
				if (left == -1) {
					left = i;
					right = left;
				}
				else {
					right++;
				}
			}
		}
		
		return null;
	}
	
	private ArrayList<BufferedImage> paging(int avgWordWidth, int avgWordHeight, ArrayList<BufferedImage> wordList) {
		ArrayList<BufferedImage> pageImageList = new ArrayList<BufferedImage>();
		
		boolean isParagraphEnd = true;		
		do {
			BufferedImage pageImage = createPageImage(calculatePageWidth(avgWordWidth), calculatePageHeight(avgWordHeight));
			
			int lineCnt = param.get(TypesetParamter.KEY_LINES_PER_PAGE);
			int yOffset = param.get(TypesetParamter.KEY_TOP_MARGIN) + 1;
			
			do {
				int wordCnt = param.get(TypesetParamter.KEY_WORDS_PER_LINE);
				int xOffset = param.get(TypesetParamter.KEY_LEFT_MARGIN) + 1;
				
				if (isParagraphEnd) {
					//indent 2 blank word
					wordCnt -= 2;
					xOffset += (avgWordWidth + param.get(TypesetParamter.KEY_WORD_SPACE)) * 2;
					
					isParagraphEnd = false;
				}
				
				Graphics2D graphics = pageImage.createGraphics();
				do {
					BufferedImage word = wordList.remove(0);
					if ((word.getWidth() == 1) && (word.getHeight() == 1)) {
						isParagraphEnd = true;
						break;
					}
					
					graphics.drawImage(word, null, xOffset, yOffset);
					
					xOffset += (word.getWidth() + param.get(TypesetParamter.KEY_WORD_SPACE));
				}
				while ((--wordCnt > 0) && !wordList.isEmpty());
				
				if (wordCnt < param.get(TypesetParamter.KEY_WORDS_PER_LINE)) {
					yOffset += (avgWordHeight + param.get(TypesetParamter.KEY_LINE_SPACE));
				}
			}
			while ((--lineCnt > 0) && !wordList.isEmpty());
			
			pageImageList.add(pageImage);
		}
		while (!wordList.isEmpty());
		
		return pageImageList;
	}
	
	private int calculatePageWidth(int avgWordWidth) {
		int pageWidth = param.get(TypesetParamter.KEY_LEFT_MARGIN)
				+ avgWordWidth * param.get(TypesetParamter.KEY_WORDS_PER_LINE)
				+ param.get(TypesetParamter.KEY_WORD_SPACE) * (param.get(TypesetParamter.KEY_WORDS_PER_LINE) - 1)
				+ param.get(TypesetParamter.KEY_RIGHT_MARGIN);
		
		return pageWidth;
	}
	
	private int calculatePageHeight(int avgWordHeight) {
		int pageHeight = param.get(TypesetParamter.KEY_TOP_MARGIN)
				+ avgWordHeight * param.get(TypesetParamter.KEY_LINES_PER_PAGE)
				+ param.get(TypesetParamter.KEY_LINE_SPACE) * (param.get(TypesetParamter.KEY_LINES_PER_PAGE) - 1)
				+ param.get(TypesetParamter.KEY_BOTTOM_MARGIN);
		
		return pageHeight;
	}
	
	private BufferedImage createPageImage(int pageWidth, int pageHeight) {
		BufferedImage pageImage = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_BYTE_GRAY);
		
		Graphics2D graphics = pageImage.createGraphics();
		graphics.setPaint(new Color(255, 255, 255));
		graphics.fillRect(0, 0, pageWidth, pageHeight);
		
		return pageImage;
	}
}