package com.tcl.pili;

public final class Application {
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.print("must set target device and storage path\r\n");
			return;
		}
		
		MessageLooper looper = new MessageLooper();
		looper.setTypesetter(Typesetter.createForDevice(arg[0]));
		looper.setPDFPacker(new PDFPacker());
		looper.start();

		WebsiteParserObserverImpl observer = new WebsiteParserObserverImpl(args[1]);
		observer.setMessageLooper(looper);

		Proxy httpProxy = null;
		if (args.length > 3) {
			int pos = args[2].indexOf(":");
			if (pos < 0) {
				System.err.print("no port in http proxy\r\n");
				return;
			}

			String ip = args[2].substring(0, pos);
			int port = Integer.parseInt(args[2].substring(pos + 1));
			httpProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
		}

		WebsiteParser parser = new WebsiteParser(httpProxy);
		parser.setObserver(observer);

		new Thread(WebsiteParser).start();

		try {
			looper.join();
		}
		catch (InterruptedException e) {
		}
	}
}
