package com.tcl.pili;

import java.util.LinkedList;

class MessageLooper extends Thread  {
	private static MessageLooper sInstance = null;
	
	public static MessageLooper getInstance() {
		if (sInstance == null) {
			sInstance = new MessageLooper();
		}
		
		return sInstance;
	}
	
	private LinkedList<Message> mMessageQueue;
	private Object mLock;
	
	private MessageHandler mHandler;
	
	private MessageLooper() {
		mMessageQueue = new LinkedList<Message>();
		mLock = new Object();
	}
	
	public void registerHandler(MessageHandler handler) {
		mHandler = handler;
	}
	
	public void post(Message msg) {
		synchronized (mLock) {
			mMessageQueue.add(msg);
		}
	}
	
	public void quitSafely() {
		try {
			join();
		}
		catch (InterruptedException e) {
		}
	}
	
	public void run() {
		boolean isDone = false;
		
		while (!isDone) {
			Message msg = getMessage();
			if (msg == null) {
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e) {
				}
				
				continue;
			}
			
			if (mHandler != null) {
				mHandler.handleMessage(msg);
			}
			
			if (msg.what == Message.MSG_COMPLETE) {
				isDone = true;
			}
		}
	}
	
	private Message getMessage() {
		Message msg = null;
		
		synchronized (mLock) {
			if (!mMessageQueue.isEmpty()) {
				msg = mMessageQueue.remove();
			}
		}
		
		return msg;
	}
}