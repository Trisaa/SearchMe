package com.solo.search.suggestion;

import android.graphics.drawable.Drawable;

public class MusicSuggestion extends Suggestion {

	private MusicSuggestions mMusicSuggestions;

	public MusicSuggestion(Suggestions suggestions, int id, String title,
			String infoText) {
		super(suggestions, id, title, infoText);
		mMusicSuggestions = (MusicSuggestions) suggestions;
	}

	@Override
	public Drawable getIcon() {
		return mMusicSuggestions.getIcon(this);
	}

	public int hashCode() {
		return super.hashCode();
	}

	public boolean equals(Object o) {
		return super.equals(o);
	}

}
