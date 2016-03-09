package com.solo.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.solo.search.card.CardConfig;
import com.solo.search.card.StockCard;
import com.solo.search.card.entry.StockEntry;
import com.solo.search.card.model.CardItem;
import com.solo.search.card.model.StockItem;
import com.solo.search.db.CardDBHelper;
import com.solo.search.util.DeviceUtils;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchConfig;
import com.solo.search.util.SharedPreferencesHelper;
import com.solo.search.widget.Titlebar;
import com.solo.search.widget.Titlebar.OnTitlebarClickListener;

public class StockManageActivity extends Activity implements OnClickListener, OnTitlebarClickListener {

	private static final String URL_SUGGESS_STOCKS = "https://s.yimg.com/aq/autoc?query={0}&region={1}&callback=YAHOO.util.UHScriptNodeDataSource.callbacks";
	private static final String RESULT_PREFIX = "{\"Query\"";
	private static final String RESULT_SUFFIX = "})";

	private static final int MSG_LOAD_LOCAL_STOCKS_SUCC = 0;
	private static final int MSG_LOAD_LOCAL_STOCKS_FAIL = 1;
	private static final int MSG_ADD_STOCK_SUCC = 2;
	private static final int MSG_ADD_STOCK_FAIL = 3;
	private static final int MSG_LOAD_HOT_STOCKS_SUCC = 4;
	private static final int MSG_LOAD_HOT_STOCKS_FAIL = 5;

	private static final int HOT_STOCKS_SIZE = 6;
	private static final String[] DEFAULT_STOCK_TITLES = { "Apple", "Google", "Amazon", "eBay", "IBM", "Facebook", };
	private static final String[] DEFAULT_STOCK_SYMBOLS = { "AAPL", "GOOG", "AMZN", "EBAY", "IBM", "FB" };

	private Titlebar mTitlebar;
	private EditText mEditText;
	private TextView mAddBtn;
	private TextView[] mStockTvs;
	private FrameLayout mSuggessLayout;
	private ScrollView mContentLayout;
	private LinearLayout mLocalStocksLayout;
	private LinearLayout mStocksContainer;
	private View mLoadingBackground;
	private ProgressBar mLoadingBar;
	private ListView mSuggessListView;
	private TextView mSuggessStatusTv;

	private StockEntry mStockEntry;
	private ArrayList<Stock> mSuggessStocks;
	private ArrayList<Stock> mHotStocks;
	private ArrayList<CardItem> mLocalStocks;
	private boolean isLoading;
	private boolean isChangeData;

	private SuggestionAsyncTask mSuggestionAsyncTask;
	private SuggessStocksAdapter mSuggessStocksAdapter;

	private RequestQueue mRequestQueue;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_LOAD_LOCAL_STOCKS_SUCC:
				if (mLocalStocks == null || mLocalStocks.isEmpty()) {
					mLocalStocksLayout.setVisibility(View.GONE);
				} else {
					mLocalStocksLayout.setVisibility(View.VISIBLE);
					for (CardItem item : mLocalStocks) {
						inflateCardItemView((StockItem) item);
					}
				}
				break;
			case MSG_LOAD_LOCAL_STOCKS_FAIL:
				stopLoadingBar();
				mLocalStocksLayout.setVisibility(View.GONE);
				break;
			case MSG_ADD_STOCK_SUCC:
				mLocalStocksLayout.setVisibility(View.VISIBLE);
				stopLoadingBar();
				StockItem item = (StockItem) msg.obj;
				Resources res = getResources();
				if (!existed(item)) {
					if (mLocalStocks == null) {
						mLocalStocks = new ArrayList<CardItem>();
					}
					isChangeData = true;
					mLocalStocks.add(item);
					inflateCardItemView(item);
					updateStockEntryDb();
					Toast.makeText(
							StockManageActivity.this,
							res.getString(ResourceUtil.getStringId(StockManageActivity.this, "ssearch_add_stock_succ")),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(
							StockManageActivity.this,
							res.getString(ResourceUtil.getStringId(StockManageActivity.this,
									"ssearch_add_stock_existed")), Toast.LENGTH_SHORT).show();
				}

				break;
			case MSG_ADD_STOCK_FAIL:
				stopLoadingBar();
				String code = (String) msg.obj;
				Toast.makeText(
						StockManageActivity.this,
						StockManageActivity.this.getResources().getString(
								ResourceUtil.getStringId(StockManageActivity.this, "ssearch_add_stock_fail"), code), Toast.LENGTH_SHORT)
						.show();
				break;
			case MSG_LOAD_HOT_STOCKS_SUCC:
				setupHotStocksLayout();
				break;
			case MSG_LOAD_HOT_STOCKS_FAIL:
				if (mHotStocks != null) {
					mHotStocks = null;
				}
				mHotStocks = new ArrayList<Stock>();
				for (int i = 0; i < DEFAULT_STOCK_TITLES.length; i++) {
					String title = DEFAULT_STOCK_TITLES[i];
					String symbol = DEFAULT_STOCK_SYMBOLS[i];
					Stock stock = new Stock(title, symbol);
					mHotStocks.add(stock);
				}
				setupHotStocksLayout();
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(ResourceUtil.getLayoutId(this, "ssearch_activity_stock_manage"));

		initView();
		setListener();
		initData(this);
	}

	private void initView() {
		mTitlebar = (Titlebar) findViewById(ResourceUtil.getId(this, "titlebar"));
		mEditText = (EditText) findViewById(ResourceUtil.getId(this, "stock_edit_text"));
		mAddBtn = (TextView) findViewById(ResourceUtil.getId(this, "stock_add_btn"));
		mLoadingBackground = (View) findViewById(ResourceUtil.getId(this, "bg_layout"));
		mContentLayout = (ScrollView) findViewById(ResourceUtil.getId(this, "content_layout"));
		mSuggessLayout = (FrameLayout) findViewById(ResourceUtil.getId(this, "suggess_stocks_layout"));
		mLocalStocksLayout = (LinearLayout) findViewById(ResourceUtil.getId(this, "card_stocks_layout"));
		mStocksContainer = (LinearLayout) findViewById(ResourceUtil.getId(this, "card_stock_container"));
		mLoadingBar = (ProgressBar) findViewById(ResourceUtil.getId(this, "loading_bar"));

		mSuggessListView = (ListView) findViewById(ResourceUtil.getId(this, "suggess_list_view"));
		mSuggessStatusTv = (TextView) findViewById(ResourceUtil.getId(this, "suggess_status"));
		mStockTvs = new TextView[DEFAULT_STOCK_TITLES.length];

		mStockTvs[0] = (TextView) findViewById(ResourceUtil.getId(this, "stock_tv1"));
		mStockTvs[1] = (TextView) findViewById(ResourceUtil.getId(this, "stock_tv2"));
		mStockTvs[2] = (TextView) findViewById(ResourceUtil.getId(this, "stock_tv3"));
		mStockTvs[3] = (TextView) findViewById(ResourceUtil.getId(this, "stock_tv4"));
		mStockTvs[4] = (TextView) findViewById(ResourceUtil.getId(this, "stock_tv5"));
		mStockTvs[5] = (TextView) findViewById(ResourceUtil.getId(this, "stock_tv6"));

		mLoadingBackground.setVisibility(View.GONE);
		mLoadingBar.setVisibility(View.GONE);

		mTitlebar.setTitle(getString(ResourceUtil.getStringId(StockManageActivity.this, "card_stock")));
		mTitlebar.setOnTitlebarClickListener(this);
	}

	private void setListener() {
		mAddBtn.setOnClickListener(this);

		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				resetSearch();
				if (TextUtils.isEmpty(s)) {
					mContentLayout.setVisibility(View.VISIBLE);
					mSuggessLayout.setVisibility(View.GONE);
					updateSearchStatus("");
				} else {
					mContentLayout.setVisibility(View.GONE);
					searchStocks(s.toString());
				}
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mSuggessListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				hideInputMethod();
				mContentLayout.setVisibility(View.VISIBLE);
				mSuggessLayout.setVisibility(View.GONE);
				addStock(mSuggessStocks.get(position).symbol);
			}
		});
	}

	private void setupHotStocksLayout() {
		for (int i = 0; i < HOT_STOCKS_SIZE; i++) {
			mStockTvs[i].setText(mHotStocks.get(i).title);
			mStockTvs[i].setTag(mHotStocks.get(i).symbol);
			mStockTvs[i].setOnClickListener(this);
		}
	}

	private void inflateCardItemView(final StockItem item) {
		final View view = getLayoutInflater().inflate(ResourceUtil.getLayoutId(this, "ssearch_card_stock_add_item"), null);
		TextView titleTv = (TextView) view.findViewById(ResourceUtil.getId(this, "stock_title"));
		ImageView deleteBtn = (ImageView) view.findViewById(ResourceUtil.getId(this, "stock_right_btn"));

		titleTv.setText(item.getTitle());
		deleteBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final Resources res = getResources();
				if (mLocalStocks.size() > 1) {
					mStocksContainer.removeView(view);
					removeCardItem(item);
				} else {
					Toast.makeText(StockManageActivity.this,
							res.getString(ResourceUtil.getStringId(StockManageActivity.this, "ssearch_stock_down")), Toast.LENGTH_SHORT)
							.show();
				}
			}
		});
		view.setTag(item.getSymbol());
		mStocksContainer.addView(view);
	}

	private void loadHotStocks() {
		String url = CardConfig.buildHotStocksUrl(this);
		StringRequest request = new StringRequest(Method.GET, url, new Listener<String>() {

			@Override
			public void onResponse(String result) {
				Message msg = new Message();
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONArray array = new JSONArray(result);
						mHotStocks = new ArrayList<Stock>();
						for (int i = 0; i < array.length(); i++) {
							JSONObject itemObj = array.getJSONObject(i);
							String title = itemObj.getString("title");
							String symbol = itemObj.getString("symbol");
							Stock item = new Stock(title, symbol);
							mHotStocks.add(item);
						}
						if (mHotStocks.size() >= HOT_STOCKS_SIZE) {
							msg.what = MSG_LOAD_HOT_STOCKS_SUCC;
						} else {
							msg.what = MSG_LOAD_HOT_STOCKS_FAIL;
						}

					} catch (JSONException e) {
						e.printStackTrace();
						msg.what = MSG_LOAD_HOT_STOCKS_FAIL;
					}
				} else {
					msg.what = MSG_LOAD_HOT_STOCKS_FAIL;
				}
				mHandler.sendMessage(msg);
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				Message msg = new Message();
				msg.what = MSG_LOAD_HOT_STOCKS_FAIL;
				mHandler.sendMessage(msg);
			}
		});
		mRequestQueue.add(request);

	}

	private void loadLocalStocks(final Context context) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				CardDBHelper dbHelper = new CardDBHelper(StockManageActivity.this);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				Cursor cursor = db.query(CardDBHelper.TABLE_NAME, CardConfig.CARD_DB_PROJECTION, CardConfig.CARD_ID + " = ?",
						new String[] { String.valueOf(CardConfig.CARD_ID_STOCK) }, null, null, null);

				if (cursor != null) {
					if (cursor.moveToFirst()) {
						mStockEntry = new StockEntry(context, cursor);
					}
					cursor.close();
				}

				Message msg = mHandler.obtainMessage();
				if (mStockEntry == null) {
					msg.what = MSG_LOAD_LOCAL_STOCKS_FAIL;
				} else {
					mLocalStocks = mStockEntry.getCardItems();
					msg.what = MSG_LOAD_LOCAL_STOCKS_SUCC;
				}
				db.close();
				mHandler.sendMessage(msg);
			}
		}).start();

	}

	private void initData(Context context) {
		mRequestQueue = Volley.newRequestQueue(this);
		mSuggessStocks = new ArrayList<Stock>();
		mSuggessStocksAdapter = new SuggessStocksAdapter(this);
		mSuggessListView.setAdapter(mSuggessStocksAdapter);

		loadHotStocks();
		loadLocalStocks(context);
	}

	private boolean existed(StockItem item) {
		if (mLocalStocks != null) {
			for (CardItem temp : mLocalStocks) {
				StockItem tempItem = (StockItem) temp;
				if (item.getSymbol().equalsIgnoreCase(tempItem.getSymbol())) {
					return true;
				}
			}
		}
		return false;
	}

	private void removeCardItem(StockItem item) {
		isChangeData = true;
		int deletedIndex = -1;
		for (int i = 0; i < mLocalStocks.size(); i++) {
			StockItem temp = (StockItem) mLocalStocks.get(i);
			if (item.getSymbol().equals(temp.getSymbol())) {
				deletedIndex = i;
				break;
			}
		}
		if (deletedIndex != -1) {
			mLocalStocks.remove(deletedIndex);
			updateStockEntryDb();
		}

		if (mLocalStocks.size() == 0) {
			mLocalStocksLayout.setVisibility(View.GONE);
		}
	}

	private void updateSavedStocksSymbols() {
		if (mLocalStocks != null) {
			StringBuilder sb = new StringBuilder();
			for (CardItem temp : mLocalStocks) {
				StockItem item = (StockItem) temp;
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(item.getSymbol());
			}
			String savedStr = sb.toString();
			if (TextUtils.isEmpty(savedStr)) {
				savedStr = CardConfig.DEFAULT_STOCKS_SYMBOLS;
			}

			SharedPreferencesHelper.setString(this, SearchConfig.KEY_STOCKS, savedStr);
		}
	}

	private void updateStockEntryDb() {
		JSONArray jsArray = new JSONArray();
		if (mStockEntry != null && mLocalStocks != null && mLocalStocks.size() > 0) {
			for (int i = 0; i < mLocalStocks.size(); i++) {
				StockItem item = (StockItem) mLocalStocks.get(i);
				JSONObject object = item.getContentJSONSObject();
				if (object != null) {
					jsArray.put(object);
				}
			}
		}
		try {
			mStockEntry.getCardData().put(CardConfig.CARD_ITEMS_DATA, jsArray);
			CardDBHelper dbHelper = new CardDBHelper(StockManageActivity.this);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.update(CardDBHelper.TABLE_NAME, mStockEntry.getContentValues(), CardConfig.CARD_ID + " = ?",
					new String[] { CardConfig.CARD_ID_STOCK });
			db.close();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		updateSavedStocksSymbols();
	}

	private void hideInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
		}
	}

	private String buildStockUrl(String symbol) {
		int version_code = DeviceUtils.getVersionCode(this, getPackageName());
		String isoCode = DeviceUtils.getCountryISOCode(this);
		String url = CardConfig.URL_STOCK.replace("{0}", symbol).replace("{1}", isoCode).replace("{2}",
				DeviceUtils.getLocaleLanguage(this)).replace("{3}", String.valueOf(version_code)).replace("{4}",
				DeviceUtils.getDeviceUUID(this));
		return url;
	}

	private void addStock(final String symbol) {
		if (!DeviceUtils.isNetConnected(this) || mLoadingBar.getVisibility() == View.VISIBLE) {
			Toast.makeText(this, getString(ResourceUtil.getStringId(this, "ssearch_network_invalid")), Toast.LENGTH_SHORT).show();
			return;
		}

		if (mLocalStocks != null && mLocalStocks.size() >= StockCard.MAX_STOCKS_SIZE) {
			Toast.makeText(this, getString(ResourceUtil.getStringId(this, "ssearch_stock_up"), StockCard.MAX_STOCKS_SIZE),
					Toast.LENGTH_SHORT).show();
		} else {
			startLoadingBar();
			String url = buildStockUrl(symbol.toUpperCase());
			StringRequest request = new StringRequest(Method.GET, url, new Listener<String>() {

				@Override
				public void onResponse(String result) {
					StockItem item = null;
					if (!TextUtils.isEmpty(result)) {
						JSONObject jsObject = null;
						try {
							jsObject = new JSONObject(result);
							item = new StockItem(jsObject);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					if (item != null && item.isValid()) {
						Message msg = new Message();
						msg.obj = item;
						msg.what = MSG_ADD_STOCK_SUCC;
						mHandler.sendMessage(msg);
					} else {
						Message msg = new Message();
						msg.obj = symbol;
						msg.what = MSG_ADD_STOCK_FAIL;
						mHandler.sendMessage(msg);
					}

				}
			}, new ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError arg0) {
					Message msg = new Message();
					msg.obj = symbol;
					msg.what = MSG_ADD_STOCK_FAIL;
					mHandler.sendMessage(msg);
				}
			});

			mRequestQueue.add(request);
		}
	}

	private void stopLoadingBar() {
		mLoadingBackground.setVisibility(View.GONE);
		mLoadingBar.setVisibility(View.GONE);
		isLoading = false;
	}

	private void startLoadingBar() {
		mLoadingBackground.requestFocusFromTouch();
		mLoadingBackground.setVisibility(View.VISIBLE);
		mLoadingBar.setVisibility(View.VISIBLE);
		isLoading = true;
	}

	private String buildSuggessStocksUrl(String keyword) {
		String isoCode = DeviceUtils.getCountryISOCode(this);
		String url = URL_SUGGESS_STOCKS.replace("{0}", keyword).replace("{1}", isoCode);
		return url;
	}

	private void updateSearchStatus(String status) {
		mSuggessStatusTv.setText(status);
	}

	private void resetSearch() {
		if (mSuggestionAsyncTask != null && !mSuggestionAsyncTask.isCancelled()) {
			mSuggestionAsyncTask.cancel(true);
			mSuggestionAsyncTask = null;
		}

		mSuggessStocks.clear();
		mSuggessStocksAdapter.notifyDataSetChanged();
		updateSearchStatus("");
	}

	private void searchStocks(String keyword) {
		if (!TextUtils.isEmpty(keyword)) {
			updateSearchStatus(getResources().getString(ResourceUtil.getStringId(this, "ssearch_searching")));
			mSuggessLayout.setVisibility(View.GONE);
			mSuggestionAsyncTask = new SuggestionAsyncTask(this);
			mSuggestionAsyncTask.execute(buildSuggessStocksUrl(keyword));
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == ResourceUtil.getId(this, "stock_add_btn")) {
			if (mEditText.getText().length() > 0 && mLoadingBar.getVisibility() != View.VISIBLE) {
				resetSearch();
				mContentLayout.setVisibility(View.VISIBLE);
				mSuggessLayout.setVisibility(View.GONE);
				hideInputMethod();
				addStock(mEditText.getText().toString());
			}
		} else if (id == ResourceUtil.getId(this, "stock_tv1") || id == ResourceUtil.getId(this, "stock_tv1")
				|| id == ResourceUtil.getId(this, "stock_tv2") || id == ResourceUtil.getId(this, "stock_tv3")
				|| id == ResourceUtil.getId(this, "stock_tv4") || id == ResourceUtil.getId(this, "stock_tv5")
				|| id == ResourceUtil.getId(this, "stock_tv6")) {
			if (mLoadingBar.getVisibility() != View.VISIBLE) {
				String code = (String) v.getTag();
				addStock(code);
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		mRequestQueue.cancelAll(this);
	}

	@Override
	public void onBackPressed() {
		if (mContentLayout.getVisibility() == View.GONE) {
			mContentLayout.setVisibility(View.VISIBLE);
			mSuggessLayout.setVisibility(View.GONE);
		} else {
			if (!isLoading) {
				if (isChangeData) {
					setResult(RESULT_OK);
				}
				finish();
			}
		}
	}

	class Stock {
		public String title;
		public String symbol;

		public Stock(String title, String symbol) {
			this.title = title;
			this.symbol = symbol;
		}
	}

	private class SuggestionAsyncTask extends AsyncTask<String, Integer, ArrayList<Stock>> {

		private Context mContext;

		public SuggestionAsyncTask(Context context) {
			mContext = context;
		}

		@Override
		protected ArrayList<Stock> doInBackground(String... urlStr) {
			ArrayList<Stock> stocks = new ArrayList<Stock>();
			try {
				HttpURLConnection urlConnection = null;
				URL url = new URL(urlStr[0]);
				urlConnection = (HttpURLConnection) url.openConnection();
				BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				mSuggessStocks.clear();
				String result = sb.toString();
				if (result.contains(RESULT_PREFIX) || result.contains(RESULT_SUFFIX)) {
					String finalResult = result.substring(result.indexOf(RESULT_PREFIX), result.lastIndexOf(RESULT_SUFFIX));
					try {
						JSONObject object = new JSONObject(finalResult);
						JSONArray array = object.getJSONArray("Result");
						for (int i = 0; i < array.length(); i++) {
							JSONObject item = array.getJSONObject(i);
							String title = item.getString("name");
							String symbol = item.getString("symbol");
							Stock stock = new Stock(title, symbol);
							stocks.add(stock);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return stocks;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(ArrayList<Stock> stocks) {
			super.onPostExecute(stocks);
			mSuggessStocks = stocks;
			if (!isCancelled()) {
				mSuggessStocksAdapter.notifyDataSetChanged();
				if (mSuggessStocks.isEmpty()) {
					mContentLayout.setVisibility(View.GONE);
					mSuggessLayout.setVisibility(View.GONE);
					updateSearchStatus(mContext.getResources().getString(
							ResourceUtil.getStringId(StockManageActivity.this, "ssearch_no_result")));
				} else {
					updateSearchStatus("");
					mContentLayout.setVisibility(View.GONE);
					mSuggessLayout.setVisibility(View.VISIBLE);
					mSuggessStocksAdapter.notifyDataSetChanged();
				}
			} else {
				mSuggessLayout.setVisibility(View.VISIBLE);
			}
		}

	}

	private class SuggessStocksAdapter extends BaseAdapter {

		private Context mContext;

		private SuggessStocksAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			return mSuggessStocks.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(
						ResourceUtil.getLayoutId(StockManageActivity.this, "ssearch_card_stock_add_item"), null);
				holder.titleTv = (TextView) convertView.findViewById(ResourceUtil
						.getId(StockManageActivity.this, "stock_title"));
				holder.symbolTv = (TextView) convertView.findViewById(ResourceUtil.getId(StockManageActivity.this,
						"stock_symbol"));
				holder.addBtn = (ImageView) convertView.findViewById(ResourceUtil.getId(StockManageActivity.this,
						"stock_right_btn"));
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.titleTv.setText(mSuggessStocks.get(position).title);
			holder.symbolTv.setText(" - " + mSuggessStocks.get(position).symbol);
			holder.addBtn.setImageResource(ResourceUtil.getDrawableId(mContext, "ssearch_ic_add"));
			holder.addBtn.setBackground(null);

			return convertView;
		}

		class ViewHolder {
			TextView titleTv;
			TextView symbolTv;
			ImageView addBtn;
		}
	}

	@Override
	public void onBackButtonClick() {
		if (isChangeData) {
			setResult(RESULT_OK);
		}
		finish();
	}

	@Override
	public void onRightButtonClick() {
	}

}
