package edu.stonybrook.cs.netsys.uiwearproxy.uiwearService;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.orhanobut.logger.Logger;

public class PhoneProxyService extends AccessibilityService {

    @Override
    public void onCreate() {
        Logger.i("");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.i("");
//        if (intent != null) {
//            int code = intent.getIntExtra(STOP_PHONE_PROXY_SERVICE_KEY, 0);
//            if (code == STOP_PHONE_PROXY_SERVICE_CODE) {
//                disableSelf(); // only exists in API 24
//            } else {
//                Logger.i("started");
//            }
//        }
        return START_STICKY;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Logger.i(event.toString());
    }

    @Override
    public void onInterrupt() {
        Logger.i("");

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Logger.i("");

        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.i("unbind");

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Logger.i("");

        super.onDestroy();
    }

    @Override
    public AccessibilityNodeInfo getRootInActiveWindow() {
        return super.getRootInActiveWindow();
    }
}
