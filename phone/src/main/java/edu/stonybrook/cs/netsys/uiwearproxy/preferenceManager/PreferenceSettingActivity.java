package edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;

import edu.stonybrook.cs.netsys.uiwearproxy.R;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.LEAF_NODES_FOR_PREFERENCE_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_NODES_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_EXIT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_PREPARED;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_SAVE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_STARTED;

public class PreferenceSettingActivity extends Activity {
    private ArrayList<Rect> preferredNodes; // initialized to all preferred from phone proxy service
    private HashMap<Rect, Boolean> nodeSelected = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);

        final CaptureView drawView = (CaptureView) findViewById(R.id.drawing);

        drawView.setOnTouchListener(new View.OnTouchListener() {
            float startX;
            float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (preferredNodes == null) {
                    Toast.makeText(getApplicationContext(),
                            R.string.not_ready_for_selection, Toast.LENGTH_SHORT).show();
                } else {
                    Logger.i("onTouch");
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            startY = event.getY();
                            break;
                        case MotionEvent.ACTION_UP:
                            float endX = event.getX();
                            float endY = event.getY();
                            // detect a click
                            Logger.i("startX: " + startX + " startY: " + startY
                                    + " endX: " + endX + " endY: " + endY);
                            if (Math.abs(startX - endX) < 5.0 && Math.abs(startY - endY) < 5.0) {
                                Logger.i("clicked");
                                //if the click area has an available node
                                // TODO: 10/21/16 possibly can be optimized to improve performance
                                // FIXME: 10/21/16 user display not work, preference fully working
                                for (Rect rect : nodeSelected.keySet()) {
                                    if (rect.contains((int) endX, (int) endY)) {

                                        drawView.getDrawPath().addRect(
                                                (float) rect.left, (float) rect.top,
                                                (float) rect.right, (float) rect.bottom,
                                                Path.Direction.CCW);
                                        if (nodeSelected.get(rect)) {
                                            nodeSelected.put(rect, false);
                                            Logger.i("unselected rect: " + rect);
                                            // fade the unselected area, discard the frame
                                            drawView.getDrawPath().reset();
                                        } else {
                                            nodeSelected.put(rect, true);
                                            Logger.i("selected rect: " + rect);
                                            // mark the selected area by drawing a slim frame
                                            drawView.getDrawPaint().setColor(Color.RED);

                                        }
                                        v.invalidate();
                                    }
                                }
                            } else {
                                // reset all preferences
                                for (Rect rect : nodeSelected.keySet()) {
                                    nodeSelected.put(rect, false);
                                }
                                drawView.getDrawPath().reset();
                                v.invalidate();

                            }
                            break;
                        default:
                            return false;
                    }
                }
                return true;
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

        if (preferredNodes == null) {
            Toast.makeText(getApplicationContext(),
                    R.string.not_ready_for_selection, Toast.LENGTH_SHORT).show();
        } else {

            // check all selected nodes and put to preferredNodes
            preferredNodes.clear();
            for (Rect rect : nodeSelected.keySet()) {
                if (nodeSelected.get(rect)) {
                    preferredNodes.add(rect);
                    Logger.i("save rect: " + rect);
                }
            }

            if (preferredNodes.isEmpty()) {
                Toast.makeText(this, R.string.null_back, Toast.LENGTH_SHORT).show();
            } else {

                Intent rectIntent = new Intent(PREFERENCE_SETTING_SAVE);
                rectIntent.putExtra(PREFERENCE_NODES_KEY, preferredNodes);
                LocalBroadcastManager.getInstance(this).sendBroadcast(rectIntent);
                Toast.makeText(this, R.string.saved_back, Toast.LENGTH_SHORT).show();
                // reset nodeSelected
                for (Rect rect : nodeSelected.keySet()) {
                    nodeSelected.put(rect, false);
                }
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
                    // initialize all nodes to false, i.e., none is selected by user
                    for (Rect rect : preferredNodes) {
                        nodeSelected.put(rect, false);
                    }
                    break;
                default:
            }
        }
    };
}
