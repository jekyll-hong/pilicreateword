package org.pilicreateworld.typeset;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

class Line {
	public static LinkedList<Line> getLines(BufferedImage image) {
		LinkedList<Line> lines = new LinkedList<Line>();
		
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
	
	private static Line getOneLine(BufferedImage image, int yOffset) {
		int top = -1;
		
		while (yOffset < image.getHeight()) {
			int count = getBlackPixelCount(image, yOffset);
			
			if (count == 0) {
				if ((top != -1) && (yOffset - top >= 15)) {
					return new Line(top, yOffset - 1);
				}
			}
			else {
				if (top == -1) {
					top = yOffset;
				}
			}
			
			yOffset++;
		}
		
		return null;
	}
	
	private static int getBlackPixelCount(BufferedImage image, int yOffset) {
		int blackPixelCnt = 0;
		
		for (int i = 0; i < image.getWidth(); i++) {
			int gray = image.getRGB(i, yOffset) & 0xff;
			if (gray < 160) {
				blackPixelCnt++;
			}
		}
		
		return blackPixelCnt;
	}
	
	private int top;
	private int bottom;
	
	private Line(int top, int bottom) {
		this.top = top;
		this.bottom = bottom;
	}
	
	public int top() {
		return top;
	}
	
	public int bottom() {
		return bottom;
	}
	
	public int length() {
		return bottom - top + 1;
	}
}