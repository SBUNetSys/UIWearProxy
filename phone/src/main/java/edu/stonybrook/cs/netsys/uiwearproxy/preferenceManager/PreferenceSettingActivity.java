package edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.AVAILABLE_NODES_PREFERENCE_SETTING_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.NODES_AVAILABLE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_NODES_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_EXIT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_SAVE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_STARTED;

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
import java.util.Collections;
import java.util.Comparator;

import edu.stonybrook.cs.netsys.uiwearlib.Constant;
import edu.stonybrook.cs.netsys.uiwearproxy.R;

public class PreferenceSettingActivity extends Activity {
    private ArrayList<Rect> mAvailableNodes; // all available nodes from phone proxy service
    private Toast mHintToast;
    private SelectPreferenceView mSelectPreferenceView;
    private boolean mDoubleBackToExitPressedOnce = false;

    // TODO: 11/4/16 render nodes on mSelectPreferenceView if preference file exist
    private BroadcastReceiver mAvailableNodesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NODES_AVAILABLE:
                    mAvailableNodes = intent.
                            getParcelableArrayListExtra(AVAILABLE_NODES_PREFERENCE_SETTING_KEY);
                    Collections.sort(mAvailableNodes, new Comparator<Rect>() {
                        @Override
                        public int compare(Rect o1, Rect o2) {
                            return o1.width() * o1.height() - o2.width() * o2.height();
                        }
                    });

                    break;
                default:
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_setting);

        mSelectPreferenceView = (SelectPreferenceView) findViewById(R.id.view_drawing);
        mHintToast = Toast.makeText(getApplicationContext(),
                R.string.not_ready_for_selection, Toast.LENGTH_SHORT);

        mSelectPreferenceView.setOnTouchListener(new View.OnTouchListener() {
            float mStartX;
            float mStartY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mAvailableNodes == null) {
                    mHintToast.show();
                } else {
//                    Logger.i("onTouch");
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mStartX = event.getX();
                            mStartY = event.getY();
                            break;
                        case MotionEvent.ACTION_UP:
                            float endX = event.getX();
                            float endY = event.getY();
                            // detect a click
//                            Logger.i("mStartX: " + mStartX + " mStartY: " + mStartY
//                                    + " endX: " + endX + " endY: " + endY);
                            if (isClick(endX, endY)) {
//                                Logger.i("clicked");
                                //if the click area has an available node
                                selectPreferenceOnClickPoint((int) endX, (int) endY);
                            } else {
                                // swipe to reset all preferences
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
                return Math.abs(mStartX - endX) < Constant.CLICK_SPAN_THRESHOLD
                        && Math.abs(mStartY - endY) < Constant.CLICK_SPAN_THRESHOLD;
            }
        });
    }

    private void selectPreferenceOnClickPoint(int endX, int endY) {
        for (Rect rect : mAvailableNodes) {
            if (rect.contains(endX, endY)) {
                if (mSelectPreferenceView.hasSelected(rect)) {
                    Logger.i("unselected rect: " + rect);
                    // discard the selected area frame
                    mSelectPreferenceView.removeNode(rect);
                } else {
                    Logger.i("selected rect: " + rect);
                    // mark the selected area by drawing a slim frame
                    mSelectPreferenceView.addNode(rect);
                }
                break;
            }
        }
    }

    private void resetPreference() {
        mSelectPreferenceView.removeAllNodes();
    }

    @Override
    public void onBackPressed() {
        if (mDoubleBackToExitPressedOnce) {
            mHintToast.setText(R.string.exit_preference_setting);
            mHintToast.show();
            LocalBroadcastManager.getInstance(this).sendBroadcast(
                    new Intent(PREFERENCE_SETTING_EXIT));
            super.onBackPressed();
            return;
        }

        this.mDoubleBackToExitPressedOnce = true;

        if (mAvailableNodes == null) {
            mHintToast.setText(R.string.not_ready_for_selection);
            mHintToast.show();
        } else {

            // retrieve all selected nodes
            ArrayList<Rect> selectedNodes = mSelectPreferenceView.getPreferredNodes();
            if (selectedNodes.isEmpty()) {
                mHintToast.setText(R.string.null_back);
                mHintToast.show();
            } else {
                Intent rectIntent = new Intent(PREFERENCE_SETTING_SAVE);
                rectIntent.putExtra(PREFERENCE_NODES_KEY, selectedNodes);
                LocalBroadcastManager.getInstance(this).sendBroadcast(rectIntent);
                mHintToast.setText(R.string.saved_back);
                mHintToast.show();
            }
        }

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mDoubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter(NODES_AVAILABLE);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mAvailableNodesReceiver, filter);
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(PREFERENCE_SETTING_STARTED));
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mAvailableNodesReceiver);
        super.onPause();
    }
}
