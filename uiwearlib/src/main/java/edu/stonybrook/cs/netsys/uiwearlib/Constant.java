package edu.stonybrook.cs.netsys.uiwearlib;

import android.content.Intent;

/**
 * Created by qqcao on 10/18/16 Tuesday.
 * Constants for UIWear
 */

public class Constant {
    public static final int ACCESSIBILITY_SERVICE_REQUEST_CODE = 1;
    public static final String PREFERENCE_SETTING_KEY = "PREFERENCE_SETTING_KEY";
    public static final int PREFERENCE_SETTING_CODE = 2;
    public static final String PREFERENCE_SETTING_SAVE = "PREFERENCE_SETTING_SAVE";
    public static final String PREFERENCE_NODES_KEY = "PREFERENCE_NODES_KEY";
    public static final String PREFERENCE_SETTING_EXIT = "PREFERENCE_SETTING_EXIT";
    public static final String SYSTEM_UI_PKG = "com.android.systemui";
    public static final String PREFERENCE_SETTING_STARTED = "PREFERENCE_SETTING_STARTED";
    public static final String NODES_AVAILABLE = "NODES_AVAILABLE";
    public static final String AVAILABLE_NODES_PREFERENCE_SETTING_KEY
            = "AVAILABLE_NODES_PREFERENCE_SETTING_KEY";
    public static final String PREFERENCE_STOP_KEY = "PREFERENCE_STOP_KEY";
    public static final int PREFERENCE_STOP_CODE = 3;
    public static final Intent ACCESSIBILITY_SETTING_INTENT
            = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
    public static final String ENABLED_APP_LIST_PREF_NAME = "UIWearServingAppList";

    public static final float CLICK_SPAN_THRESHOLD = 5.0f;
}
