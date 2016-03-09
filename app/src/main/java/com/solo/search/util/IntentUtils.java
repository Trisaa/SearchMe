package com.solo.search.util;

public class IntentUtils {

	public static final String SET_DEFAULT_FROM_INSTRUCTION = "set_default_from_instruction";
	public static final String SET_SOLO_WALLPAPER = "set_solo_wallpaper";
	public static final String ACTION_HIDN_GUIDE = "home.solo.launcher.action.HIDE_GUIDE";
	public static final String ACTION_SOLO_ACTION = "home.solo.launcher.free.action.SOLO_ACTION";// 自定义solo
	public static final String ACTION_LAUNCHER_THEME = "home.solo.launcher.free.action.LAUNCHER_THEME";// 品牌定制主题
	public static final String ACTION_SOLO_THEME = "home.solo.launcher.free.THEMES";
	public static final String ACTION_SOLO_FONT = "home.solo.launcher.free.FONTS";
	public static final String ACTION_DIY_LOCKER = "android.intent.action.diy";

	public static final String ACTION_DRAWER_HIDE_APP = "hide_drawer_app";
	public static final String ACTION_DRAWER_ADD_TAB = "add_drawer_tab";
	public static final String ACTION_DRAWER_UPDATE_TAB = "update_drawer_tab";
	public static final String ACTION_DRAWER_UPDATE_TABLIST = "update_drawer_tablist";
	public static final String ACTION_DRAWER_DELETE_TAB = "delete_drawer_tab";
	public static final String ACTION_DRAWER_ADD_FOLDER = "add_drawer_folder";
	public static final String ACTION_DRAWER_DELETE_FOLDER = "delete_drawer_folder";
	public static final String ACTION_DRAWER_UPDATE_FOLDER = "update_drawer_folder";
	public static final String ACTION_HIDE_APPFLOOD_ICON = "hide_appflood_icon";
	public static final String ACTION_DIY_DRAWER = "action_diy_drawer";

	public static final String ACTION_LOCK_SCREEN_SERVICE = "home.solo.launcher.free.plugin.aidl.TurnOffScreenService";

	public static final String ACTION_HIDE_GUIDE_LAYOUT = "home.solo.launcher.free.action.HIDE_GUIDE_LAYOUT";
	// Unread count
	public static final String ACTION_UNREAD_APP_CHANGED = "home.solo.launcher.free.action.COUNTER_APP_CHANGED";// 更新未读消息
	public static final String ACTION_COUNTER_CHANGED = "home.solo.launcher.free.action.COUNTER_CHANGED";// 更新未读消息内容
	public static final String ACTION_UPDATE_COUNTER = "home.solo.launcher.free.action.UPDATE_COUNTER";
	public static final String ACTION_CANCEL_UNREAD_COUNT = "home.solo.launcher.free.action.CANCEL_UNREAD_COUNT";
	public static final String EXTRA_UNREAD_APP = "counter_app";
	public static final String EXTRA_NOTIFY_COUNT = "count";
	public static final String EXTRA_NOTIFY_PACKAGE = "package";
	public static final String EXTRA_NOTIFY_CLASS = "class";

	// Tools Notification
	public static final String ACTION_TOOLS_NOTIFICATION = "home.solo.launcher.free.action.TOOLS_NOTIFICATION";
	public static final String EXTRA_TOOLS_INDEX = "home.solo.launcher.free.extra.tools_index";

	// Search Bar
	public static final String ACTION_SEARCH_BAR = "home.solo.launcher.free.action.SEARCH_BAR";

	// Themes
	public static final String ACTION_LAUNCHER_START = "home.solo.launcher.free.action.LAUNCHER_START";// 桌面启动
	public static final String ACTION_APPLY_THEME = "home.solo.launcher.free.APPLY_THEME";// 应用主题
	public static final String ACTION_APPLY_FONT = "home.solo.launcher.free.APPLY_FONT";// 应用字体

	// Browser
	public static final String ACTION_BROWSER = "home.solo.launcher.free.ACTION.BROWSER";
	public static final String EXTRA_SEARCH_BROWSER_URL = "search_browser_url";
	public static final String EXTRA_BROWSER_FULL_SCREEN = "browser_full_screen";

	public static final String ACTION_APPLY_ICON_THEME = "home.solo.launcher.free.APPLY_ICON_THEME";// 应用主题图标的接口（开放给第三方主题开发者）

	public static final String ACTION_SELECT_PAGE = "home.solo.launcher.free.action.SELECT_PAGE";

	public static final String ACTION_SOLO_PICK_ICON = "home.solo.launcher.free.ACTION_ICON";
	public static final String ACTION_GO_THEME = "com.gau.go.launcherex.theme";
	public static final String ACTION_APEX_THEME = "android.intent.action.MAIN";
	public static final String CATEGORY_APEX_THEME = "com.anddoes.launcher.THEME";
	public static final String ACTION_ADW_THEME = "org.adw.launcher.icons.ACTION_PICK_ICON";
	public static final String ACTION_LAUNCHERACTION = "home.solo.launcher.free.action.launcheraction";

	public static final String CATEGORY_APEX_PICK_ICON = "com.anddoes.launcher.THEME";

	public static final String EXTRA_FONT_FILE = "home.solo.launcher.free.extra.FONT_FILE";// 兼容第三方字体包
	public static final String EXTRA_LAUNCHER_THEME_URL = "LAUNCHER_THEME_URL";// 品牌定制主题url
	public static final String EXTRA_NAME = "home.solo.launcher.free.extra.NAME";
	public static final String EXTRA_OLD_NAME = "EXTRA_THEMENAME";// 老版本规则的名称extra（V1.7.6前）
	public static final String EXTRA_OLD_PACKAGE = "EXTRA_PACKAGENAME";// 老版本规则的包名extra（V1.7.6前）
	public static final String EXTRA_PACKAGE = "home.solo.launcher.free.extra.PACKAGE";
	public static final String ACTION_INSTALL_SOLO_SHORTCUT = "home.solo.launcher.free.INSTALL_SHORTCUT";
	public static final String EXTRA_SHORTCUT_COORDINATATE = "home.solo.launcher.free.extra.shortcut.COORDINATATE";// shortcuts
																													// position{screen：2,x：2,y：3}
	public static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";
	public static final String EXTRA_UNREAD_APP_KEY = "unread_app_key";
	public static final String EXTRA_UNREAD_APP_TITLE = "unread_app_title";

	public static final String EXTRA_APP_ID = "id";
	public static final String EXTRA_APP_NAME = "name";
	public static final String EXTRA_SUMMARY = "summary";

	public static final String EXTRA_SEARCH_HINT_TEXT = "search_hint_text";
	public static final String EXTRA_SEARCH_TEXT = "search_text";

}
