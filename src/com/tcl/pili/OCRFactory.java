package com.tcl.pili;

final class OCRFactory {
	public static final int OCR_ABBYY = 0;
	public static final int OCR_BAIDU = 1;
	
	static public OCRInterface create(int type) {
		switch (type) {
			case OCR_ABBYY: {
				return new AbbyyOCR();
			}
			case OCR_BAIDU: {
				return new BaiduOCR();
			}
			default: {
				return null;
			}
		}
	}
}