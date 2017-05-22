package com.tcl.pili;

final class Typesetter {
	public static Typesetter createForDevice(String device) {
		if (device.equals("nexus 5")) {
			return new Typesetter(, , 2);
		}
		else {
			return null;
		}
	}
	
	private int mLinesOfPage;
	private int mWordsOfLine;
	private int mSpace;
	
	private Typesetter(int linesOfPage, int wordsOfLine, int space) {
		mLinesOfPage = linesOfPage;
		mWordsOfLine = wordsOfLine;
		mSpace = space;
	}
	
	public void process(File imageFile) {
		File episodeDir = imageFile.getParentFile();

		File pageDir = Utils.getSubFile(episodeDir, "page-" + mTargetDevice);
		if (!pageDir.exist()) {
			pageDir.mkdir();
		}
		
		BufferedImage image = preprocess(ImageIO.read(imageFile));
		if (Utils.DEBUG) {
			ImageIO.write(image, "jpeg", Utils.getSubFile(episodeDir, "preprocess.jpg"));
		}

		ArrayList<BufferedImage> pageList = typeset(image);
		for (int i = 0; i < pageList.size(); i++) {
			ImageIO.write(image, "png", Utils.getSubFile(pageDir, i + ".png"));
		}
	}
	
	private BufferedImage preprocess(BufferedImage src) {
		BufferedImage dst;
		
		dst = ImageProcess.crop(src, 30, 1);
		dst = ImageProcess.gray(dst);
		dst = ImageProcess.enhance(dst);
		
		return dst;
	}
	
	private ArrayList<BufferedImage> typeset(BufferedImage image) {
		ArrayList<BufferedImage> pageList = new ArrayList<BufferedImage>();
		
		//predict word rectangle
		int[] word = wordRect(image);
		final int wordWidth = word[3] - word[1] + 1;
		final int wordHeight = word[2] - word[0] + 1;
		
		//calculate page parameter
		final int margin = 2 * wordHeight;
		final int pageWidth = margin + (wordWidth * mWordsOfLine) + ((mWordsOfLine - 1) * space) + margin;
		final int pageHeight = margin + (wordHeight * mLinesOfPage) + ((mLinesOfPage - 1) * space) + margin;
		
		//get all word rectangles
		ArrayList<int[]> words = wordsInImage(image, wordWidth, wordHeight);
			
		boolean newSection = true;		
		while (!words.isEmpty()) {
			//create page image
			BufferedImage pageImage = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_BYTE_GRAY);
			
			//fill page image by white color 
			Graphics2D graphics = pageImage.createGraphics();
			graphics.setPaint(new Color(255, 255, 255));
			graphics.fillRect(0, 0, pageImage.getWidth(), pageImage.getHeight());
			
			//place words
			int yPos = margin + 1;
			for (int i = 0; i < lineNumber; i++) {
				if (words.isEmpty()) {
					break;
				}
				
				//if next line is a new section
				if (!newSection) {
					word = words.get(0);
					if (word[0] == -1) {
						words.remove(0);
						//end of this section. next line is a new section
						newSection = true;
					}
				}
				
				int wordsNumber = wordsInLine;
				int xPos = margin + 1;
				
				// if new section, indent 2 blank word
				if (newSection) {
					xPos += ((wordWidth + space) * 2);
					wordsNumber -= 2;
					
					newSection = false;
				}
				
				for (int j = 0; j < wordsNumber; j++) {
					if (words.isEmpty()) {
						break;
					}
					
					//if section end
					word = words.remove(0);
					if (word[0] == -1) {
						newSection = true;
						break;
					}
					
					//crop word image and draw on page
					BufferedImage wordImage = image.getSubimage(word[1], word[0], word[3] - word[1] + 1, word[2] - word[0] + 1);
					graphics.drawImage(wordImage, null, xPos, yPos);
					xPos += wordWidth;
					xPos += space;
				}
				
				yPos += wordHeight;
				yPos += space;
			}
			
			pageList.add(pageImage);
		}
	}

	private static int[] wordRect(BufferedImage image) {
		int[] line = getLineOfWords(image, 0);
		int[] column = getColumnOfWords(image, 0);
		
		return new int[] {line[0], column[0], line[1], column[1]};
	}
	
	private static ArrayList<int[]> wordsInImage(BufferedImage image, int wordWidth, int wordHeight) {
		ArrayList<int[]> lines = new ArrayList<int[]>();
		ArrayList<int[]> columns = new ArrayList<int[]>();
		
		int yOffset = 0;
		while (yOffset < image.getHeight()) {
			int[] line = getLineOfWords(image, yOffset);
			if (line[0] == -1) {
				//no more line
				break;
			}
			
			lines.add(line);
			yOffset = line[1] + 1;
		}
		
		int xOffset = 0;
		while (true) {
			int[] column = getColumnOfWords(image, xOffset);
			if (column[0] == -1) {
				//no more column
				break;
			}
			
			columns.add(column);
			xOffset = column[1] + 1;
		}
		
		ArrayList<int[]> words = new ArrayList<int[]>();
		for (int i = 0; i < lines.size(); i++) {
			int[] line = lines.get(i);
			
			if (i >= 1) {
				int[] prevLine = lines.get(i - 1);
				if (line[0] - prevLine[1] > wordHeight) {
					words.add(new int[] {-1, -1, -1, -1}); //section flag
				}
			}
			
			for (int j = 0; j < columns.size(); j++) {
				int[] column = columns.get(j);
				
				int[] word = new int[] {line[0], column[0], line[1], column[1]};
				if (!isBlankWord(image, word)) {
					words.add(word);
				}
			}
		}
		return words;
	}
	
	private static int[] getLineOfWords(BufferedImage image, int pos) {
		int start = -1;
		int end = -1;
		
		for (int i = pos; i < image.getHeight(); i++) {
			int wordPixels = 0;
			for (int j = 0; j < image.getWidth(); j++) {
				int gray = image.getRGB(j, i) & 0xff;
				//FIXME: background gray is bigger than 240
				if (gray < 240) {
					wordPixels++;
				}
			}
			
			//FIXME: pixels in word are less than 5, line belongs to background
			if (wordPixels < 5) {
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
	
	private static int[] getColumnOfWords(BufferedImage image, int pos) {
	    int start = -1;
	    int end = -1;

	    for (int i = pos; i < image.getWidth(); i++) {
	        int wordPixels = 0;
	        for (int j = 0; j < image.getHeight(); j++) {
				int gray = image.getRGB(i, j) & 0xff;
				//FIXME: background gray is bigger than 240
				if (gray < 240) {
					wordPixels++;
				}
			}
	        
	        //FIXME: pixels in word are less than 150, column belongs to background
	        if (wordPixels < 150) {
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
	
	private static boolean isBlankWord(BufferedImage image, int[] word) {
		for (int i = word[0]; i <= word[2]; i++) {
			for (int j = word[1]; j <= word[3]; j++) {
				int gray = image.getRGB(j, i) & 0xff;
				//FIXME: background gray is bigger than 240
				if (gray < 240) {
					return false;
				}
			}
		}
		
		return true;
	}
}
