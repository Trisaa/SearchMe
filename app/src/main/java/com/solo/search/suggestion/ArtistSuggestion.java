package com.solo.search.suggestion;

import android.graphics.drawable.Drawable;

public class ArtistSuggestion extends Suggestion {

	private final ArtistSuggestions mArtistSuggestions;

	public ArtistSuggestion(Suggestions suggestions, int id, String title) {
		super(suggestions, id, title);
		mArtistSuggestions = (ArtistSuggestions) suggestions;
	}

	@Override
	public Drawable getIcon() {
		return mArtistSuggestions.getIcon();
	}

	public int hasCode() {
		return super.hashCode();
	}

	public boolean equals(Object object) {
		return super.equals(object);
	}

}
