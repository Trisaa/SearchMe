package com.solo.search.util;

public class SearchPatternLevel {

	public static final int SEARCH_LEVEL_NUM = 4;

	public static final int NONE = 0;
	public static final int SEARCH_CONTAINS_EACH_CHAR = 1;
	public static final int SEARCH_CONTAINS_TEXT = 2;
	public static final int SEARCH_CONTAINS_WORD_STARTS_WITH_TEXT = 3;
	public static final int SEARCH_STARTS_WITH_TEXT = 4;

	public static int next(int level) {
		switch (level) {
		case SEARCH_STARTS_WITH_TEXT:
			return SEARCH_CONTAINS_WORD_STARTS_WITH_TEXT;
		case SEARCH_CONTAINS_WORD_STARTS_WITH_TEXT:
			return SEARCH_CONTAINS_TEXT;
		case SEARCH_CONTAINS_TEXT:
			return SEARCH_CONTAINS_EACH_CHAR;
		case SEARCH_CONTAINS_EACH_CHAR:
			return NONE;
		default:
			return NONE;
		}
	}
}
