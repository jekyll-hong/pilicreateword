package org.pilicreateworld.job.runnable;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

public interface OnTextTypesetListener {
	public void onTextTypeset(LinkedList<BufferedImage> pages);
}