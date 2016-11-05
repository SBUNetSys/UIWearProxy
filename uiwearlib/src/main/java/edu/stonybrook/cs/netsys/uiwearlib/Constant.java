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
    public static final String ENABLED_APPS_PREF_NAME = "UIWearEnabledApps";

    public static final float CLICK_SPAN_THRESHOLD = 5.0f;

    public static final String WEAR_PLUGIN_ACTION_MAIN = "plugin_action_main";
    public static final String WEAR_PLUGIN_KEY = "WEAR_PLUGIN_KEY";

    public static final String WEAR_HOST_ACTION_MAIN = "host_action_main";
    public static final String WEAR_HOST_KEY = "WEAR_HOST_KEY";

    public static final String MSG_CAPABILITY = "msg_capability";
    public static final int JSON_INDENT_SPACES = 4;

    public static final int PERSIST_PREFERENCE_NODES_SUCCESS = 5;
    public static final int READ_PREFERENCE_NODES_SUCCESS = 6;
    public static final int RUNNING_APP_CACHE_NO = 10;

    public static final String JSON_EXT = ".json";
    public static final String ID_STRING = "id";
    public static final String RECT_STRING = "rect";

}
