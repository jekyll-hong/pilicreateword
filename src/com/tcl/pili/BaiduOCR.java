package com.tcl.pili;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.baidu.aip.ocr.AipOcr;

final class BaiduOCR implements OCRInterface {
	private AipOcr client;
	
	public BaiduOCR() {
		client = new AipOcr("9768716", "vYBvRadjOeOXhnjXZOG7hAOV", "E0lAKr5ldKj1buRDtDwTnuAPdbDehopK");
		client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
	}
	
	public void process(File image, File text) {
		HashMap<String, String> options = new HashMap<String, String>();
	    options.put("detect_direction", "false");
	    options.put("language_type", "CHN_ENG");
	    
        JSONObject response = client.basicGeneral(image.getPath(), options);
        if (response != null) {
        	JSONArray words = response.getJSONArray("words_result");
        	if ((words != null) && (words.length() > 0)) {
        		try {
        			writeText(words, text);
        		}
        		catch (IOException e) {
        		}
        	}
        }
	}
	
	private void writeText(JSONArray words, File text) throws IOException {
		FileWriter writer = new FileWriter(text);
		
		for (int i = 0; i < words.length(); i++) {
			JSONObject word = words.getJSONObject(i);
			writer.write(word.getString("words"));
			writer.write("\r\n");
		}
		
		writer.close();
	}
}
