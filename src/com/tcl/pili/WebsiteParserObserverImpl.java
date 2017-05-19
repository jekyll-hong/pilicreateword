class WebsiteParserObserverImpl implements WebsiteParser.Observer {
	private File mStorageDir;
	private File mDramaDir;
	private File mEpisodeDir;

	private MessageLooper mLooper;
	
	public WebsiteParserObserverImpl(String storagePath) {
		mStorageDir = new File(storagePath);
		if (!mStorageDir.exist()) {
			mStorageDir.mkdirs();
		}
	}

	public void setMessageLooper(MessageLooper looper) {
		mLooper = looper;
	}
	
	public void onGetDrama(String name) {
		if (mDramaDir != null) {
			Message msg = new Message();
			msg.what = Message.MSG_PACK_PDF;
			msg.obj = mDramaDir;
			
			mLooper.post(msg);
		}
		
		mDramaDir = Utils.getSubFile(mStorageDir, name);
		if (!mDramaDir.exist()) {
			mDramaDir.mkdir();
		}
	}

	public void onGetEpisode(String name) {
		mEpisodeDir = Utils.getSubFile(mDramaDir, name);
		if (!mEpisodeDir.exist()) {
			mEpisodeDir.mkdir();
		}
	}

	public void onGetPlot(BufferedImage image) {
		File plotImage = Utils.getSubFile(mEpisodeDir, "剧情口白.png");
		if (!plotImage.exist()) {
			ImageIO.write(image, "png", plotImage);
		}

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
}
