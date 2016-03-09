package com.solo.search.card;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.solo.search.card.entry.CardEntry;
import com.solo.search.util.ResourceUtil;

public abstract class BaseCard {

	protected Context mContext;
	protected LayoutInflater mInflater;
	protected CardEntry mCardEntry;

	public BaseCard(Context paramContext, CardEntry cardEntry) {
		mContext = paramContext;
		mInflater = LayoutInflater.from(mContext);
		setCardEntry(cardEntry);
	}

	public void setCardEntry(CardEntry cardEntry) {
		mCardEntry = cardEntry;
		buildCardView();
	}

	public CardEntry getCardEntry() {
		return mCardEntry;
	}

	protected void startAppearAnimation(final View view) {
		view.startAnimation(AnimationUtils.loadAnimation(mContext, ResourceUtil.getAnimId(mContext,
				"ssearch_refresh_card_appear")));
	}

	protected void startDisappearAnimation(final View view) {
		view.startAnimation(AnimationUtils.loadAnimation(mContext, ResourceUtil.getAnimId(mContext,
				"ssearch_refresh_card_disappear")));
	}

	public abstract String getCardId();

	public abstract void buildCardView();

	public abstract View getCardView();

}
