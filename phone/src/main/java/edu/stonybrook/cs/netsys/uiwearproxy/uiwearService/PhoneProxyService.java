package edu.stonybrook.cs.netsys.uiwearproxy.uiwearService;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ACCESSIBILITY_SETTING_INTENT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant
        .AVAILABLE_NODES_FOR_PREFERENCE_SETTING_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.NODES_AVAILABLE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_NODES_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_CODE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_EXIT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_SAVE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_STARTED;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_STOP_CODE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_STOP_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.SYSTEM_UI_PKG;

import android.accessibilityservice.AccessibilityService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.stonybrook.cs.netsys.uiwearlib.NodeUtils;
import edu.stonybrook.cs.netsys.uiwearproxy.R;

public class PhoneProxyService extends AccessibilityService {

    private NotificationManager mNotificationManager;

    // for preference setting, only need to parse once
    private boolean mIsRunningPreferenceSetting;

    // either region or id alone is not enough for detecting the specific UI elements
    // so combined them and can cover most cases
    // must use region as key, since id can be the same
    private HashMap<Rect, String> mAppLeafNodesMap = new HashMap<>();
    private String mAppRootNodePkgName;

    // for bitmap extracting and other heavy work
    private WorkerThread mWorkerThread;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PREFERENCE_SETTING_STARTED:
                    Logger.i("preference activity started");
                    sendLeafNodesToPreferenceSetting();
                    break;

                case PREFERENCE_SETTING_SAVE:
                    ArrayList<Rect> preferredRect =
                            intent.getParcelableArrayListExtra(PREFERENCE_NODES_KEY);
                    savePreferenceNodes(preferredRect);
                    Logger.i("received preferredRect");
                    break;

                case PREFERENCE_SETTING_EXIT:

                    Logger.i("setting exit ");
                    break;
                default:
            }
        }
    };

    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter(PREFERENCE_SETTING_SAVE);
        filter.addAction(PREFERENCE_SETTING_STARTED);
        filter.addAction(PREFERENCE_SETTING_EXIT);

        LocalBroadcastManager.getInstance(getApplicationContext())
                .registerReceiver(mBroadcastReceiver, filter);

        mWorkerThread = new WorkerThread("worker-thread");
        mWorkerThread.start();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        raiseRunningNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int startCode = intent.getIntExtra(PREFERENCE_SETTING_KEY, 0);
            if (startCode == PREFERENCE_SETTING_CODE) {
                mIsRunningPreferenceSetting = true;
                Logger.i("start preference setting");
            }

            int stopCode = intent.getIntExtra(PREFERENCE_STOP_KEY, 0);
            if (stopCode == PREFERENCE_STOP_CODE) {
                mIsRunningPreferenceSetting = false;
                mAppLeafNodesMap.clear();
                Logger.i("stop preference setting");
            }

        }
        return START_STICKY;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        // preference setting functionality
        if (mIsRunningPreferenceSetting) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
//            Logger.v("root node: " + rootNode.toString());
//            NodeUtils.printNodeTree(rootNode);
            if (isAppRootNode(rootNode)) {
                Logger.i("app node: " + NodeUtils.getBriefNodeInfo(rootNode));
                mAppRootNodePkgName = rootNode.getPackageName().toString();
                mAppLeafNodesMap.clear();
                parseLeafNodes(rootNode);
            }
        }

        // extracting view tree workflow
        // TODO: 10/21/16 extract view tree and send to wearable side based on app preference
        // TODO: 10/21/16 migrate previous on demand bitmap extraction
        // TODO: 10/21/16 possible caching and optimization here, including migrating previous
        // LRU cache and use worker thread (not AsyncTask, use handler thread)


    }

    private boolean isAppRootNode(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            return false;
        }
        CharSequence nodePkgName = rootNode.getPackageName();
        return !SYSTEM_UI_PKG.equals(nodePkgName) && !getPackageName().equals(nodePkgName);
    }

    // parse all UI leaf nodes and save them to list mAppLeafNodesMap
    private void parseLeafNodes(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            Logger.i("null root node ");
            return;
        }

        int count = rootNode.getChildCount();
        if (count == 0) { // no child, leaf node
            if (rootNode.isVisibleToUser() && !rootNode.getClassName()
                    .toString().endsWith("Layout")) { // filter the ViewGroup
                Rect region = new Rect();
                rootNode.getBoundsInScreen(region);
                String id = rootNode.getViewIdResourceName();
                if (!region.isEmpty() && id != null) {
                    Logger.i("add: " + NodeUtils.getBriefNodeInfo(rootNode));
                    mAppLeafNodesMap.put(region, id);
                }
            }
            rootNode.recycle();
        } else {
            for (int i = 0; i < count; i++) {
                parseLeafNodes(rootNode.getChild(i));
            }
        }
    }

    // send prepared leaf nodes to PreferenceSettingActivity
    private void sendLeafNodesToPreferenceSetting() {
        if (mAppLeafNodesMap.size() > 0) {
            Intent nodesIntent = new Intent(NODES_AVAILABLE);
            ArrayList<Rect> nodes = new ArrayList<>(mAppLeafNodesMap.keySet());
            nodesIntent.putParcelableArrayListExtra(AVAILABLE_NODES_FOR_PREFERENCE_SETTING_KEY,
                    nodes);
            LocalBroadcastManager.getInstance(this).sendBroadcast(nodesIntent);
        } else {
            Logger.i("no available nodes to send!");
        }
    }

    // save rect-ID pair of user's selected UI nodes and persist them to app specific xml file
    // TODO: 10/21/16 save file format to activity granularity, although currently it support all
    // activities
    private void savePreferenceNodes(ArrayList<Rect> preferredNodes) {
        // ensure that mAppLeafNodesMap is not cleared
        HashMap<Rect, String> savedMap = new HashMap<>(mAppLeafNodesMap);
        Logger.i("mapped nodes: " + mAppLeafNodesMap.keySet().toString());
        SharedPreferences sharedPref = getSharedPreferences(mAppRootNodePkgName, MODE_APPEND);
        SharedPreferences.Editor editor = sharedPref.edit();
        for (Rect rect : preferredNodes) {
            String id = savedMap.get(rect); // can't be null since already checked during put time
            String rectString = rect.flattenToString();
            Logger.i("save preference: " + id + " " + rect);
            // FIXME: 10/21/16 possibly to be improved
            try {
                String existingRect = sharedPref.getString(id, "");
                Logger.i("existingRect: " + existingRect);
                if (existingRect.isEmpty()) {
                    editor.putString(id, rectString);
                } else {
                    if (!existingRect.equals(rectString)) {
                        HashSet<String> set = new HashSet<>();
                        set.add(existingRect);
                        set.add(rectString);
                        Logger.i("set add two: " + Arrays.toString(set.toArray()));
                        editor.putStringSet(id, set);
                    }
                }
            } catch (ClassCastException e) { // catch return Set exception
                Logger.e(e.getMessage());
                Set<String> set = sharedPref.getStringSet(id, new HashSet<String>());
                if (set.size() > 0) {
                    Logger.i("existing set: " + id + " " + Arrays.toString(set.toArray()));
                    set.add(rectString);
                    Logger.i("set add one: " + id + " " + Arrays.toString(set.toArray()));
                    editor.putStringSet(id, set);
                }
            }
            editor.commit();// need to commit here, otherwise file not correct!

        }
        editor.apply();

    }

    @Override
    public void onInterrupt() {
        Logger.i("");

    }

    @Override
    public void onDestroy() {
        Logger.i("");
        stopRunningNotification();
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public AccessibilityNodeInfo getRootInActiveWindow() {
        return super.getRootInActiveWindow();
    }

    private void raiseRunningNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.running_phone_proxy))
                .setContentText("")
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        ACCESSIBILITY_SETTING_INTENT, PendingIntent.FLAG_UPDATE_CURRENT));
        mNotificationManager.notify(2, mBuilder.build());
    }

    private void stopRunningNotification() {
        mNotificationManager.cancelAll();
    }
}
