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

import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import edu.stonybrook.cs.netsys.uiwearproxy.R;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.AVAILABLE_NODES_FOR_PREFERENCE_SETTING_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_NODES_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_EXIT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.NODES_AVAILABLE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_SAVE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_STARTED;

public class PreferenceSettingActivity extends Activity {
    private ArrayList<Rect> availableNodes; // all available nodes from phone proxy service
    private Toast notReadyToast;
    private SelectPreferenceView preferenceView;
    private static final float CLICK_SPAN_THRESHOLD = 5.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);

        preferenceView = (SelectPreferenceView) findViewById(R.id.drawing);
        notReadyToast = Toast.makeText(getApplicationContext(),
                R.string.not_ready_for_selection, Toast.LENGTH_SHORT);

        preferenceView.setOnTouchListener(new View.OnTouchListener() {
            float startX;
            float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (availableNodes == null) {
                    notReadyToast.show();
                } else {
//                    Logger.i("onTouch");
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            startY = event.getY();
                            break;
                        case MotionEvent.ACTION_UP:
                            float endX = event.getX();
                            float endY = event.getY();
                            // detect a click
//                            Logger.i("startX: " + startX + " startY: " + startY
//                                    + " endX: " + endX + " endY: " + endY);
                            if (isClick(endX, endY)) {
//                                Logger.i("clicked");
                                //if the click area has an available node
                                selectPreferenceOnClickPoint((int) endX, (int) endY);
                            } else {// swipe to reset all preferences
                                resetPreference();
                            }
                            break;
                        default:
                            return false;
                    }
                }
                return true;
            }

            private boolean isClick(float endX, float endY) {
                return Math.abs(startX - endX) < CLICK_SPAN_THRESHOLD
                        && Math.abs(startY - endY) < CLICK_SPAN_THRESHOLD;
            }
        });
    }

    private void selectPreferenceOnClickPoint(int endX, int endY) {
        for (Rect rect : availableNodes) {
            if (rect.contains(endX, endY)) {
                if (preferenceView.hasSelected(rect)) {
                    Logger.i("unselected rect: " + rect);
                    // discard the selected area frame
                    preferenceView.removeNode(rect);
                } else {
                    Logger.i("selected rect: " + rect);
                    // mark the selected area by drawing a slim frame
                    preferenceView.addNode(rect);
                }
            }
        }
    }

    private void resetPreference() {
        preferenceView.removeAllNodes();
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

        if (availableNodes == null) {
            notReadyToast.show();
        } else {

            // retrieve all selected nodes
            ArrayList<Rect> selectedNodes = preferenceView.getPreferredNodes();
            if (selectedNodes.isEmpty()) {
                Toast.makeText(this, R.string.null_back, Toast.LENGTH_SHORT).show();
            } else {
                Intent rectIntent = new Intent(PREFERENCE_SETTING_SAVE);
                rectIntent.putExtra(PREFERENCE_NODES_KEY, selectedNodes);
                LocalBroadcastManager.getInstance(this).sendBroadcast(rectIntent);
                Toast.makeText(this, R.string.saved_back, Toast.LENGTH_SHORT).show();
            }
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
        IntentFilter filter = new IntentFilter(NODES_AVAILABLE);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(availableNodesReceiver, filter);
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(PREFERENCE_SETTING_STARTED));
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(availableNodesReceiver);
        super.onPause();
    }

    private BroadcastReceiver availableNodesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NODES_AVAILABLE:
                    availableNodes = intent.
                            getParcelableArrayListExtra(AVAILABLE_NODES_FOR_PREFERENCE_SETTING_KEY);
                    break;
                default:
            }
        }
    };
}
