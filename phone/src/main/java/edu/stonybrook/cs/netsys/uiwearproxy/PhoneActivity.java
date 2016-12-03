package edu.stonybrook.cs.netsys.uiwearproxy;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ACCESSIBILITY_SERVICE_REQUEST_CODE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ACCESSIBILITY_SETTING_INTENT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.CACHE_DISABLED;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.CACHE_ENABLED;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PERMISSIONS_REQUEST_CODE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_CODE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_STOP_CODE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_STOP_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PROXY_STARTED;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PROXY_STATUS_PREF;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PURGE_CACHE_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.RESET_DIFF_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CACHE_STATUS_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CACHE_STATUS_PREF;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager.AppSettingActivity;
import edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager.PreferenceSettingActivity;
import edu.stonybrook.cs.netsys.uiwearproxy.uiwearService.PhoneProxyService;

public class PhoneActivity extends Activity {

    private boolean mIsProxyStarted;
    private Switch mCacheSwitch;
    private TextView mResetDiffTextView;
    private TextView mPrefSettingTextView;
    private TextView mSetAppTextView;
    private TextView mCacheHintTextView;
    private TextView mPurgeCacheTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        checkPermissions();

        mResetDiffTextView = (TextView) findViewById(R.id.tv_reset_diff);
        mCacheHintTextView = (TextView) findViewById(R.id.tv_cache_status);
        mPrefSettingTextView = (TextView) findViewById(R.id.tv_pref_set);
        mSetAppTextView = (TextView) findViewById(R.id.tv_set_app);
        mPurgeCacheTextView = (TextView) findViewById(R.id.tv_purge_cache);
        mCacheSwitch = (Switch) findViewById(R.id.switch_cache);

        SharedPreferences settings = getSharedPreferences(PROXY_STATUS_PREF, MODE_PRIVATE);
        mIsProxyStarted = settings.getBoolean(PROXY_STARTED, false);
        Logger.i("onCreate: " + mIsProxyStarted);
        setControlState(mIsProxyStarted);

        final SharedPreferences cachePref = getSharedPreferences(CACHE_STATUS_PREF,
                Context.MODE_PRIVATE);
        mCacheSwitch.setChecked(cachePref.getBoolean(CACHE_STATUS_KEY, true));
        mCacheSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent proxyIntent = new Intent(getApplicationContext(), PhoneProxyService.class);
                SharedPreferences.Editor editor = cachePref.edit();
                if (isChecked) {
                    mPurgeCacheTextView.setEnabled(true);
                    proxyIntent.putExtra(CACHE_STATUS_KEY, CACHE_ENABLED);
                    editor.putBoolean(CACHE_STATUS_KEY, true);
                    editor.apply();
                } else {
                    mPurgeCacheTextView.setEnabled(false);
                    proxyIntent.putExtra(CACHE_STATUS_KEY, CACHE_DISABLED);
                    editor.putBoolean(CACHE_STATUS_KEY, false);
                }
                startService(proxyIntent);
                editor.apply();

            }
        });
    }

    private void setControlState(boolean enabled) {
        mResetDiffTextView.setEnabled(enabled);
        mPrefSettingTextView.setEnabled(enabled);
        mSetAppTextView.setEnabled(enabled);
        mCacheHintTextView.setEnabled(enabled);
        mPurgeCacheTextView.setEnabled(enabled);
        mCacheSwitch.setEnabled(enabled);
    }

    public void startProxyService(View startButton) {
        startActivityForResult(ACCESSIBILITY_SETTING_INTENT, ACCESSIBILITY_SERVICE_REQUEST_CODE);
    }

//    public void stopProxyService(View stopButton) {
//        finishAffinity();
//        Intent proxyIntent = new Intent(this, PhoneProxyService.class);
//        stopService(proxyIntent);
//        startActivity(ACCESSIBILITY_SETTING_INTENT);
////        android.os.Process.killProcess(android.os.Process.myPid());
//    }

    public void startPreferenceSetting(View setButton) {
        Intent proxyIntent = new Intent(this, PhoneProxyService.class);
        proxyIntent.putExtra(PREFERENCE_SETTING_KEY, PREFERENCE_SETTING_CODE);
        startService(proxyIntent);
        raisePreferenceSettingNotification();
        finish();
    }

    public void startSelectingApp(View view) {
        startActivity(new Intent(this, AppSettingActivity.class));
    }

    public void purgeCache(View view) {
        Intent proxyIntent = new Intent(this, PhoneProxyService.class);
        proxyIntent.putExtra(PURGE_CACHE_KEY, true);
        startService(proxyIntent);
    }

    public void resetDiff(View view) {
        Intent proxyIntent = new Intent(this, PhoneProxyService.class);
        proxyIntent.putExtra(RESET_DIFF_KEY, true);
        startService(proxyIntent);
    }

    public void setCacheStatus(View view) {
        mCacheSwitch.toggle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ACCESSIBILITY_SERVICE_REQUEST_CODE) {
            if (isAccessibilityEnabled()) {
                mIsProxyStarted = true;
                Toast.makeText(this, R.string.service_enabled, Toast.LENGTH_SHORT).show();
                Intent proxyIntent = new Intent(this, PhoneProxyService.class);
                startService(proxyIntent);
            } else {
                mIsProxyStarted = false;
                Toast.makeText(this, R.string.service_not_enabled, Toast.LENGTH_SHORT).show();
            }
            setControlState(mIsProxyStarted);
        }
    }

    private boolean isAccessibilityEnabled() {
        int accessibilityEnabled = 0;
        final String accessibilityServiceName =
                getPackageName() + "/" + PhoneProxyService.class.getName();
        //"edu.stonybrook.cs.netsys.uiwearproxy/edu.stonybrook.cs.netsys.uiwearproxy
        // .uiwearService.PhoneProxyService";
        try {
            accessibilityEnabled = Settings.Secure.getInt(this.getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Logger.v("Accessibility code: " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Logger.e(
                    "Error finding setting, default accessibility to not found: " + e.getMessage());
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Logger.v("Setting: " + settingValue);
            Logger.v("accessibilityServiceName: " + accessibilityServiceName);
            if (accessibilityServiceName.equalsIgnoreCase(settingValue)) {
                Logger.v("We've found the correct setting - accessibility is switched on!");
                return true;
            }

        } else {
            Logger.e("Accessibility is disabled");
        }

        return false;
    }

    private void raisePreferenceSettingNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.running_phone_proxy))
                        .setContentText(getString(R.string.setting_preference));

        Intent preferenceSettingIntent = new Intent(getApplicationContext(),
                PreferenceSettingActivity.class);
        PendingIntent preferenceSettingPendingIntent = PendingIntent.getActivity(this, 0,
                preferenceSettingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent proxyIntent = new Intent(this, PhoneProxyService.class);
        proxyIntent.putExtra(PREFERENCE_STOP_KEY, PREFERENCE_STOP_CODE);
        PendingIntent stopPreferenceSetting = PendingIntent.getService(this, 1,
                proxyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(preferenceSettingPendingIntent);
        mBuilder.setDeleteIntent(stopPreferenceSetting);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void checkPermissions() {
        boolean writeExternalStoragePermissionGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;

        if (!writeExternalStoragePermissionGranted) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Logger.v("write storage permission granted");
            } else {
                Toast.makeText(this, "Please grant write permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.d(": " + mIsProxyStarted);
    }

    @Override
    protected void onResume() {
        Logger.d(": " + mIsProxyStarted);
        super.onResume();
    }

    @Override
    protected void onPause() {
        SharedPreferences settings = getSharedPreferences(PROXY_STATUS_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PROXY_STARTED, mIsProxyStarted);
        editor.commit();
        Logger.d(": " + mIsProxyStarted);
        super.onPause();
    }

    @Override
    protected void onStop() {
        Logger.d(": " + mIsProxyStarted);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Logger.d("onDestroy: " + mIsProxyStarted);
        super.onDestroy();
    }

//    @Override
//    protected void onUserLeaveHint() {
//        Logger.v("onUserLeaveHint");
//        super.onUserLeaveHint();
//    }

//    @Override
//    public void onAttachedToWindow() {
//        Logger.v("");
//        super.onAttachedToWindow();
//    }
//
//    @Override
//    public void onDetachedFromWindow() {
//        Logger.v("");
//        super.onDetachedFromWindow();
//    }
}
