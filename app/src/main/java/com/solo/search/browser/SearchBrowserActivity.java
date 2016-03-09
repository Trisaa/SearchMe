package com.solo.search.browser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.VideoView;

import com.solo.search.browser.util.BrowserConstants;
import com.solo.search.browser.util.BrowserController;
import com.solo.search.browser.widget.AssistInputBar;
import com.solo.search.model.MenuItem;
import com.solo.search.source.SearchSource;
import com.solo.search.util.IntentUtils;
import com.solo.search.util.LogUtils;
import com.solo.search.util.ResourceUtil;
import com.solo.search.util.SearchHelper;
import com.solo.search.widget.AnimatedProgressBar;
import com.solo.search.widget.PopupMenuWindow;
import com.solo.search.widget.PopupMenuWindow.MenuType;
import com.solo.search.widget.PopupMenuWindow.OnMenuItemClickListener;
import com.solo.search.widget.scrollview.ObservableScrollViewCallbacks;
import com.solo.search.widget.scrollview.ScrollState;

public class SearchBrowserActivity extends Activity implements BrowserController, OnClickListener,
		ObservableScrollViewCallbacks {

	private static final String TAG = LogUtils.makeLogTag(SearchBrowserActivity.class);

	private static final int MENU_REFRESH = 0;
	private static final int MENU_SHARE = 1;
	private static final int MENU_COPY_URL = 2;
	private static final int MENU_OPEN_WITH_BROWSER = 3;

	private View mRootView;
	private LinearLayout mActionbarLayout;
	private LinearLayout mContentContainer;
	private LinearLayout mMenuLayout;
	private LinearLayout mClearLayout;
	private EditText mSearchText;
	private AssistInputBar mAssistInputBar;
	private SearchBrowserView mBrowserView;
	private AnimatedProgressBar mProgressBar;
	private VideoView mVideoView;
	private View mCustomView, mVideoProgressView;

	private FullscreenHolder mFullscreenContainer;
	private Bitmap mDefaultVideoPoster;
	private SearchSource mSearchSource;
	private int mOriginalOrientation;
	private CustomViewCallback mCustomViewCallback;

	private PopupMenuWindow mPopupMenuWindow;
	private boolean isFullScreen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Intent intent = getIntent();
		String url = intent.getStringExtra(IntentUtils.EXTRA_SEARCH_BROWSER_URL);
		if (TextUtils.isEmpty(url)) {
			finish();
		}

		isFullScreen = intent.getBooleanExtra(IntentUtils.EXTRA_BROWSER_FULL_SCREEN, false);
		if (isFullScreen) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		setContentView(ResourceUtil.getLayoutId(this, "ssearch_activity_search_browser"));

		initPopupMenu();
		initView();
		setListener();

		if (!TextUtils.isEmpty(url)) {
			searchTheWeb(url);
		}

		mSearchSource = SearchHelper.getSearchSource(this);

	}

	private void initView() {
		mRootView = findViewById(ResourceUtil.getId(this, "root_view"));
		mActionbarLayout = (LinearLayout) findViewById(ResourceUtil.getId(this, "actionbar_layout"));
		mProgressBar = (AnimatedProgressBar) findViewById(ResourceUtil.getId(this, "progress_bar"));
		mBrowserView = (SearchBrowserView) findViewById(ResourceUtil.getId(this, "browserview"));
		mBrowserView.setBrowserController(this);

		mMenuLayout = (LinearLayout) findViewById(ResourceUtil.getId(this, "search_menu_layout"));
		mClearLayout = (LinearLayout) findViewById(ResourceUtil.getId(this, "search_clear_layout"));
		mSearchText = (EditText) findViewById(ResourceUtil.getId(this, "search_edit"));
		mContentContainer = (LinearLayout) findViewById(ResourceUtil.getId(this, "content_container"));

		mAssistInputBar = (AssistInputBar) findViewById(ResourceUtil.getId(this, "assist_input_bar"));
		mAssistInputBar.setEditTextView(mSearchText);
		mAssistInputBar.setWebView(mBrowserView);

		if (isFullScreen) {
			mActionbarLayout.setVisibility(View.GONE);

			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mContentContainer.getLayoutParams();
			lp.topMargin = 0;
			mContentContainer.requestLayout();
		}
	}

	private void setListener() {
		mMenuLayout.setOnClickListener(this);
		mMenuLayout.setOnClickListener(this);
		mClearLayout.setOnClickListener(this);

		SearchTextListener search = new SearchTextListener();
		mSearchText.setOnKeyListener(search.new KeyListener());
		mSearchText.setOnFocusChangeListener(search.new FocusChangeListener());
		mSearchText.setOnEditorActionListener(search.new EditorActionListener());

		mBrowserView.setScrollViewCallbacks(this);

		try {
			mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					Rect r = new Rect();
					mRootView.getWindowVisibleDisplayFrame(r);
					int heightDiff = mRootView.getRootView().getHeight() - (r.bottom - r.top);

					DisplayMetrics dm = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(dm);
					int screenHeight = dm.heightPixels;

					if (heightDiff > screenHeight / 3) {
						mAssistInputBar.setVisibility(View.VISIBLE);
					} else {
						mAssistInputBar.setVisibility(View.GONE);
					}
				}
			});
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	private MenuItem[] getMenuItems() {
		int[] titles = { ResourceUtil.getStringId(this, "ssearch_refresh"),
				ResourceUtil.getStringId(this, "ssearch_setting_about_share"),
				ResourceUtil.getStringId(this, "ssearch_copy_url"), ResourceUtil.getStringId(this, "ssearch_open_in_browser") };
		int[] ids = { MENU_REFRESH, MENU_SHARE, MENU_COPY_URL, MENU_OPEN_WITH_BROWSER };
		MenuItem[] menuItems = new MenuItem[ids.length];
		for (int i = 0; i < titles.length; i++) {
			MenuItem item = new MenuItem(ids[i], titles[i]);
			menuItems[i] = item;
		}
		return menuItems;
	}

	private int getMenuWidth() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		return width / 2;
	}

	private void initPopupMenu() {
		mPopupMenuWindow = new PopupMenuWindow(this, MenuType.Spinner, getMenuItems());
		mPopupMenuWindow.setWidth(getMenuWidth());
		mPopupMenuWindow.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {

				switch (item.getId()) {
				case MENU_REFRESH:
					refreshOrStop();
					break;
				case MENU_SHARE:
					if (mBrowserView != null && !mBrowserView.getUrl().startsWith(BrowserConstants.FILE)) {
						Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
						shareIntent.setType("text/plain");
						shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mBrowserView.getTitle());
						String shareMessage = mBrowserView.getUrl();
						shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
						startActivity(Intent.createChooser(
								shareIntent,
								getResources().getString(
										ResourceUtil.getStringId(SearchBrowserActivity.this, "ssearch_dialog_title_share"))));
					}
					break;
				case MENU_COPY_URL:
					if (mBrowserView != null && !mBrowserView.getUrl().startsWith(BrowserConstants.FILE)) {
						ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
						ClipData clip = ClipData.newPlainText("label", mBrowserView.getUrl());
						clipboard.setPrimaryClip(clip);
						Toast.makeText(SearchBrowserActivity.this,
								ResourceUtil.getStringId(SearchBrowserActivity.this, "ssearch_message_link_copied"),
								Toast.LENGTH_SHORT).show();
					}
					break;
				case MENU_OPEN_WITH_BROWSER:
					if (mBrowserView != null && !TextUtils.isEmpty(mBrowserView.getUrl())) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(mBrowserView.getUrl()));
						startActivity(intent);
					}
					break;
				}
				return false;
			}
		});

	}

	private void showMenu() {
		if (mPopupMenuWindow == null) {
			initPopupMenu();
		}

		mPopupMenuWindow.showAsDropDown(findViewById(ResourceUtil.getId(this, "search_menu_layout")), 0, 0);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mBrowserView != null) {
			mBrowserView.clearData();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mBrowserView != null && mBrowserView.getVisibility() == View.VISIBLE) {
			mBrowserView.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mBrowserView != null && mBrowserView.getVisibility() == View.VISIBLE) {
			mBrowserView.onPause();
		}
	}

	@Override
	public void onBackPressed() {
		if (mSearchText.hasFocus()) {
			mSearchText.clearFocus();
		} else if (mBrowserView.canGoBack()) {
			if (!mBrowserView.isShown()) {
				// onHideCustomView();
			} else {
				mBrowserView.goBack();
			}
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public Activity getActivity() {
		return this;
	}

	@Override
	public void updateUrl(String url, boolean shortUrl) {
		if (url == null || mSearchText == null) {
			return;
		}

		if (shortUrl && !url.startsWith(BrowserConstants.FILE)) {
			// Title, show the page's title
			if (mBrowserView != null && !TextUtils.isEmpty(mBrowserView.getTitle())) {
				mSearchText.setText(mBrowserView.getTitle());
			}
		} else {
			if (url.startsWith(BrowserConstants.FILE)) {
				url = "";
			}
			mSearchText.setText(url);
		}

	}

	private void searchTheWeb(String query) {
		if (query.equals("")) {
			return;
		}
		query = query.trim();
		mBrowserView.stopLoading();

		if (query.startsWith("www.")) {
			query = BrowserConstants.HTTP + query;
		} else if (query.startsWith("ftp.")) {
			query = "ftp://" + query;
		}

		boolean containsPeriod = query.contains(".");
		boolean isIPAddress = (TextUtils.isDigitsOnly(query.replace(".", "")) && (query.replace(".", "").length() >= 4) && query
				.contains("."));
		boolean aboutScheme = query.contains("about:");
		boolean validURL = (query.startsWith("ftp://") || query.startsWith(BrowserConstants.HTTP)
				|| query.startsWith(BrowserConstants.FILE) || query.startsWith(BrowserConstants.HTTPS))
				|| isIPAddress;

		boolean isSearch = ((query.contains(" ") || !containsPeriod) && !aboutScheme && !validURL);

		if (isIPAddress && (!query.startsWith(BrowserConstants.HTTP) || !query.startsWith(BrowserConstants.HTTPS))) {
			query = BrowserConstants.HTTP + query;
		}

		if (isSearch) {
			try {
				query = URLEncoder.encode(query, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			mBrowserView.loadUrl(mSearchSource.getSearchUrl(query));

		} else if (!validURL) {
			mBrowserView.loadUrl(BrowserConstants.HTTP + query);
		} else {
			mBrowserView.loadUrl(query);
		}
	}

	private void setFinishedLoading() {
		mProgressBar.setVisibility(View.GONE);
	}

	private void setLoading() {
		mProgressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public void updateProgress(int progress) {
		if (progress >= 100) {
			setFinishedLoading();
		} else {
			setLoading();
		}

		mProgressBar.setProgress(progress);
	}

	/**
	 * turns on fullscreen mode in the app
	 * 
	 * @param enabled
	 *            whether to enable fullscreen or not
	 */
	public void setFullscreen(boolean enabled) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;
		if (enabled) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
			if (mCustomView != null) {
				mCustomView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			} else {
				mContentContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
		}
		win.setAttributes(winParams);
	}

	@Override
	public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
		if (view == null) {
			return;
		}
		if (mCustomView != null && callback != null) {
			callback.onCustomViewHidden();
			return;
		}
		try {
			view.setKeepScreenOn(true);
		} catch (SecurityException e) {
			LogUtils.e(TAG, "WebView is not allowed to keep the screen on");
		}
		mOriginalOrientation = getRequestedOrientation();
		FrameLayout decor = (FrameLayout) getWindow().getDecorView();
		mFullscreenContainer = new FullscreenHolder(this);
		mCustomView = view;

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mFullscreenContainer.addView(mCustomView, params);
		decor.addView(mFullscreenContainer, params);
		setFullscreen(true);
		mBrowserView.setVisibility(View.GONE);
		if (view instanceof FrameLayout) {
			if (((FrameLayout) view).getFocusedChild() instanceof VideoView) {
				mVideoView = (VideoView) ((FrameLayout) view).getFocusedChild();
				mVideoView.setOnErrorListener(new VideoCompletionListener());
				mVideoView.setOnCompletionListener(new VideoCompletionListener());
			}
		}
		mCustomViewCallback = callback;

	}

	@Override
	public void onHideCustomView() {
		if (mCustomView == null || mCustomViewCallback == null || mBrowserView == null) {
			return;
		}
		LogUtils.d(TAG, "onHideCustomView");
		mBrowserView.setVisibility(View.VISIBLE);
		try {
			mCustomView.setKeepScreenOn(false);
		} catch (SecurityException e) {
			LogUtils.e(TAG, "WebView is not allowed to keep the screen on");
		}
		setFullscreen(false);
		FrameLayout decor = (FrameLayout) getWindow().getDecorView();
		if (decor != null) {
			decor.removeView(mFullscreenContainer);
		}

		if (android.os.Build.VERSION.SDK_INT < 19) {
			try {
				mCustomViewCallback.onCustomViewHidden();
			} catch (Throwable ignored) {

			}
		}
		mFullscreenContainer = null;
		mCustomView = null;
		if (mVideoView != null) {
			mVideoView.setOnErrorListener(null);
			mVideoView.setOnCompletionListener(null);
			mVideoView = null;
		}
		setRequestedOrientation(mOriginalOrientation);
	}

	@Override
	/**
	 * a stupid method that returns the bitmap image to display in place of
	 * a loading video
	 */
	public Bitmap getDefaultVideoPoster() {
		if (mDefaultVideoPoster == null) {
			mDefaultVideoPoster = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_media_play);
		}
		return mDefaultVideoPoster;
	}

	@SuppressLint("InflateParams")
	@Override
	/**
	 * dumb method that returns the loading progress for a video
	 */
	public View getVideoLoadingProgressView() {
		if (mVideoProgressView == null) {
			LayoutInflater inflater = LayoutInflater.from(this);
			mVideoProgressView = inflater.inflate(ResourceUtil.getLayoutId(this, "ssearch_video_loading_progress"), null);
		}
		return mVideoProgressView;
	}

	/**
	 * handle presses on the refresh icon in the search bar, if the page is
	 * loading, stop the page, if it is done loading refresh the page.
	 * 
	 * See setIsFinishedLoading and setIsLoading for displaying the correct icon
	 */
	public void refreshOrStop() {
		if (mBrowserView != null) {
			if (mBrowserView.getProgress() < 100) {
				mBrowserView.stopLoading();
			} else {
				mBrowserView.reload();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_ENTER) {
			if (mSearchText.hasFocus()) {
				searchTheWeb(mSearchText.getText().toString());
			}
		} else if ((keyCode == KeyEvent.KEYCODE_MENU) && (Build.VERSION.SDK_INT <= 16)
				&& (Build.MANUFACTURER.compareTo("LGE") == 0)) {
			// Workaround for stupid LG devices that crash
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == ResourceUtil.getId(this, "search_menu_layout")) {
			showMenu();
		} else if (id == ResourceUtil.getId(this, "search_clear_layout")) {
			mSearchText.setText("");
		}
	}

	@Override
	public void onLongPress() {
	}

	public void onRefresh() {
		refreshOrStop();
	}

	private void onSearchbarStateChanged(boolean hasFocus) {
		if (hasFocus) {
			mMenuLayout.setVisibility(View.GONE);
			mClearLayout.setVisibility(View.VISIBLE);
		} else {
			mMenuLayout.setVisibility(View.VISIBLE);
			mClearLayout.setVisibility(View.GONE);
		}
	}

	private class SearchTextListener {

		public class KeyListener implements OnKeyListener {

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {

				switch (arg1) {
				case KeyEvent.KEYCODE_ENTER:
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
					searchTheWeb(mSearchText.getText().toString());
					if (mBrowserView != null) {
						mBrowserView.requestFocus();
					}
					return true;
				}
				return false;
			}

		}

		public class EditorActionListener implements OnEditorActionListener {
			@Override
			public boolean onEditorAction(TextView arg0, int actionId, KeyEvent arg2) {
				// hide the keyboard and search the web when the enter key
				// button is pressed
				if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE
						|| actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_SEND
						|| actionId == EditorInfo.IME_ACTION_SEARCH || (arg2.getAction() == KeyEvent.KEYCODE_ENTER)) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
					searchTheWeb(mSearchText.getText().toString());
					if (mBrowserView != null) {
						mBrowserView.requestFocus();
					}
					return true;
				}
				return false;
			}
		}

		public class FocusChangeListener implements OnFocusChangeListener {
			@Override
			public void onFocusChange(View v, final boolean hasFocus) {
				onSearchbarStateChanged(hasFocus);
				if (!hasFocus && mBrowserView != null) {
					if (mBrowserView.getProgress() < 100) {
						setLoading();
					} else {
						setFinishedLoading();
					}
					updateUrl(mBrowserView.getUrl(), true);
				} else if (hasFocus) {
					String url = mBrowserView.getUrl();
					if (url == null || url.startsWith(BrowserConstants.FILE)) {
						mSearchText.setText("");
					} else {
						mSearchText.setText(url);
					}
					((EditText) v).selectAll();
				}

				if (!hasFocus) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
				}
			}
		}

	}

	private class VideoCompletionListener implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			return false;
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			onHideCustomView();
		}

	}

	private class FullscreenHolder extends FrameLayout {

		public FullscreenHolder(Context ctx) {
			super(ctx);
			setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
		}

		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouchEvent(MotionEvent evt) {
			return true;
		}

	}

	@Override
	public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
	}

	@Override
	public void onDownMotionEvent() {
	}

	@Override
	public void onUpOrCancelMotionEvent(ScrollState scrollState) {
		LogUtils.d(TAG, "onUpOrCancelMotionEvent: " + scrollState + " toolbarIsShown:" + toolbarIsShown() + " toolbarIsHidden:"
				+ toolbarIsHidden() + " getTranslationY:" + mActionbarLayout.getTranslationY() + " getHeight:"
				+ mActionbarLayout.getHeight());
		if (!isFullScreen) {
			if (scrollState == ScrollState.UP) {
				if (toolbarIsShown()) {
					hideToolbar();
				}
			} else if (scrollState == ScrollState.DOWN) {
				if (toolbarIsHidden()) {
					showToolbar();
				}
			}
		}
	}

	private boolean toolbarIsShown() {
		return mActionbarLayout.getTranslationY() == 0;
	}

	private boolean toolbarIsHidden() {
		return mActionbarLayout.getTranslationY() == -mActionbarLayout.getHeight();
	}

	private void showToolbar() {
		moveToolbar(0);
	}

	private void hideToolbar() {
		moveToolbar(-mActionbarLayout.getHeight());
	}

	protected int getScreenHeight() {
		return findViewById(android.R.id.content).getHeight();
	}

	private void moveToolbar(float toTranslationY) {
		LogUtils.d(TAG, "moveToolbar getTranslationY: " + mActionbarLayout.getTranslationY() + " toTranslationY:"
				+ toTranslationY);
		if (mActionbarLayout.getTranslationY() == toTranslationY) {
			return;
		}
		ValueAnimator animator = ValueAnimator.ofFloat(mActionbarLayout.getTranslationY(), toTranslationY).setDuration(200);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float translationY = (float) animation.getAnimatedValue();
				mActionbarLayout.setTranslationY(translationY);
				mContentContainer.setTranslationY(translationY);
				FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mContentContainer.getLayoutParams();
				lp.height = (int) -translationY + getScreenHeight() - lp.topMargin;
				mContentContainer.requestLayout();
			}
		});
		animator.start();
	}

}
