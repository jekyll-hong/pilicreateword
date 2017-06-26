package org.pilicreateworld.parser;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

class Utils {
	public static File getChildFile(File file, String name) {
		String dirPath = "";
		try {
			dirPath = file.getCanonicalPath();
		}
		catch (IOException e) {
		}
		
		return new File(dirPath + "/" + name);
	}
	
	public static String convertToUTF16(String in) {
		String out;
		
		try {
			out = new String(in.getBytes("UTF-16"), "UTF-16");
		}
		catch (UnsupportedEncodingException e) {
			out = in;
		}
		
		return out;
	}
}