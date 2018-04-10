package com.pilicreateworld.ocr;

import com.pilicreateworld.ocr.tencent.OcrApi;

public class Factory {
	public static Client create() {
		/**
		 * 目前只集成了腾讯云ocr
		 */
		switch (Settings.getOcrService()) {
			default: {
				return new OcrApi.getInstance();
			}
		}
	}
}
