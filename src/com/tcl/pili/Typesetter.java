package com.tcl.pili;

final class Typesetter {
	public static Typesetter createForDevice(String device) {
		if (device.equals("nexus5")) {
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
	
	public ArrayList<BufferedImage> typeset(BufferedImage image) {
	}
}
