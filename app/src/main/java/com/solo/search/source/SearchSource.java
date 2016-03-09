package com.solo.search.source;

import android.content.Context;

public abstract class SearchSource {

	protected Context mContext;

	public SearchSource(Context context) {
		mContext = context;
	}

	/**
	 * 搜索引擎的首页
	 * 
	 * @return
	 */
	public abstract String getSearchHomeUrl();

	/**
	 * 搜索源根据关键字跳转到搜索页的url
	 * 
	 * @param keyWord
	 *            关键字
	 * @return
	 */
	public abstract String getSearchUrl(String keyWord);

	/**
	 * 获取keyword相对应的搜索源的url
	 * 
	 * @param keyWord
	 *            关键字
	 * @return
	 */
	public abstract String getSuggestionUrl(String keyWord);

	/**
	 * 获取请求搜索源后返回的与搜索关键字相关的suggestion数组（需要在此实现搜索源相关的解析逻辑）
	 * 
	 * @param resultString
	 *            从搜索源获取到的数据字符串
	 * @return 解析后的suggestion词汇数组
	 */
	public abstract String[] getSuggestions(String resultString);

}
