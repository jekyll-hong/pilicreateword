package org.pilicreateworld.ocr;

public class OCRFactory {
	static public OCRInterface create(int type) {
		switch (type) {
			case OCRType.OCR_ABBYY: {
				return new AbbyyOCR();
			}
			case OCRType.OCR_BAIDU: {
				return new BaiduOCR();
			}
			default: {
				return null;
			}
		}
	}
}