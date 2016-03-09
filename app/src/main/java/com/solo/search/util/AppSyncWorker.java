package com.solo.search.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;

import com.solo.search.db.AppDBHelper;

public class AppSyncWorker extends Service implements Runnable {

	public static final String APPS_SETTINGS = "AppsSettings";

	private static final int INDEX_COLUMN_ID = 0;
	private static final int INDEX_COLUMN_PACKAGE = 1;
	private static final int INDEX_COLUMN_CLASS = 2;

	public static final String[] APPS_PROJECTION = new String[] { AppDBHelper.COLUMN_ID, AppDBHelper.COLUMN_PACKAGE,
			AppDBHelper.COLUMN_CLASS };

	private Thread mSyncThread = null;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		synchronized (this) {
			if (mSyncThread == null) {
				mSyncThread = new Thread(this);
				mSyncThread.setPriority(Thread.MIN_PRIORITY + 1);
				mSyncThread.start();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		synchronized (this) {
			if (mSyncThread != null) {
				mSyncThread.interrupt();
				while (mSyncThread != null) {
					try {
						wait(100);
					} catch (InterruptedException e) {
					}
				}
			}
		}
		super.onDestroy();
	}

	@Override
	public void run() {
		SharedPreferences settings = getSharedPreferences(APPS_SETTINGS, 0);
		int syncState = settings.getInt("syncState", AppDBHelper.STATE_OUT_OF_SYNC);
		do {
			if (syncState != AppDBHelper.STATE_SYNC) {
				if (synchronize()) {
					SharedPreferences.Editor editor = settings.edit();
					syncState = AppDBHelper.STATE_SYNC;
					editor.putInt("syncState", syncState);
					editor.commit();
				}
			}

			synchronized (this) {
				syncState = settings.getInt("syncState", AppDBHelper.STATE_OUT_OF_SYNC);
				if (syncState == AppDBHelper.STATE_SYNC) {
					mSyncThread = null;
					stopSelf();
					return;
				}
			}
		} while ((syncState != AppDBHelper.STATE_SYNC) && !mSyncThread.isInterrupted());

		synchronized (this) {
			mSyncThread = null;
			stopSelf();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public static final void start(Context context) {
		SharedPreferences settings = context.getSharedPreferences(AppSyncWorker.APPS_SETTINGS, 0);
		int syncState = settings.getInt("syncState", AppDBHelper.STATE_OUT_OF_SYNC);
		if (syncState == AppDBHelper.STATE_OUT_OF_SYNC) {
			context.startService(new Intent(context, AppSyncWorker.class));
		}
	}

	private boolean synchronize() {
		PackageManager pm = getPackageManager();
		AppDBHelper dbHelper = new AppDBHelper(this);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query(AppDBHelper.APPS_TABLE, APPS_PROJECTION, null, null, null, null, null);

		if (cursor != null) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast() && !mSyncThread.isInterrupted()) {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setClassName(cursor.getString(INDEX_COLUMN_PACKAGE), cursor.getString(INDEX_COLUMN_CLASS));
				List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

				if (list.size() == 0) {
					db.delete(AppDBHelper.APPS_TABLE, AppDBHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(cursor
							.getInt(INDEX_COLUMN_ID)) });
				}
				cursor.moveToNext();
			}
			cursor.close();
		}

		if (mSyncThread.isInterrupted()) {
			return false;
		}

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> launchActivities = pm.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
		ArrayList<ContentValues> apps = new ArrayList<ContentValues>();

		for (ResolveInfo ri : launchActivities) {
			if (mSyncThread.isInterrupted()) {
				return false;
			}
			if (ri != null && ri.activityInfo != null) {
				CharSequence appLabel = ri.activityInfo.loadLabel(pm);
				if (appLabel != null) {
					cursor = db.query(AppDBHelper.APPS_TABLE, APPS_PROJECTION, AppDBHelper.COLUMN_TITLE + " != ? AND "
							+ AppDBHelper.COLUMN_PACKAGE + " = ? AND " + AppDBHelper.COLUMN_CLASS + " = ? AND "
							+ AppDBHelper.COLUMN_INTENT + " = ? AND " + AppDBHelper.COLUMN_CATEGORY + " = ?", new String[] {
							appLabel.toString(), ri.activityInfo.packageName, ri.activityInfo.name, Intent.ACTION_MAIN,
							Intent.CATEGORY_LAUNCHER }, null, null, null);

					if (cursor != null) {
						cursor.moveToFirst();
						if (cursor.getCount() == 1) {
							ContentValues values = new ContentValues();
							values.put(AppDBHelper.COLUMN_TITLE, appLabel.toString());
							db.update(AppDBHelper.APPS_TABLE, values, AppDBHelper.COLUMN_ID + " = ?", new String[] { String
									.valueOf(cursor.getInt(INDEX_COLUMN_ID)) });
						} else {
							while (!cursor.isAfterLast()) {
								db.delete(AppDBHelper.APPS_TABLE, AppDBHelper.COLUMN_ID + " = ?", new String[] { String
										.valueOf(cursor.getInt(INDEX_COLUMN_ID)) });
								cursor.moveToNext();
							}
						}
						cursor.close();
					}

					cursor = db.query(AppDBHelper.APPS_TABLE, APPS_PROJECTION, AppDBHelper.COLUMN_TITLE + " = ? AND "
							+ AppDBHelper.COLUMN_PACKAGE + " = ? AND " + AppDBHelper.COLUMN_CLASS + " = ? AND "
							+ AppDBHelper.COLUMN_INTENT + " = ? AND " + AppDBHelper.COLUMN_CATEGORY + " = ?", new String[] {
							appLabel.toString(), ri.activityInfo.packageName, ri.activityInfo.name, Intent.ACTION_MAIN,
							Intent.CATEGORY_LAUNCHER }, null, null, null);
					if (cursor != null) {
						if (cursor.getCount() == 0) {
							ContentValues values = new ContentValues();
							values.put(AppDBHelper.COLUMN_TITLE, appLabel.toString());
							values.put(AppDBHelper.COLUMN_PACKAGE, ri.activityInfo.packageName);
							values.put(AppDBHelper.COLUMN_CLASS, ri.activityInfo.name);
							values.put(AppDBHelper.COLUMN_INTENT, Intent.ACTION_MAIN);
							values.put(AppDBHelper.COLUMN_CATEGORY, Intent.CATEGORY_LAUNCHER);
							apps.add(values);
							if (apps.size() >= 10) {
								ContentValues[] contentValues = new ContentValues[apps.size()];
								for (ContentValues temp : apps.toArray(contentValues)) {
									db.insert(AppDBHelper.APPS_TABLE, null, temp);
								}
								apps.clear();
							}
						}
						cursor.close();
					}
				}
			}
		}

		if (mSyncThread.isInterrupted()) {
			return false;
		}
		if (apps.size() > 0) {
			ContentValues[] contentValues = new ContentValues[apps.size()];
			for (ContentValues temp : apps.toArray(contentValues)) {
				db.insert(AppDBHelper.APPS_TABLE, null, temp);
			}

			apps.clear();
		}

		db.close();

		return true;
	}
}