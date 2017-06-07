package com.tcl.pili;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

interface OnTextTypesetListener {
	public void onTextTypeset(ArrayList<BufferedImage> pageImageList);
}