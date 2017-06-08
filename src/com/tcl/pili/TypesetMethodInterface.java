package com.tcl.pili;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

interface TypesetMethodInterface {
	public void setParameter(TypesetParamter param);
	public ArrayList<BufferedImage> typeset(BufferedImage src);
}