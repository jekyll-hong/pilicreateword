package org.pilicreateworld.job;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class JobDispatcher  {
	private static JobDispatcher sInstance = null;
	
	public synchronized static JobDispatcher getInstance() {
		if (sInstance == null) {
			sInstance = new JobDispatcher();
		}
		
		return sInstance;
	}
	
	private ScheduledThreadPoolExecutor networkThreadPool;
	private ScheduledThreadPoolExecutor processThreadPool;
	private ScheduledThreadPoolExecutor typesetThreadPool;
	private ScheduledThreadPoolExecutor packThreadPool;
	
	private JobDispatcher() {
		networkThreadPool = new ScheduledThreadPoolExecutor(4);
		processThreadPool = new ScheduledThreadPoolExecutor(1);
		typesetThreadPool = new ScheduledThreadPoolExecutor(1);
		packThreadPool = new ScheduledThreadPoolExecutor(2);
	}
	
	public void dispatch(int type, Runnable runnable) {
		switch (type) {
			case JobType.JOB_LOAD_WEBPAGE:
			case JobType.JOB_DOWNLOAD_IMAGE: {
				networkThreadPool.execute(runnable);
				break;
			}
			case JobType.JOB_PROCESS_IMAGE: {
				processThreadPool.execute(runnable);
				break;
			}
			case JobType.JOB_TYPESET_TEXT: {
				typesetThreadPool.execute(runnable);
				break;
			}
			case JobType.JOB_PACK_PDF: {
				packThreadPool.execute(runnable);
				break;
			}
			default: {
				break;
			}
		}
	}
	
	public void release() {
		networkThreadPool.shutdown();
		typesetThreadPool.shutdown();
		packThreadPool.shutdown();
	}
}