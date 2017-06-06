package com.tcl.pili;

import java.util.concurrent.Executor;

abstract class MessageHandlerImpl implements MessageHandler {
	private Executor mExecutor;
	
	public MessageHandlerImpl(Executor executor) {
		mExecutor = executor;
	}
	
	public abstract boolean handleMessage(Message msg);
	
	protected void execute(Runnable runnable) {
		mExecutor.execute(runnable);
	}
}
