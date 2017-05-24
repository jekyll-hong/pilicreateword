package com.tcl.pili;

final class Utils {
	public static boolean DEBUG = true;
	
	public static File getChildFile(File file, String name) {
		String dirPath;
		try {
			dirPath = file.getCanonicalPath();
		}
		catch (IOException e) {
		}
		
		return new File(dirPath + "/" + name);
	}
	
	public static File getSiblingFile(File file, String name) {
		String dirPath;
		try {
			dirPath = file.getParentFile().getCanonicalPath();
		}
		catch (IOException e) {
		}

		return new File(dirPath + "/" + name);
	}
}
