package com.tcl.pili;

public class Application {
	private static boolean sUseProxy = false;
	
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.print("no storage path or target device\r\n");
			return;
		}
		
		String storageDirPath = args[0];
		String targetDevice = args[1];
		
		MessageLooper looper = new MessageLooper();
		looper.registerHandler(Typesetter.createForDevice(targetDevice));
		looper.registerHandler(new PDFPacker());
		looper.start();
		
		WebsiteParser websiteParser = new WebsiteParser(storageDirPath);
		websiteParser.setListener(new WebsiteParseListenerImpl(looper));
		if (sUseProxy) {
			websiteParser.setProxy("127.0.0.1", 37689);
		}
		websiteParser.start();
		
		try {
			websiteParser.join();
			looper.join();
		}
		catch (InterruptedException e) {
		}
		
		System.out.print("mission complete\r\n");
	}
}
