package com.tcl.pili;

import java.io.File;

public interface WebsiteParseListener {
	void onDrama(File dir);
	void onEpisode(File dir);
	void onParseCompleted();
}
