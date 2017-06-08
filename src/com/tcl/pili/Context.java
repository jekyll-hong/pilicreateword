package com.tcl.pili;

import java.io.File;

final class Context {
	private String path;
	
	public Pili pili;
	
	public Context(String path) {
		this.path = path;
	}
	
	public File getDir() {
		File storageDir = new File(path);
		if (!storageDir.exists()) {
			storageDir.mkdirs();
		}
		
		return storageDir;
	}
	
	public void createPili(String url) {
		pili = new Pili(this, url);
	}
	
	public Pili getPili() {
		return pili;
	}
}