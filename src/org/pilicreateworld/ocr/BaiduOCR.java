package org.pilicreateworld.ocr;

import java.io.File;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.baidu.aip.ocr.AipOcr;

class BaiduOCR implements OCRInterface {
	private AipOcr client;
	
	public BaiduOCR() {
		client = new AipOcr("9768716", "vYBvRadjOeOXhnjXZOG7hAOV", "E0lAKr5ldKj1buRDtDwTnuAPdbDehopK");
		client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
	}
	
	public String process(File image) {
		HashMap<String, String> options = new HashMap<String, String>();
	    options.put("detect_direction", "false");
	    options.put("language_type", "CHN_ENG");
	    
        JSONObject response = client.basicGeneral(image.getPath(), options);
        if (response != null) {
        	JSONArray words = response.getJSONArray("words_result");
        	if ((words != null) && (words.length() > 0)) {
        		return writeText(words);
        	}
        }
        
        return "";
	}
	
	private String writeText(JSONArray words) {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < words.length(); i++) {
			JSONObject word = words.getJSONObject(i);
			builder.append(word.getString("words"));
			builder.append("\r\n");
		}
		
		return builder.toString();
	}
}