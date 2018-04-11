package com.pilicreateworld.ocr;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface OcrService {
	String process(BufferedImage image) throws IOException;
}
