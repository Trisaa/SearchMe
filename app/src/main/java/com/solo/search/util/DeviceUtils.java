package com.solo.search.util;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * 设备工具类。
 * 
 * @author bianque
 * 
 */
public class DeviceUtils {

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 判断程序是否安装
	 */
	public static boolean isApkInstalled(Context context, String packageName) {
		if (TextUtils.isEmpty(packageName))
			return false;
		try {
			@SuppressWarnings("unused")
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	public static boolean isNetConnected(Context context) {
		try {
			ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cManager.getActiveNetworkInfo();
			if (info != null && info.isAvailable()) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public static boolean isWifiConnectTimeOut(Context context) {
		WifiManager wifi_service = (WifiManager) context.getSystemService(Service.WIFI_SERVICE);
		WifiInfo wifiInfo = wifi_service.getConnectionInfo();
		if (wifiInfo != null) {
			int rssi = wifiInfo.getRssi();
			if (rssi < -70) {
				// 这个时候网络状况很不好，可认为无网络
				return false;
			}
		}
		return true;
	}

	public static boolean isWifiConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
	}

	/**
	 * 判断GPS是否开启
	 * 
	 * @param context
	 * @return true 表示开启
	 */
	public static boolean isGPSConnected(Context context) {
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (network) {
			return true;
		}
		return false;
	}

	public static String trimSpace(String text) {
		return text.replaceAll("\\s", "").trim();
	}

	/**
	 * 获取apk 版本号
	 * 
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context) {
		return getVersionName(context, context.getPackageName());
	}

	public static String getVersionName(Context context, String packageName) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			return trimSpace(info.versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取apk versioncode
	 * 
	 * @param context
	 * @return
	 */
	public static int getVersionCode(Context context, String packageName) {
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			return info.versionCode;
		} catch (NameNotFoundException e) {
			return 100;
		}
	}

	/**
	 * 获取uuid
	 */
	public static String getDeviceUUID(Context mContext) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			String uuid = Settings.Secure.getString(mContext.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			byte[] bytes = uuid.getBytes("UTF-8");
			digest.update(bytes, 0, bytes.length);
			bytes = digest.digest();

			for (final byte b : bytes) {
				stringBuilder.append(String.format("%02X", b));
			}

			return trimSpace(stringBuilder.toString().toLowerCase());
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isSamsungPhone(Context context) {
		return isApkInstalled(context, "com.sec.android.provider.badge");
	}

	public static boolean isSDCardMounted() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}

	/**
	 * 判断当前是系统时间设置是24小时还是12小时制
	 * 
	 * @param context
	 * @return
	 */
	public static boolean is24HourFormat(Context context) {
		boolean is24Format = true;
		ContentResolver cv = context.getContentResolver();
		String strTimeFormat = "24";
		try {
			strTimeFormat = android.provider.Settings.System.getString(cv, android.provider.Settings.System.TIME_12_24);
		} catch (Exception e) {
			strTimeFormat = "24";
		}
		if (strTimeFormat == null) {
			strTimeFormat = "24";
		}
		if (!strTimeFormat.equals("24")) {
			is24Format = false;
		}
		return is24Format;
	}

	/**
	 * 获取SDK版本号
	 * 
	 * @return
	 */
	public static int getSDKVersion() {
		return android.os.Build.VERSION.SDK_INT;
	}

	/**
	 * 获取当前手机使用的语言
	 * 
	 * @param context
	 * @return 语言的代码 如中文是CN
	 */
	public static String getLocaleLanguage(Context context) {
		String language = context.getResources().getConfiguration().locale.getLanguage();
		if (language != null) {
			language.toLowerCase(Locale.US);
		}
		return trimSpace(language);
	}

	/**
	 * 获取当前国家ISO码<br>
	 * 策略:<br>
	 * 1.返回SIM卡提供商的国家代码。<br>
	 * 2.返回网络所在的国家代码（ISO标准形式)<br>
	 * http://countrycode.org/
	 * 
	 * @param context
	 * @return
	 */
	public static String getCountryISOCode(Context context) {
		String isoStr = "";
		try {
			final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			final String simCountry = tm.getSimCountryIso();
			if (simCountry != null && simCountry.length() == 2) {
				isoStr = simCountry.toLowerCase(Locale.US);
			} else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) {
				String networkCountry = tm.getNetworkCountryIso();
				if (networkCountry != null && networkCountry.length() == 2) {
					isoStr = networkCountry.toLowerCase(Locale.US);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (TextUtils.isEmpty(isoStr)) {
			try {
				isoStr = context.getResources().getConfiguration().locale.getCountry();
			} catch (Exception e) {
				isoStr = "us";
			}
		}

		return trimSpace(isoStr.toLowerCase());
	}

	public static boolean isAppProcessRunning(Context context, String packageName) {
		ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> processList = am.getRunningAppProcesses();

		for (RunningAppProcessInfo process : processList) {
			if (process.processName.equals(packageName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断当前手机是否处于省电模式，5.0(SDK 21)以上系统才有此功能，5.0以下系统默认返回False。
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isPowerSaveMode(Context context) {
		if (Build.VERSION.SDK_INT >= 21) {
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			return pm.isPowerSaveMode();
		} else {
			return false;
		}
	}

	/**
	 * 判断手机上的语音搜索是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isVoiceSearchAvailable(Context context) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(new Intent(intent), 0);
		return activities != null && !activities.isEmpty();
	}

	public static void collapseStatusBar(Context context) {
		try {
			Object statusBarManager = context.getSystemService("statusbar");
			Method collapse;
			if (Build.VERSION.SDK_INT <= 16) {
				collapse = statusBarManager.getClass().getMethod("collapse");
			} else {
				collapse = statusBarManager.getClass().getMethod("collapsePanels");
			}
			collapse.invoke(statusBarManager);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param activity
	 * @return > 0 success; <= 0 fail
	 */
	public static int getStatusHeight(Activity activity) {
		int statusHeight = 0;
		Rect localRect = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
		statusHeight = localRect.top;
		if (0 == statusHeight) {
			Class<?> localClass;
			try {
				localClass = Class.forName("com.android.internal.R$dimen");
				Object localObject = localClass.newInstance();
				int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
				statusHeight = activity.getResources().getDimensionPixelSize(i5);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		return statusHeight;
	}

	/**
	 * 返回当前屏幕是否为竖屏。
	 * 
	 * @param context
	 * @return 当且仅当当前屏幕为竖屏时返回true,否则返回false。
	 */
	public static boolean isScreenOriatationPortrait(Context context) {
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}

	public static void launchDataUsageSettings(Context context) {
		DeviceUtils.collapseStatusBar(context);
		try {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setComponent(new ComponentName("com.android.settings",
					"com.android.settings.Settings$DataUsageSummaryActivity"));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(intent);
		} catch (Exception ex) {
		}
	}

	/**
	 * 监测手机上是否安装有Sim卡。
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isCanUseSim(Context context) {
		try {
			TelephonyManager mgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			return TelephonyManager.SIM_STATE_READY == mgr.getSimState();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isPad(Context context) {
		PackageManager packageManager = context.getPackageManager();
		boolean hasFeatureTelephony = packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
		return !hasFeatureTelephony;
	}
}
