package com.solo.search;


import android.app.Application;

public class SearchApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		SoloSearch.initialize(this,"1004");
	}
}
