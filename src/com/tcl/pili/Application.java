package com.tcl.pili;

import java.io.File;

public class Application implements MessageHandler {
	private WebsiteParser websiteParser;
	
	public Application(String storageDirPath) {
		File storageDir = new File(storageDirPath);
		if (!storageDir.exists()) {
			storageDir.mkdirs();
		}
		
		websiteParser = new WebsiteParser(storageDir);
	}
	
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
	    	case Message.MSG_START: {
	    		websiteParser.parse();
	    		break;
	    	}
			case Message.MSG_LOAD_WEBPAGE: {
				break;
			}
			case Message.MSG_DOWNLOAD_IMAGE: {
				break;
			}
			case Message.MSG_TYPESET: {
				break;
			}
			case Message.MSG_PACK_PDF: {
				break;
			}
			case Message.MSG_COMPLETE: {
				break;
			}
			default: {
				return false;
			}
		}
		
		return true;
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.print("no storage path\r\n");
			return;
		}
		
		MessageLooper looper = MessageLooper.getInstance();
		looper.registerHandler(new Application(args[0]));
		looper.start();
		
		try {
			looper.join();
		}
		catch (InterruptedException e) {
		}
		
		System.out.print("mission complete\r\n");
	}
}