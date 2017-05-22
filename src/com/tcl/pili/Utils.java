package com.tcl.pili;

final class Utils {
	public static boolean DEBUG = true;
	
	public static File getSubFile(File dir, String name) {
		String dirPath;
		try {
			dirPath = dir.getCanonicalPath();
		}
		catch (IOException e) {
		}

		return new File(dir + "/" + name)
	}
}
