package com.tcl.pili;

import java.util.concurrent.ScheduledThreadPoolExecutor;

final class MessageHandler {
	private ScheduledThreadPoolExecutor mNetworkThreadPool;
	private ScheduledThreadPoolExecutor mTypesetThreadPool;
	private ScheduledThreadPoolExecutor mPackThreadPool;
	
	public MessageHandler() {
		mNetworkThreadPool = new ScheduledThreadPoolExecutor(4);
		mTypesetThreadPool = new ScheduledThreadPoolExecutor(2);
		mPackThreadPool = new ScheduledThreadPoolExecutor(2);
	}
	
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case Message.MSG_LOAD_WEBPAGE: {
				LoadPage job = (LoadPage)msg.obj;
				mNetworkThreadPool.execute(job);
				break;
			}
			case Message.MSG_DOWNLOAD_IMAGE: {
				DownloadImage job = (DownloadImage)msg.obj;
				mNetworkThreadPool.execute(job);
				break;
			}
			case Message.MSG_TYPESET_TEXT: {
				TypesetText job = (TypesetText)msg.obj;
				mTypesetThreadPool.execute(job);
				break;
			}
			case Message.MSG_PACK_PDF: {
				PackPDF job = (PackPDF)msg.obj;
				mPackThreadPool.execute(job);
				break;
			}
			case Message.MSG_COMPLETE: {
				mNetworkThreadPool.shutdown();
				mTypesetThreadPool.shutdown();
				mPackThreadPool.shutdown();
				break;
			}
			default: {
				return false;
			}
		}
		
		return true;
	}
}