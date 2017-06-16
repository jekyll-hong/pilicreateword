package com.tcl.pili;

import java.io.File;

import com.abbyy.ocrsdk.Client;
import com.abbyy.ocrsdk.ProcessingSettings;
import com.abbyy.ocrsdk.Task;

final class AbbyyOCR implements OCRInterface {
	private Client client;
	
	public AbbyyOCR() {
		client = new Client();
		client.applicationId = "pilicreateworld";
		client.password = "9LFnguxkQ7UzXy1tofUGJPKm";
	}
	
	public void process(File image, File text) {
		ProcessingSettings settings = new ProcessingSettings();
		settings.setLanguage("ChineseTaiwan");
		settings.setOutputFormat(ProcessingSettings.OutputFormat.txt);
		
		try {
			Task task = client.processImage(image.getPath(), settings);
			
			do {
				try {
					Thread.sleep(2000);
				}
				catch (InterruptedException e) {
				}
				
				task = client.getTaskStatus(task.Id);
			}
			while (task.isTaskActive());
			
			if (task.Status == Task.TaskStatus.Completed) {
				client.downloadResult(task, text.getPath());
			}
			
		}
		catch (Exception e) {
			System.out.print("abbyy ocr exception " + e.getMessage());
		}
	}
}