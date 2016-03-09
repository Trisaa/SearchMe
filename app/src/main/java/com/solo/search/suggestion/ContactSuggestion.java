package com.solo.search.suggestion;

import android.graphics.drawable.Drawable;
import android.net.Uri;

public class ContactSuggestion extends Suggestion {

	private ContactSuggestions mSuggestions;
	private Uri mLookupUri;

	public ContactSuggestion(ContactSuggestions suggestions, int id,
			String title, String infoText, Uri lookupUri) {
		super(suggestions, id, title, infoText);
		mSuggestions = (ContactSuggestions) suggestions;
		mLookupUri = lookupUri;
	}

	public boolean smsTo() {
		return mSuggestions.smsTo(this);
	}

	public boolean dial() {
		return mSuggestions.dial(this);
	}

	@Override
	public Drawable getIcon() {
		return mSuggestions.getIcon(this);
	}

	public Uri getLookupUri() {
		return mLookupUri;
	}

	public int hashCode() {
		return super.hashCode();
	}

	public boolean equals(Object o) {
		return super.equals(o);
	}

}
