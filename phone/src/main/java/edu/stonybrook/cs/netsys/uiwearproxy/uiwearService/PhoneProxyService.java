package edu.stonybrook.cs.netsys.uiwearproxy.uiwearService;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ACCESSIBILITY_SETTING_INTENT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.AVAILABLE_NODES_PREFERENCE_SETTING_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ENABLED_APPS_PREF_NAME;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ID_STRING;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.JSON_EXT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.JSON_INDENT_SPACES;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.NODES_AVAILABLE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PERSIST_PREFERENCE_NODES_SUCCESS;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_NODES_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_CODE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_EXIT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_SAVE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_SETTING_STARTED;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_STOP_CODE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PREFERENCE_STOP_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.READ_PREFERENCE_NODES_SUCCESS;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.RECT_STRING;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.RUNNING_APP_CACHE_NO;
import static edu.stonybrook.cs.netsys.uiwearlib.NodeUtils.getNodePkgName;

import android.accessibilityservice.AccessibilityService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.util.LruCache;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import edu.stonybrook.cs.netsys.uiwearlib.FileUtils;
import edu.stonybrook.cs.netsys.uiwearlib.NodeUtils;
import edu.stonybrook.cs.netsys.uiwearlib.WorkerThread;
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

    // main thread handler
    private Handler mMainThreadHandler;

    private SharedPreferences mEnabledAppListPreferences;

    // list of app preference nodes, for each pair, the first is id, the second is rect
    private LruCache<String, ArrayList<Pair<String, String>>> mAppPreferenceNodesCache =
            new LruCache<>(RUNNING_APP_CACHE_NO);

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PREFERENCE_SETTING_STARTED:
                    Logger.v("preference activity started");
                    sendLeafNodesToPreferenceSetting();
                    break;

                case PREFERENCE_SETTING_SAVE:
                    ArrayList<Rect> preferredRect =
                            intent.getParcelableArrayListExtra(PREFERENCE_NODES_KEY);
                    persistAppPreferenceNodesAsync(preferredRect);
                    // TODO: 11/5/16  read mapping rule and start build app process
                    Logger.v("received preferredRect");
                    break;

                case PREFERENCE_SETTING_EXIT:

                    Logger.v("setting exit ");
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

        mMainThreadHandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PERSIST_PREFERENCE_NODES_SUCCESS:
                        Logger.d(msg.obj + " json preference saved");
                        break;
                    case READ_PREFERENCE_NODES_SUCCESS:
                        Logger.d("json preference read");
//                        Pair<String,ArrayList<Pair<String, String>>> appPkgNodeMap =
//                        ArrayList<Pair<String, String>> preferredNodes =
//                                (ArrayList<Pair<String, String>>) msg.obj;
//                        mAppPreferenceNodesCache.get()

                    default:
                        Logger.e("unknown msg");
                }
            }
        };

        mEnabledAppListPreferences = getSharedPreferences(ENABLED_APPS_PREF_NAME,
                Context.MODE_PRIVATE);

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
                Logger.v("start preference setting");
            }

            int stopCode = intent.getIntExtra(PREFERENCE_STOP_KEY, 0);
            if (stopCode == PREFERENCE_STOP_CODE) {
                mIsRunningPreferenceSetting = false;
                mAppLeafNodesMap.clear();
                Logger.v("stop preference setting");
            }

        }
        return START_STICKY;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
//        NodeUtils.printNodeTree(rootNode);

        // skip non app node
        if (!NodeUtils.isAppRootNode(this, rootNode)) {
            return;
        }

        /********** Preference Setting Functionality **********/
        // TODO: 11/5/16Saturday support multiple preferences (functionality)
        if (mIsRunningPreferenceSetting) {
            Logger.v("app node: " + NodeUtils.getBriefNodeInfo(rootNode));
            mAppRootNodePkgName = rootNode.getPackageName().toString();
            mAppLeafNodesMap.clear();
            parseLeafNodes(rootNode);

            // TODO: 11/5/16 extract preference related sub view tree here for app building
        }

        // or use NotifyService
//        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
//            final String packageName = String.valueOf(event.getPackageName());
//            if ("com.spotify.music".equals(packageName)) {
//                // represents the actual notification
//                final Parcelable payload = event.getParcelableData();
//                // check for a notification
//                if (!(payload instanceof Notification)) {
//                    Logger.v();("not notification");
//                    return;
//                }
//
//                final Notification notification = (Notification) payload;
//                final RemoteViews contentView = notification.contentView;
//                Logger.v();("contentView " + contentView.getPackage());
//            }
//        }

        // extracting view tree workflow
        // TODO: 10/21/16 extract view tree and send to wearable side based on app preference
        // TODO: 10/21/16 migrate previous on demand bitmap extraction
        // TODO: 10/21/16 possible caching and optimization here, including migrating previous
        // LRU cache and use worker thread (not AsyncTask, use handler thread)
        /********** Extracting Sub View Tree Based on App Preference  *********/
        String appPkgName = getNodePkgName(rootNode);
        boolean isAppEnabled = mEnabledAppListPreferences.getBoolean(appPkgName, false);
        if (!isAppEnabled) {
            return;
        }
        // read app preference json file
        readAppPreferenceNodesAsync(appPkgName, new AppNodesReadyCallback() {
            @Override
            public void onAppNodesReady(ArrayList<Pair<String, String>> nodes) {
                // TODO: 11/5/16Saturday find right preference before extracting
                // extract app subview tree and deliver to wear proxy
                for (Pair<String, String> pair : nodes) {
                    Logger.i("id: " + pair.first + " rect: " + pair.second);
                }
            }
        });

    }

    // parse all UI leaf nodes and save them to list mAppLeafNodesMap
    private void parseLeafNodes(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            Logger.v("null root node ");
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
                    Logger.v("add: " + NodeUtils.getBriefNodeInfo(rootNode));
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
            nodesIntent.putParcelableArrayListExtra(AVAILABLE_NODES_PREFERENCE_SETTING_KEY,
                    nodes);
            LocalBroadcastManager.getInstance(this).sendBroadcast(nodesIntent);
        } else {
            Logger.v("no available nodes to send!");
        }
    }

    // save rect-ID pair of user's selected UI nodes and persist them to app specific xml file
    // TODO: 10/21/16 save file format to activity granularity,
    // although currently it support all activities
    private void persistAppPreferenceNodesAsync(final ArrayList<Rect> preferredNodes) {
        // ensure that mAppLeafNodesMap is not cleared
        final HashMap<Rect, String> savedMap = new HashMap<>(mAppLeafNodesMap);
        final String appPkgName = mAppRootNodePkgName;

        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
                JSONObject root = new JSONObject();
                // save id, rect pair to json array
                JSONArray nodeItems = new JSONArray();
                for (Rect rect : preferredNodes) {
                    JSONObject item = new JSONObject();
                    String id = savedMap.get(rect);
                    String rectString = rect.flattenToString();
                    try {
                        item.put(ID_STRING, id);
                        item.put(RECT_STRING, rectString);
                    } catch (JSONException e) {
                        Logger.e(e.getMessage());
                    }
                    nodeItems.put(item);
                }

                // write json array to obb file
                File preferenceFile = new File(getObbDir(), appPkgName + JSON_EXT);
                try {
                    root.put(appPkgName, nodeItems);
                    FileUtils.writeFile(preferenceFile.getPath(),
                            root.toString(JSON_INDENT_SPACES), false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Logger.e(e.getMessage());
                }

                Message successMsg = mMainThreadHandler.obtainMessage();
                successMsg.what = PERSIST_PREFERENCE_NODES_SUCCESS;
                successMsg.obj = appPkgName;
                mMainThreadHandler.sendMessage(successMsg);
            }
        });
//        Logger.v();("mapped nodes: " + mAppLeafNodesMap.keySet().toString());
//        SharedPreferences sharedPref = getSharedPreferences(mAppRootNodePkgName, MODE_APPEND);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        for (Rect rect : preferredNodes) {
//            String id = savedMap.get(rect); // can't be null since already checked during put time
//            String rectString = rect.flattenToString();
//            Logger.v();("save preference: " + id + " " + rect);
//            // FIXME: 10/21/16 possibly to be improved
//            try {
//                String existingRect = sharedPref.getString(id, "");
//                Logger.v();("existingRect: " + existingRect);
//                if (existingRect.isEmpty()) {
//                    editor.putString(id, rectString);
//                } else {
//                    if (!existingRect.equals(rectString)) {
//                        HashSet<String> set = new HashSet<>();
//                        set.add(existingRect);
//                        set.add(rectString);
//                        Logger.v();("set add two: " + Arrays.toString(set.toArray()));
//                        editor.putStringSet(id, set);
//                    }
//                }
//            } catch (ClassCastException e) { // catch return Set exception
//                Logger.e(e.getMessage());
//                Set<String> set = sharedPref.getStringSet(id, new HashSet<String>());
//                if (set.size() > 0) {
//                    Logger.v();("existing set: " + id + " " + Arrays.toString(set.toArray()));
//                    set.add(rectString);
//                    Logger.v();("set add one: " + id + " " + Arrays.toString(set.toArray()));
//                    editor.putStringSet(id, set);
//                }
//            }
//            // need to commit here, otherwise file not correct!
//            editor.commit();
//
//        }
//        editor.apply();

    }

    private void readAppPreferenceNodesAsync(final String appPkgName,
            final AppNodesReadyCallback appNodesReadyCallback) {
        ArrayList<Pair<String, String>> nodes = mAppPreferenceNodesCache.get(appPkgName);

        // already in cache, no need to read from file
        if (nodes != null) {
            Logger.v("read app: " + appPkgName + " nodes from cache");
            appNodesReadyCallback.onAppNodesReady(nodes);
            return;
        }

        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
                ArrayList<Pair<String, String>> nodes = new ArrayList<>();
                File preferenceFile = new File(getObbDir(), appPkgName + JSON_EXT);
                StringBuilder sb = FileUtils.readFile(preferenceFile.getPath());

                try {
                    JSONObject root = new JSONObject(sb.toString());
                    JSONArray nodeItems = root.getJSONArray(appPkgName);
                    for (int i = 0; i < nodeItems.length(); i++) {
                        JSONObject object = nodeItems.getJSONObject(i);
                        String id = (String) object.get(ID_STRING);
                        String rect = (String) object.get(RECT_STRING);

                        Pair<String, String> pair = new Pair<>(id, rect);
                        nodes.add(pair);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Logger.e(e.getMessage());
                }

                mAppPreferenceNodesCache.put(appPkgName, nodes);
                appNodesReadyCallback.onAppNodesReady(nodes);
//                Message nodesMsg = mMainThreadHandler.obtainMessage();
//                nodesMsg.what = READ_PREFERENCE_NODES_SUCCESS;
//                nodesMsg.obj = new Pair<>(appPkgName, nodes);
//                mMainThreadHandler.sendMessage(nodesMsg);
            }
        });
    }

    @Override
    public void onInterrupt() {
        Logger.v("");

    }

    @Override
    public void onDestroy() {
        Logger.v("");
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
