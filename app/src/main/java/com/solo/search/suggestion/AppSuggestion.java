package com.solo.search.suggestion;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class AppSuggestion extends Suggestion {

	private final Intent mIntent;
	private final Drawable mIcon;

	public AppSuggestion(Suggestions suggestions, int id, String title,
			Intent intent, Drawable icon) {
		super(suggestions, id, title);
		mIntent = intent;
		mIcon = icon;
	}

	@Override
	public Drawable getIcon() {
		return mIcon;
	}

	public Intent getIntent() {
		return mIntent;
	}

	public int hasCode() {
		return super.hashCode();
	}

	public boolean equals(Object o) {
		return super.equals(o);
	}

}
