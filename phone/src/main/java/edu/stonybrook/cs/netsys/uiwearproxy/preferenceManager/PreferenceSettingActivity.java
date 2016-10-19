package edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import edu.stonybrook.cs.netsys.uiwearproxy.R;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_RECT_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_EXIT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_INTENT;

public class PreferenceSettingActivity extends Activity {
    private CaptureView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);

        drawView = (CaptureView) findViewById(R.id.drawing);
        drawView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

    }

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Toast.makeText(this, R.string.exit_preference_setting, Toast.LENGTH_SHORT).show();
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PREFERENCE_SETTING_EXIT));
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Rect preferRect = drawView.getPreferRect();
        if (preferRect == null) {
            Toast.makeText(this, R.string.null_back, Toast.LENGTH_SHORT).show();
        } else {
            Logger.d(preferRect);
            Intent rectIntent = new Intent(PREFERENCE_SETTING_INTENT);
            rectIntent.putExtra(PREFERENCE_RECT_KEY, preferRect);
            LocalBroadcastManager.getInstance(this).sendBroadcast(rectIntent);
            Toast.makeText(this, R.string.saved_back, Toast.LENGTH_SHORT).show();
        }

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onUserLeaveHint() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.exit_preference_setting)
                .setMessage(R.string.exit_preference_confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }

                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
