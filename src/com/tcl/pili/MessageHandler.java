package com.tcl.pili;

import java.util.concurrent.ScheduledThreadPoolExecutor;

final class MessageHandler {
	private ScheduledThreadPoolExecutor mNetworkThreadPool;
	private ScheduledThreadPoolExecutor mThreadPool;
	
	public MessageHandler() {
		mNetworkThreadPool = new ScheduledThreadPoolExecutor(2);
		mThreadPool = new ScheduledThreadPoolExecutor(2);
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
				mThreadPool.execute(job);
				break;
			}
			case Message.MSG_PACK_PDF: {
				PackPDF job = (PackPDF)msg.obj;
				mThreadPool.execute(job);
				break;
			}
			case Message.MSG_COMPLETE: {
				mNetworkThreadPool.shutdown();
				mThreadPool.shutdown();
				break;
			}
			default: {
				return false;
			}
		}
		
		return true;
	}
}