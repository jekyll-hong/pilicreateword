class MessageLooper extends Thread  {
	private LinkedList<Message> mMessageQueue;
	private Object mLock;
	
	private ArrayList<MessageHandler> mHandlerList;
	
	public MessageLooper(String storagePath) {
		mMessageQueue = new LinkedList<Message>();
		mLock = new Object();
		
		mHandlerList = new ArrayList<MessageHandler>();
	}
	
	public void registerHandler(MessageHandler handler) {
		mHandlerList.add(handler);
	}
	
	public void post(Message msg) {
		synchronized (mLock) {
			mImageQueue.add(msg);
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
			
			if (msg.what == Message.MSG_COMPLETE) {
				isDone = true;
			}
			else {
				boolean isHandled = false;
				for (int i = 0; i < mHandlerList.size(); i++) {
					MessageHandler handler = mHandlerList.get(i);
					if (handler.handleMessage(msg)) {
						isHandled = true;
						break;
					}
				}
				
				if (!isHandled) {
					System.err.print("unknown message " + msg.what + "\r\n");
				}
			}
		}
	}
	
	private Message getMessage() {
		Message msg;
		
		synchronized (mLock) {
			msg = mMessageQueue.remove();
		}
		
		return msg;
	}
}
