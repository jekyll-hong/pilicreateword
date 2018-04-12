package com.pilicreateworld.ocr;

import com.pilicreateworld.Settings;
import com.pilicreateworld.ocr.tencent.TencentApi;

public class OcrServiceFactory {
	public static OcrService getService() {
		/**
		 * 目前只集成了腾讯云ocr
		 */
		switch (Settings.getInstance().getOcrServiceType()) {
			case OcrServiceType.TENCENT: {
				return TencentApi.getInstance();
			}
			default: {
				throw new IllegalArgumentException("unknown ocr service");
			}
		}
	}
}
