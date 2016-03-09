package com.solo.search.card;

import java.math.BigDecimal;
import java.util.ArrayList;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.solo.search.card.entry.CardEntry;
import com.solo.search.card.entry.CurrencyEntry;
import com.solo.search.card.model.CardItem;
import com.solo.search.card.model.CurrencyItem;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchConfig;
import com.solo.search.util.SharedPreferencesHelper;
import com.solo.search.widget.ListDialog;

public class CurrencyCard extends BaseCard {

	private CharSequence[] mCurrencyNames;
	private CharSequence[] mCurrencyValues;

	private View mMainView;
	private TextView mTitleTv;
	private TextView mTimeTv;
	private TextView mSourceCurrencyTv;
	private TextView mTargetCurrencyTv;
	private EditText mSourceAmountTv;
	private TextView mTargetAmountTv;

	private ListDialog mListDialog;
	private ArrayList<CardItem> mItems;

	private enum Type {
		SOURCE_CURRENCY, TARGET_CURRENCY
	};

	private Type mType = Type.SOURCE_CURRENCY;

	private String mSourceCurrencyValue;
	private String mTargetCurrencyValue;

	private double mCurrentFromCourseRate = 1.00;
	private double mCurrentToCourseRate = 1.00;

	public CurrencyCard(Context paramContext, CardEntry cardEntry) {
		super(paramContext, cardEntry);
	}

	@Override
	public String getCardId() {
		return CardConfig.CARD_ID_CURRENCY;
	}

	private CharSequence getCurrencyText(String key, String defaultValue) {
		String value = SharedPreferencesHelper.getString(mContext, key, defaultValue);
		for (int i = 0; i < mCurrencyValues.length; i++) {
			if (value.equals(mCurrencyValues[i])) {
				return mCurrencyNames[i];
			}
		}
		return defaultValue;
	}

	@Override
	public void buildCardView() {
		mMainView = mInflater.inflate(ResourceUtil.getLayoutId(mContext, "ssearch_card_currency"), null);
		mTitleTv = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "card_header_title"));
		mTimeTv = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "create_time"));
		mSourceCurrencyTv = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "source_currency"));
		mTargetCurrencyTv = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "target_currency"));
		mSourceAmountTv = (EditText) mMainView.findViewById(ResourceUtil.getId(mContext, "source_amount"));
		mTargetAmountTv = (TextView) mMainView.findViewById(ResourceUtil.getId(mContext, "target_amount"));

		if (mCardEntry == null) {
			mMainView.setVisibility(View.GONE);
			return;
		}

		mItems = mCardEntry.getCardItems();
		if (mItems == null || mItems.size() == 0) {
			mMainView.setVisibility(View.GONE);
			return;
		}

		int length = mItems.size();
		mCurrencyNames = new CharSequence[length];
		mCurrencyValues = new CharSequence[length];

		for (int i = 0; i < length; i++) {
			CurrencyItem curItem = (CurrencyItem) mItems.get(i);
			mCurrencyNames[i] = curItem.getTitle();
			mCurrencyValues[i] = curItem.getSymbol();
		}

		mTitleTv.setText(ResourceUtil.getStringId(mContext, "ssearch_card_currency"));
		mTimeTv.setText(((CurrencyEntry) mCardEntry).getCreateTime());

		setupRate();

		mListDialog = new ListDialog(mContext, mContext.getResources().getString(
				ResourceUtil.getStringId(mContext, "ssearch_currency")), mCurrencyNames, mCurrencyValues, null);
		mSourceCurrencyTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mType = Type.SOURCE_CURRENCY;
				mListDialog.setItemChecked(mSourceCurrencyValue);
				mListDialog.show();
			}
		});

		mTargetCurrencyTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mType = Type.TARGET_CURRENCY;
				mListDialog.setItemChecked(mTargetCurrencyValue);
				mListDialog.show();
			}
		});

		mTargetAmountTv.setOnKeyListener(null);

		mListDialog.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String key = (mType == Type.SOURCE_CURRENCY ? SearchConfig.KEY_SOURCE_CURRENCY
						: SearchConfig.KEY_TARGET_CURRENCY);
				SharedPreferencesHelper.setString(mContext, key, mCurrencyValues[position].toString());

				if (mType == Type.SOURCE_CURRENCY) {
					mSourceCurrencyTv.setText(getCurrencyText(SearchConfig.KEY_SOURCE_CURRENCY,
							SearchConfig.DEFAULT_SOURCE_CURRENCY));
				} else {
					mTargetCurrencyTv.setText(getCurrencyText(SearchConfig.KEY_TARGET_CURRENCY,
							SearchConfig.DEFAULT_TARGET_CURRENCY));
				}
				setupRate();
				updateTargetResult();

			}
		});

		mSourceAmountTv.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable arg0) {
				setupRate();
				updateTargetResult();
			}

			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
		});

		mSourceAmountTv.setText("1.000");
		updateTargetResult();
	}

	private void setupRate() {
		mSourceCurrencyValue = SharedPreferencesHelper.getString(mContext, SearchConfig.KEY_SOURCE_CURRENCY,
				SearchConfig.DEFAULT_SOURCE_CURRENCY);
		mTargetCurrencyValue = SharedPreferencesHelper.getString(mContext, SearchConfig.KEY_TARGET_CURRENCY,
				SearchConfig.DEFAULT_TARGET_CURRENCY);

		mSourceCurrencyTv.setText(getCurrencyText(SearchConfig.KEY_SOURCE_CURRENCY, SearchConfig.DEFAULT_SOURCE_CURRENCY));
		mTargetCurrencyTv.setText(getCurrencyText(SearchConfig.KEY_TARGET_CURRENCY, SearchConfig.DEFAULT_TARGET_CURRENCY));

		if (mCardEntry != null && mCardEntry instanceof CurrencyEntry) {
			ArrayList<CardItem> cardItems = mCardEntry.getCardItems();
			if (cardItems != null && cardItems.size() > 0) {
				for (CardItem item : cardItems) {
					final CurrencyItem currencyItem = (CurrencyItem) item;

					if (currencyItem.getSymbol().equals(mSourceCurrencyValue)) {
						mCurrentFromCourseRate = currencyItem.getRate();
					}

					if (currencyItem.getSymbol().equals(mTargetCurrencyValue)) {
						mCurrentToCourseRate = currencyItem.getRate();
					}
				}
			} else {
				mMainView.setVisibility(View.GONE);
			}
		} else {
			mMainView.setVisibility(View.GONE);
		}
	}

	private void updateTargetResult() {
		if (mSourceAmountTv.getText().length() == 0 || mSourceAmountTv.getText().toString().equals("-")) {
			mTargetAmountTv.setText("0.00");
			return;
		}

		try {
			BigDecimal x = new BigDecimal(Double.parseDouble(mSourceAmountTv.getText().toString()) * mCurrentToCourseRate
					/ mCurrentFromCourseRate);
			x = x.setScale(3, BigDecimal.ROUND_HALF_UP);
			mTargetAmountTv.setText(x.toString());
		} catch (Exception ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public View getCardView() {
		return mMainView;
	}

}
