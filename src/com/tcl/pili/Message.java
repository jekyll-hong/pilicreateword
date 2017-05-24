final class Message {
	public static final int MSG_MAKE_PAGES = 1;
	public static final int MSG_PACK_PDF = 2;
	public static final int MSG_COMPLETE = 3;
	
	public int what;
	public Object obj;
	
	public Message(int what) {
		this.what = what;
		this.obj = null;
	}
	
	public Message(int what, Object obj) {
		this.what = what;
		this.obj = obj;
	}
}
