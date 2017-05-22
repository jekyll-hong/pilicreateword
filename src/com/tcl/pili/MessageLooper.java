class MessageLooper implements WebsiteParser.Observer extends Thread  {
	private LinkedList<Message> mMessageQueue;
	private Object mLock;

	private Typesetter mTypesetter;
	private PDFPacker mPDFPacker;

	private File mStorageDir;
	private File mDramaDir;
	private File mEpisodeDir;

	public MessageLooper(String storagePath) {
		mMessageQueue = new LinkedList<Message>();
		mLock = new Object();
	}
	
	public void setStoragePath(String storagePath) {
		mStorageDir = new File(storagePath);
		if (!mStorageDir.exist()) {
			mStorageDir.mkdirs();
		}
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
				case MSG_MAKE_PAGES: {
					File imageFile = (File)msg.obj;
					mTypesetter.process(imageFile);
					break;
				}
				case MSG_PACK_PDF: {
					File dramaDir = (File)msg.obj;
					mPDFPacker.process(dramaDir);
					break;
				}
				case MSG_COMPLETE: {
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
	
	public void onGetDrama(String name) {
		if (mDramaDir != null) {
			System.out.print("pack " + mDramaDir.getName() + ".pdf file\r\n");
			
			Message msg = new Message();
			msg.what = Message.MSG_PACK_PDF;
			msg.obj = mDramaDir;
			
			mLooper.post(msg);
		}
		
		mDramaDir = Utils.getSubFile(mStorageDir, name);
		if (!mDramaDir.exist()) {
			mDramaDir.mkdir();
		}

		System.out.print("processing " + mDramaDir.getName() + "\r\n");
	}
	
	public void onGetEpisode(String name) {
		mEpisodeDir = Utils.getSubFile(mDramaDir, name);
		if (!mEpisodeDir.exist()) {
			mEpisodeDir.mkdir();
		}

		System.out.print("processing " + mEpisodeDir.getName() + "\r\n");
	}
	
	public void onGetPlot(BufferedImage image) {
		File plotImage = Utils.getSubFile(mEpisodeDir, "剧情口白.png");
		if (!plotImage.exist()) {
			ImageIO.write(image, "png", plotImage);
		}

		System.out.print("typeset " + plotImage.getName() + "\r\n");

		Message msg = new Message();
		msg.what = Message.MSG_MAKE_PAGES;
		msg.obj = plotImage;
		
		mLooper.post(msg);
	}
	
	public void onCompleted() {
		Message msg = new Message();
		msg.what = Message.MSG_COMPLETE;
		
		mLooper.post(msg);
	}
	
	private static final int MSG_MAKE_PAGES = 0;
	private static final int MSG_PACK_PDF = 1;
	private static final int MSG_COMPLETE = 2;
	
	private class Message {
		public int what;
		public Object obj;
	}
}
