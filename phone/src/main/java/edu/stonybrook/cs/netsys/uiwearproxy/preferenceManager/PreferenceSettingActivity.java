package edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import edu.stonybrook.cs.netsys.uiwearproxy.R;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.LEAF_NODES_FOR_PREFERENCE_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_NODES_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_EXIT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_PREPARED;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_SAVE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_STARTED;

public class PreferenceSettingActivity extends Activity {
    private CaptureView drawView;
    private ArrayList<Rect> preferredNodes = new ArrayList<>();
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

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PREFERENCE_SETTING_STARTED));
    }

    //TODO: use click to select available nodes, which are prepared from phone proxy service
    // to do this, need to render the nodes on current CaptureView and set onCheckLister on that

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

        //TODO: change this to get user's selected nodes
        Rect preferRect = drawView.getPreferRect();
        if (preferRect == null) {
            Toast.makeText(this, R.string.null_back, Toast.LENGTH_SHORT).show();
        } else {

            Intent rectIntent = new Intent(PREFERENCE_SETTING_SAVE);
            rectIntent.putExtra(PREFERENCE_NODES_KEY, preferredNodes);
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
        IntentFilter filter = new IntentFilter(PREFERENCE_SETTING_PREPARED);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(preparedReceiver, filter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(preparedReceiver);
        super.onPause();
    }

    private BroadcastReceiver preparedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PREFERENCE_SETTING_PREPARED:
                    preferredNodes = intent.
                            getParcelableArrayListExtra(LEAF_NODES_FOR_PREFERENCE_KEY);
                    break;
                default:
            }
        }
    };
}
