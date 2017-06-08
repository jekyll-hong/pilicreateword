package com.tcl.pili;

public class Application {
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.print("no storage path\r\n");
			return;
		}
		
		MessageLooper looper = MessageLooper.getInstance();
		looper.registerHandler(new MessageHandler());
		looper.start();
		
		new WebsiteParser().execute(args[0]);
		
		looper.quitSafely();
	}
}