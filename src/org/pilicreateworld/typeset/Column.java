package org.pilicreateworld.typeset;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

class Column {
	public static LinkedList<Column> getColumns(BufferedImage image) {
		LinkedList<Column> columns = new LinkedList<Column>();
		
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
	
	private static Column getOneColumn(BufferedImage image, int xOffset) {
		int left = -1;
		int right = -1;
		int minCount = 0x7fffffff;
		
		while (xOffset < image.getWidth()) {
			int count = getBlackPixelCount(image, xOffset);
			
			if (count < 500) {
				if ((left != -1) && (count < minCount)) {
					minCount = count;
					right = xOffset;
				}
			}
			else {
				if (left == -1) {
					left = xOffset;
				}
				else {
					if (right != -1)  {
						return new Column(left, right - 1);
					}
				}
			}
			
			xOffset++;
		}
		
		if ((left != -1) && (right != 1)) {
			return new Column(left, right - 1);
		}
		else {
			return null;
		}
	}
	
	private static int getBlackPixelCount(BufferedImage image, int xOffset) {
		int blackPixelCnt = 0;
		
		for (int i = 0; i < image.getHeight(); i++) {
			int gray = image.getRGB(xOffset, i) & 0xff;
			if (gray < 160) {
				blackPixelCnt++;
			}
		}
		
		return blackPixelCnt;
	}
	
	private int left;
	private int right;
	
	public Column(int left, int right) {
		this.left = left;
		this.right = right;
	}
	
	public int left() {
		return left;
	}
	
	public int right() {
		return right;
	}
	
	public int length() {
		return right - left + 1;
	}
}