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
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import edu.stonybrook.cs.netsys.uiwearproxy.R;
import edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager.PreferenceSettingActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_RECT_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_CODE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_EXIT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_INTENT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_KEY;

public class PhoneProxyService extends AccessibilityService {

    private AccessibilityNodeInfo rootNodeOfCurrentScreen;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PREFERENCE_SETTING_INTENT:
                    Rect preferredRect = intent.getParcelableExtra(PREFERENCE_RECT_KEY);
                    processPreferenceRect(preferredRect);
                    Logger.i("received: "+preferredRect.toString());
                    break;
                case PREFERENCE_SETTING_EXIT:
                    rootNodeOfCurrentScreen = null;
                    Logger.i("setting exit ");
                    break;
                default:
            }
        }
    };

    // save all UI nodes that fall into the preferredRect
    private void processPreferenceRect(Rect preferredRect) {
        if (rootNodeOfCurrentScreen != null) {
            Logger.i(rootNodeOfCurrentScreen.toString());
        }
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, R.string.service_enabled, Toast.LENGTH_SHORT).show();

        IntentFilter filter = new IntentFilter(PREFERENCE_SETTING_INTENT);
        filter.addAction(PREFERENCE_SETTING_EXIT);

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(broadcastReceiver,filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int code = intent.getIntExtra(PREFERENCE_SETTING_KEY, 0);
            if (code == PREFERENCE_SETTING_CODE) {
                Logger.i("setting preference");
                rootNodeOfCurrentScreen = getRootInActiveWindow();
//                Logger.i(rootNodeOfCurrentScreen.toString());
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
        Logger.e(event.getSource().toString());
        Logger.e(event.toString());
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
