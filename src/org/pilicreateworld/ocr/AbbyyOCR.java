package org.pilicreateworld.ocr;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;

import com.abbyy.ocrsdk.Client;
import com.abbyy.ocrsdk.ProcessingSettings;
import com.abbyy.ocrsdk.Task;

class AbbyyOCR implements OCRInterface {
	private Client client;
	
	public AbbyyOCR() {
		client = new Client();
		client.applicationId = "pilicreateworld";
		client.password = "9LFnguxkQ7UzXy1tofUGJPKm";
	}
	
	public String process(File image) {
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
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				client.downloadResult(task, out);
				
				byte[] content = out.toByteArray();
				return new String(content, Charset.forName("UTF-16"));
			}
		}
		catch (Exception e) {
			System.out.print("abbyy ocr exception " + e.getMessage());
		}
		
		return "";
	}
}