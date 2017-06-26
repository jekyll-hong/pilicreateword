package org.pilicreateworld;

import org.pilicreateworld.job.JobDispatcher;
import org.pilicreateworld.parser.Observer;
import org.pilicreateworld.parser.Parser;

public class Application implements Observer   {
	private Object lock = new Object();
	
	public void execute(String path) {
		Parser parser = new Parser(path);
		parser.parse(this);
		
		synchronized (lock) {
			try {
				lock.wait();
			}
			catch (InterruptedException e) {
			}
		}
		
		JobDispatcher.getInstance().release();
	}
	
	public void onComplete() {
		synchronized (lock) {
			lock.notify();
		}
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.print("no storage path\r\n");
			return;
		}
		
		Application app = new Application();
		app.execute(args[0]);
	}
}