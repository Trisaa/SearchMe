package com.solo.search.suggestion;

import android.graphics.drawable.Drawable;

public class MessageSuggestion extends Suggestion {

	private MessageSuggestions mSuggestions;

	public MessageSuggestion(MessageSuggestions suggestions, int id, String title, String infoText) {
		super(suggestions, id, title, infoText);
		mSuggestions = (MessageSuggestions) suggestions;
	}

	@Override
	public Drawable getIcon() {
		return mSuggestions.getIcon(this);
	}

	public int hashCode() {
		return super.hashCode();
	}

	public boolean equals(Object o) {
		return super.equals(o);
	}

}
