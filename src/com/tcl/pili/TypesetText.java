package com.tcl.pili;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

final class TypesetText implements Runnable {
	private BufferedImage plotImage;
	private OnTextTypesetListener listener;
	
	public TypesetText(BufferedImage plotImage, OnTextTypesetListener listener) {
		this.plotImage = plotImage;
		this.listener = listener;
	}
	
	public void run() {
		BufferedImage dst = preprocess(plotImage);
		listener.onTextTypeset(typeset(dst));
	}
	
	private BufferedImage preprocess(BufferedImage src) {
		BufferedImage temp = ImageProcess.grayScale(src);
		return ImageProcess.sharpen(temp);
	}
	
	private class Rect {
		public int top;
		public int left;
		public int bottom;
		public int right;
		
		public Rect() {
			top = 0;
			left = 0;
			bottom = top;
			right = left;
		}
		
		public Rect(int top, int left, int bottom, int right) {
			this.top = top;
			this.left = left;
			this.bottom = bottom;
			this.right = right;
		}
		
		public boolean isValid() {
			if ((bottom - top > 0) && (right - left > 0)) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	
	private static final int mLineOfPage = 15;
	private static final int mWordOfLine = 20;
	
	private static final int mLineSpace = 5;
	private static final int mWordSpace = 2;
	
	private static final int mTopMargin = 30;
	private static final int mBottomMargin = 30;
	private static final int mLeftMargin = 50;
	private static final int mRightMargin = 50;
	
	private static final int sBackgroundGrayThreshold = 160;
	
	private ArrayList<BufferedImage> typeset(BufferedImage src) {
		ArrayList<BufferedImage> pageImageList = new ArrayList<BufferedImage>();
		
		BufferedImage image = cropMargin(src);
		int wordWidth = predictWordWidth(image);
		int wordHeight = predictWordHeight(image);
		ArrayList<Rect> wordList = getWordInImage(image, wordWidth, wordHeight);
		
		boolean isParagraphEnd = true;		
		do {
			BufferedImage pageImage = createPageImage(wordWidth, wordHeight);
			
			int lineCnt = mLineOfPage;
			int yOffset = mTopMargin + 1;
			
			do {
				if (!isParagraphEnd) {
					Rect word = wordList.get(0);
					if (!word.isValid()) {
						wordList.remove(0);
						isParagraphEnd = true;
					}
				}
				
				int wordCnt = mWordOfLine;
				int xOffset = mLeftMargin + 1;
				
				if (isParagraphEnd) {
					//indent 2 blank word
					wordCnt -= 2;
					xOffset += (wordWidth + mWordSpace) * 2;
					
					isParagraphEnd = false;
				}
				
				Graphics2D graphics = pageImage.createGraphics();
				do {
					Rect word = wordList.remove(0);
					if (!word.isValid()) {
						isParagraphEnd = true;
						break;
					}
					
					BufferedImage wordImage = image.getSubimage(word.left, word.top, word.right - word.left + 1, word.bottom - word.top + 1);
					graphics.drawImage(wordImage, null, xOffset, yOffset);
					
					xOffset += (wordWidth + mWordSpace);
				}
				while ((--wordCnt > 0) && !wordList.isEmpty());
				
				yOffset += (wordHeight + mLineSpace);
			}
			while ((--lineCnt > 0) && !wordList.isEmpty());
			
			pageImageList.add(pageImage);
		}
		while (!wordList.isEmpty());
		
		return pageImageList;
	}
	
	private BufferedImage cropMargin(BufferedImage image) {
		int top = 0;
		for (int i = 0; i < image.getHeight(); i++) {
			int wordPixels = 0;
			for (int j = 0; j < image.getWidth(); j++) {
				int gray = image.getRGB(j, i) & 0xff;
				if (gray < sBackgroundGrayThreshold) {
					wordPixels++;
				}
			}
			
			if (wordPixels > 0) {
				top = i;
				break;
			}
		}
		
		int bottom = image.getHeight() - 1;
		for (int i = image.getHeight() - 1; i >= 0; i--) {
			int wordPixels = 0;
			for (int j = 0; j < image.getWidth(); j++) {
				int gray = image.getRGB(j, i) & 0xff;
				if (gray < sBackgroundGrayThreshold) {
					wordPixels++;
				}
			}
			
			if (wordPixels > 0) {
				bottom = i;
				break;
			}
		}
		
		int left = 0;
		for (int i = 0; i < image.getWidth(); i++) {
			int wordPixels = 0;
			for (int j = 0; j < image.getHeight(); j++) {
				int gray = image.getRGB(i, j) & 0xff;
				if (gray < sBackgroundGrayThreshold) {
					wordPixels++;
				}
			}
			
			if (wordPixels > 0) {
				left = i;
				break;
			}
		}
		
		int right = image.getWidth() - 1;
		for (int i = image.getWidth() - 1; i >= 0; i--) {
			int wordPixels = 0;
			for (int j = 0; j < image.getHeight(); j++) {
				int gray = image.getRGB(i, j) & 0xff;
				if (gray < sBackgroundGrayThreshold) {
					wordPixels++;
				}
			}
			
			if (wordPixels > 0) {
				right = i;
				break;
			}
		}
		
		return image.getSubimage(left, top, right - left + 1, bottom - top + 1);
	}
	
	private int predictWordWidth(BufferedImage image) {
		Hashtable<Integer, Integer> widthStat = new Hashtable<Integer, Integer>();
		
		int xOffset = 0;
		while (true) {
			int[] column = getOneColumn(image, xOffset);
			if (column[0] == -1) {
				break;
			}
			
			int width = column[1] - column[0] + 1;
			int count = 1;
			if (widthStat.containsKey(width)) {
				count = widthStat.get(width) + 1;
			}
			widthStat.put(width, count);
			
			xOffset = column[1] + 1;
		}
		
		return getMode(widthStat);
	}
	
	private int[] getOneColumn(BufferedImage image, int pos) {
		int start = -1;
		int end = -1;
		
		for (int i = pos; i < image.getWidth(); i++) {
			int wordPixels = 0;
			for (int j = 0; j < image.getHeight(); j++) {
				int gray = image.getRGB(i, j) & 0xff;
				if (gray < sBackgroundGrayThreshold) {
					wordPixels++;
				}
			}
			
			if (wordPixels < 100) {
				if (start != -1) {
					break;
				}
			}
			else {
				if (start == -1) {
					start = i;
					end = start;
				}
				else {
					end += 1;
				}
			}
		}
		
		return new int[] {start, end};
	}
	
	private int predictWordHeight(BufferedImage image) {
		Hashtable<Integer, Integer> heightStat = new Hashtable<Integer, Integer>();
		
		int yOffset = 0;
		while (yOffset < image.getHeight()) {
			int[] line = getOneLine(image, yOffset);
			if (line[0] == -1) {
				break;
			}
			
			int height = line[1] - line[0] + 1;
			int count = 1;
			if (heightStat.containsKey(height)) {
				count = heightStat.get(height) + 1;
			}
			heightStat.put(height, count);
			
			yOffset = line[1] + 1;
		}
		
		return getMode(heightStat);
	}
	
	private int[] getOneLine(BufferedImage image, int pos) {
		int start = -1;
		int end = -1;
		
		for (int i = pos; i < image.getHeight(); i++) {
			int wordPixels = 0;
			for (int j = 0; j < image.getWidth(); j++) {
				int gray = image.getRGB(j, i) & 0xff;
				if (gray < sBackgroundGrayThreshold) {
					wordPixels++;
				}
			}
			
			if (wordPixels < 3) {
				if (start != -1) {
					break;
				}
			}
			else {
				if (start == -1) {
					start = i;
					end = start;
				}
				else {
					end += 1;
				}
			}
		}
		
		return new int[] {start, end};
	}
	
	private int getMode(Hashtable<Integer, Integer> stat) {
		Iterator<Map.Entry<Integer, Integer>> entryIterator = stat.entrySet().iterator();
		
		Map.Entry<Integer, Integer> maxCntEntry = null;
		while (entryIterator.hasNext()) {
			Map.Entry<Integer, Integer> entry = entryIterator.next();
			
			if ((maxCntEntry == null)
				|| (entry.getValue() > maxCntEntry.getValue())
				|| (entry.getValue() == maxCntEntry.getValue() && entry.getKey() > maxCntEntry.getKey())) {
				maxCntEntry = entry;
			}
		}
		
		return maxCntEntry.getKey();
	}
	
	private ArrayList<Rect> getWordInImage(BufferedImage image, int wordWidth, int wordHeight) {
		ArrayList<Rect> wordList = new ArrayList<Rect>();
		
		ArrayList<int[]> columns = getColumns(image, wordWidth);
		ArrayList<int[]> lines = getLines(image, wordHeight);
		for (int i = 0; i < lines.size(); i++) {
			int[] line = lines.get(i);
			
			if (i > 0) {
				int[] prevLine = lines.get(i - 1);
				if (line[0] - prevLine[1] > wordHeight) {
					wordList.add(new Rect());
				}
			}
			
			for (int j = 0; j < columns.size(); j++) {
				int[] column = columns.get(j);
				
				if (!isBlank(image, column, line)) {
					wordList.add(new Rect(line[0], column[0], line[1], column[1]));
				}
			}
		}
		
		return wordList;
	}
	
	private ArrayList<int[]> getLines(BufferedImage image, int wordHeight) {
		ArrayList<int[]> lines = new ArrayList<int[]>();
		
		int yOffset = 0;
		while (yOffset < image.getHeight()) {
			int[] line = getOneLine(image, yOffset);
			if (line[0] == -1) {
				break;
			}
			
			int height = line[1] - line[0] + 1;
			if (height < wordHeight / 4) {
				if (!lines.isEmpty()) {
					int[] prevLine = lines.get(lines.size() -1);
					
					int prevHeight = prevLine[1] - prevLine[0] + 1;
					if (prevHeight < wordHeight) {
						prevLine[1] = line[1];
					}
					else {
						lines.add(line);
					}					
				}
			}
			else {
				lines.add(line);
			}			
			
			yOffset = line[1] + 1;
		}
		
		return lines;
	}
	
	private ArrayList<int[]> getColumns(BufferedImage image, int wordWidth) {
		ArrayList<int[]> columns = new ArrayList<int[]>();
		
		int xOffset = 0;
		while (xOffset < image.getWidth()) {
			int[] column = getOneColumn(image, xOffset);
			if (column[0] == -1) {
				break;
			}
			
			int width = column[1] - column[0] + 1;
			if (width < wordWidth / 4) {
				if (!columns.isEmpty()) {
					int[] prevColumn = columns.get(columns.size() -1);
					
					int prevWidth = prevColumn[1] - prevColumn[0] + 1;
					if (prevWidth < wordWidth) {
						prevColumn[1] = column[1];
					}
					else {
						columns.add(column);
					}					
				}
			}
			else {
				columns.add(column);
			}			
			
			xOffset = column[1] + 1;
		}
		
		return columns;
	}
	
	private boolean isBlank(BufferedImage image, int[] column, int[] line) {
		int wordPixels = 0;
		for (int i = line[0]; i <= line[1]; i++) {
			for (int j = column[0]; j <= column[1]; j++) {
				int gray = image.getRGB(j, i) & 0xff;
				if (gray < sBackgroundGrayThreshold) {
					wordPixels++;
				}
			}
		}
		
		return wordPixels == 0;
	}
	
	private BufferedImage createPageImage(int wordWidth, int wordHeight) {
		int pageWidth = mLeftMargin + (wordWidth * mWordOfLine) + ((mWordOfLine - 1) * mWordSpace) + mRightMargin;
		int pageHeight = mTopMargin + (wordHeight * mLineOfPage) + ((mLineOfPage - 1) * mLineSpace) + mBottomMargin;
		BufferedImage pageImage = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_BYTE_GRAY);
		
		Graphics2D graphics = pageImage.createGraphics();
		graphics.setPaint(new Color(255, 255, 255));
		graphics.fillRect(0, 0, pageWidth, pageHeight);
		
		return pageImage;
	}
}
