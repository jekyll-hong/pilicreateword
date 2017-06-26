package org.pilicreateworld.job.runnable;

import java.io.File;

public interface OnImageDownloadListener {
	public void onImageDownload(File file);
	public void onError();
}