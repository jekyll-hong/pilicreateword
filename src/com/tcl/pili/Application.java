package com.tcl.pili;

public final class Application {
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.print("no storage path or target device\r\n");
			return;
		}
		
		String storageDirPath = args[0];
		String targetDevice = arg[1];
		
		WebsiteParser websiteParser = new WebsiteParser(storageDirPath);
		if (args.length > 3) {
			int pos = args[2].indexOf(":");
			if (pos < 0) {
				System.err.print("no port in http proxy\r\n");
				return;
			}
			
			String ip = args[2].substring(0, pos);
			int port = Integer.parseInt(args[2].substring(pos + 1));
			
			websiteParser.setProxy(ip, port);
		}
		
		MessageLooper looper = MessageLooper.getInstance();
		looper.setWebsiteParser();
		looper.setTypesetter(Typesetter.createForDevice(targetDevice));
		looper.setPDFPacker(new PDFPacker());
		looper.start();
		
		websiteParser.start();
		
		try {
			websiteParser.join();
			looper.join();
		}
		catch (InterruptedException e) {
		}
	}
}
