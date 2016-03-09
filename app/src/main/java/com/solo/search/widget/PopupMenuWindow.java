package com.solo.search.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.solo.search.model.MenuItem;
import com.solo.search.util.ResourceUtil;

public class PopupMenuWindow {

	private Context mContext;
	private PopupWindow mPopuWindow;
	private LinearLayout mBaseView;
	private MenuItem[] mMenuItems;
	private MenuType mMenuType;
	private MenuAdapter mMenuAdapter;

	private OnMenuItemClickListener mMenuItemClickListener;

	public enum MenuType {
		Normal, Spinner
	};

	public PopupMenuWindow(Context context, MenuItem[] menuItems) {
		mContext = context;
		mMenuType = MenuType.Normal;
		mMenuItems = menuItems;
		initialize();
	}

	public PopupMenuWindow(Context context, MenuType menuType, MenuItem[] menuItems) {
		mContext = context;
		mMenuType = menuType;
		mMenuItems = menuItems;

		initialize();
	}

	private void initialize() {
		mPopuWindow = new PopupWindow(mContext);

		LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mBaseView = (LinearLayout) layoutInflater.inflate(ResourceUtil.getLayoutId(mContext, "ssearch_menu_main"), null);

		ListView listview = (ListView) mBaseView.findViewById(ResourceUtil.getId(mContext, "listview"));

		mPopuWindow.setWidth(LayoutParams.WRAP_CONTENT);
		mPopuWindow.setHeight(LayoutParams.WRAP_CONTENT);

		mMenuAdapter = new MenuAdapter();
		listview.setAdapter(mMenuAdapter);

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mPopuWindow.dismiss();
				mMenuItemClickListener.onMenuItemClick(mMenuItems[position]);
			}
		});

		mPopuWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopuWindow.setContentView(mBaseView);
		mPopuWindow.setFocusable(true);
		mPopuWindow.setOutsideTouchable(true);
		mPopuWindow.setTouchable(true);

		// 解决点击物理menu键打开popupwindow后，再点击无反应的bug
		mBaseView.setFocusableInTouchMode(true);
		mBaseView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((keyCode == KeyEvent.KEYCODE_MENU) && (mPopuWindow.isShowing())) {
					mPopuWindow.dismiss();
					return true;
				}
				return false;
			}
		});

	}

	public void setWidth(int width) {
		mPopuWindow.setWidth(width);
	}

	public void setType(MenuType type) {
		mMenuType = type;
	}

	public void setMenuItems(MenuItem[] menuItems) {
		mMenuItems = menuItems;
	}

	public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
		mMenuItemClickListener = listener;
	}

	public PopupWindow getPopupWindow() {
		return mPopuWindow;
	}

	public boolean isShowing() {
		return mPopuWindow.isShowing();
	}

	public void showAtLocation(View parent, int gravity, int x, int y) {
		mPopuWindow.showAtLocation(parent, gravity, x, y);
	}

	public void showAsDropDown(View anchor, int xoff, int yoff) {
		mPopuWindow.showAsDropDown(anchor, xoff, yoff);
	}

	public void dismiss() {
		mPopuWindow.dismiss();
	}

	class MenuAdapter extends BaseAdapter {

		public MenuAdapter() {
		}

		@Override
		public int getCount() {
			return mMenuItems.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(mContext).inflate(ResourceUtil.getLayoutId(mContext, "ssearch_menu_item"), null);
			TextView text = (TextView) convertView.findViewById(ResourceUtil.getId(mContext, "title"));

			text.setText(mMenuItems[position].getTitle());

			if (mMenuType == MenuType.Spinner) {
				final LayoutParams lp = text.getLayoutParams();
				lp.width = LayoutParams.MATCH_PARENT;
				text.setLayoutParams(lp);
			}

			return convertView;
		}
	}

	public interface OnMenuItemClickListener {

		public boolean onMenuItemClick(MenuItem item);

	}

}
