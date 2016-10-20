package edu.stonybrook.cs.netsys.uiwearproxy.uiwearService;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.support.v4.content.LocalBroadcastManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager.PreferenceSettingActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_NODE_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_CODE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_EXIT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_SAVE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_STARTED;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.SYSTEM_UI_PKG;

public class PhoneProxyService extends AccessibilityService {

    private AccessibilityNodeInfo rootNodeOfCurrentScreen;
    private boolean isSettingPreference;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PREFERENCE_SETTING_STARTED:
                    preparedNodesForPreferenceSetting();
                    break;
                case PREFERENCE_SETTING_SAVE:
                    ArrayList<Rect> preferredRect = intent.getParcelableExtra(PREFERENCE_NODE_KEY);
                    savePreferenceNodes(preferredRect);
                    Logger.i("received: " + preferredRect.toString());
                    break;
                case PREFERENCE_SETTING_EXIT:

                    isSettingPreference = false;
                    Logger.i("setting exit ");
                    break;
                default:
            }
        }
    };

    // parse all UI leaf nodes and send to preference setting activity
    private void preparedNodesForPreferenceSetting() {

    }

    // save user's selected UI nodes and persist it to app specific xml file
    private void savePreferenceNodes(ArrayList<Rect> preferredNodes) {

    }

    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter(PREFERENCE_SETTING_SAVE);
        filter.addAction(PREFERENCE_SETTING_STARTED);
        filter.addAction(PREFERENCE_SETTING_EXIT);

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int code = intent.getIntExtra(PREFERENCE_SETTING_KEY, 0);
            if (code == PREFERENCE_SETTING_CODE) {
                isSettingPreference = true;
                Logger.i("setting preference");
                Intent preferenceSettingIntent = new Intent(getApplicationContext(),
                        PreferenceSettingActivity.class);
                preferenceSettingIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(preferenceSettingIntent);

            } else {
                Logger.i("started");
            }
        }
        return START_STICKY;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (isSettingPreference) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (appRootNode(rootNode)) {

            }
        }

    }

    private boolean appRootNode(AccessibilityNodeInfo rootNode) {
        CharSequence nodePkgName = rootNode.getPackageName();
        return !SYSTEM_UI_PKG.equals(nodePkgName) && !getPackageName().equals(nodePkgName);
    }

    @Override
    public void onInterrupt() {
        Logger.i("");

    }

    @Override
    public void onDestroy() {
        Logger.i("");
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    @Override
    public AccessibilityNodeInfo getRootInActiveWindow() {
        return super.getRootInActiveWindow();
    }
}
