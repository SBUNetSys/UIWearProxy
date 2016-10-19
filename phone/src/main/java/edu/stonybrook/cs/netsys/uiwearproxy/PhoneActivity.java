package edu.stonybrook.cs.netsys.uiwearproxy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager.TransparentActivity;
import edu.stonybrook.cs.netsys.uiwearproxy.uiwearService.PhoneProxyService;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.REQUEST_ACCESSIBILITY_SERVICE_CODE;

public class PhoneActivity extends Activity {

    private final Intent accessibilitySettingIntent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
    private Intent phoneServiceIntent;
    private AccessibilityNodeInfo rootNodeOfCurrentScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phoneServiceIntent = new Intent(this, PhoneProxyService.class);
    }

    public void startProxyService(View startButton) {
        startActivityForResult(accessibilitySettingIntent, REQUEST_ACCESSIBILITY_SERVICE_CODE);
    }

    public void stopProxyService(View stopButton) {
//        phoneServiceIntent.putExtra(STOP_PHONE_PROXY_SERVICE_KEY, STOP_PHONE_PROXY_SERVICE_CODE);
        //stopProxyService(phoneServiceIntent); //bounded to AccessibilityService, so not working
//        startService(phoneServiceIntent);
        startActivity(accessibilitySettingIntent);
        Toast.makeText(this, R.string.turn_off_proxy_service, Toast.LENGTH_SHORT).show();
    }

    public void setPreference(View setButton) {
        startActivity(new Intent(getApplication(), TransparentActivity.class));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ACCESSIBILITY_SERVICE_CODE) {
            if (isAccessibilityEnabled()) {

                Toast.makeText(this, R.string.service_enabled, Toast.LENGTH_SHORT).show();
                startService(phoneServiceIntent);

            } else {
                Toast.makeText(this, R.string.service_not_enabled, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isAccessibilityEnabled() {
        int accessibilityEnabled = 0;
        final String ACCESSIBILITY_SERVICE_NAME = getPackageName() + "/" + PhoneProxyService.class.getName();
        //"edu.stonybrook.cs.netsys.uiwearproxy/edu.stonybrook.cs.netsys.uiwearproxy.uiwearService.PhoneProxyService";
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Logger.i("Accessibility code: " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Logger.e("Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Logger.i("Setting: " + settingValue);
            Logger.i("ACCESSIBILITY_SERVICE_NAME: " + ACCESSIBILITY_SERVICE_NAME);
            if (ACCESSIBILITY_SERVICE_NAME.equalsIgnoreCase(settingValue)) {
                Logger.i("We've found the correct setting - accessibility is switched on!");
                return true;
            }

        } else {
            Logger.e("Accessibility is disabled");
        }

        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Logger.i("");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        Logger.i("");
    }

    @Override
    protected void onResume() {
//        Logger.i("");
        super.onResume();
    }

    @Override
    protected void onPause() {
//        Logger.i("");
        super.onPause();
    }

    @Override
    protected void onStop() {
//        Logger.i("");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Logger.i("onDestroy");
        super.onDestroy();
    }

//    @Override
//    protected void onUserLeaveHint() {
//        Logger.i("onUserLeaveHint");
//        super.onUserLeaveHint();
//    }

    @Override
    public void onAttachedToWindow() {
        Logger.i("");
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        Logger.i("");
        super.onDetachedFromWindow();
    }
}
