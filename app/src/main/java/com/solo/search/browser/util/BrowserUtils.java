package com.solo.search.browser.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class BrowserUtils {

	private BrowserUtils() {
	}

	public static Intent newEmailIntent(Context context, String address, String subject, String body, String cc) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
		intent.putExtra(Intent.EXTRA_TEXT, body);
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_CC, cc);
		intent.setType("message/rfc822");
		return intent;
	}

	public static Intent newTelIntent(String telurl) {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		// FIXME : need to check XXX is really a short number in tel:XXX
		intent.setData(Uri.parse(telurl));
		return intent;
	}

}
