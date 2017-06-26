package org.pilicreateworld.typeset;

import java.util.HashMap;

public class TypesetParamter {
	public static final String KEY_TOP_MARGIN = "top_margin";
	public static final String KEY_BOTTOM_MARGIN = "bottom_margin";
	public static final String KEY_LEFT_MARGIN = "left_margin";
	public static final String KEY_RIGHT_MARGIN = "right_margin";
	public static final String KEY_LINES_PER_PAGE = "lines_per_page";
	public static final String KEY_LINE_SPACE = "line_space";
	public static final String KEY_WORDS_PER_LINE = "words_per_line";
	public static final String KEY_WORD_SPACE = "word_space";
	
	private HashMap<String, Integer> mTable;
	
	public TypesetParamter() {
		mTable = new HashMap<String, Integer>();
	}
	
	public void set(String key, int value) {
		mTable.put(key, value);
	}
	
	public int get(String key) {
		return mTable.get(key);
	}
}