package com.tcl.pili;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

final class Utils {
	public static boolean DEBUG = true;
	
	public static File getChildFile(File file, String name) {
		String dirPath = "";
		try {
			dirPath = file.getCanonicalPath();
		}
		catch (IOException e) {
		}
		
		return new File(dirPath + "/" + name);
	}
	
	public static File getSiblingFile(File file, String name) {
		String dirPath = "";
		try {
			dirPath = file.getParentFile().getCanonicalPath();
		}
		catch (IOException e) {
		}

		return new File(dirPath + "/" + name);
	}
	
	public static String convertToUTF16(String in) {
		String out = "";
		
		try {
			out = new String(in.getBytes("UTF-16"), "UTF-16");
		}
		catch (UnsupportedEncodingException e) {
		}
		
		return out;
	}
}
