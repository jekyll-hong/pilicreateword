class MessageLooper extends Thread {
	private LinkedList<Message> mMessageQueue;
	private Object mLock;

	private Typesetter mTypesetter;
	private PDFPacker mPDFPacker;

	public MessageLooper() {
		mMessageQueue = new LinkedList<Message>();
		mLock = new Object();
	}
	
	public void setTypesetter(Typesetter typesetter) {
		mTypesetter = typesetter;
	}

	public void setPDFPacker(PDFPacker pdfPacker) {
		mPDFPacker = pdfPacker;
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
			
			switch (msg.what) {
				case Message.MSG_MAKE_PAGES: {
					File imageFile = (File)msg.obj;
					mTypesetter.process(imageFile);
					break;
				}
				case Message.MSG_PACK_PDF: {
					File dramaDir = (File)msg.obj;
					mPDFPacker.process(dramaDir);
					break;
				}
				case Message.MSG_COMPLETE: {
					isDone = true;
					break;
				}
				default: {
					System.err.print("unknown message " + msg.what + "\r\n");
					break;
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
