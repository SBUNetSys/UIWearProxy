package edu.stonybrook.cs.netsys.uiwearproxy.uiwearService;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ACCESSIBILITY_SETTING_INTENT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.AVAILABLE_NODES_PREFERENCE_SETTING_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.BITMAP_CACHE_SIZE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.CLICK_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ENABLED_APPS_PREF_NAME;
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
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.RUNNING_APP_PREF_CACHE_SIZE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.TIME_FORMAT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.XML_EXT;
import static edu.stonybrook.cs.netsys.uiwearlib.NodeUtils.getNodePkgName;

import android.accessibilityservice.AccessibilityService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.text.format.DateFormat;
import android.util.LruCache;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cscao.libs.gmswear.GmsWear;
import com.cscao.libs.gmswear.connectivity.FileTransfer;
import com.cscao.libs.gmswear.consumer.AbstractDataConsumer;
import com.cscao.libs.gmswear.consumer.DataConsumer;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.MessageEvent;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.stonybrook.cs.netsys.uiwearlib.AppUtil;
import edu.stonybrook.cs.netsys.uiwearlib.DataBundle;
import edu.stonybrook.cs.netsys.uiwearlib.DataNode;
import edu.stonybrook.cs.netsys.uiwearlib.FileUtils;
import edu.stonybrook.cs.netsys.uiwearlib.NodeUtils;
import edu.stonybrook.cs.netsys.uiwearlib.WorkerThread;
import edu.stonybrook.cs.netsys.uiwearlib.XmlUtils;
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
    private LruCache<String, ArrayList<Pair<String, Rect>>> mAppPreferenceNodesCache =
            new LruCache<>(RUNNING_APP_PREF_CACHE_SIZE);

    private HashSet<Pair<String, Rect>> mAppNodes = new HashSet<>();
    private HashMap<Pair<String, Rect>, AccessibilityNodeInfo> mPairAccessibilityNodeMap =
            new HashMap<>();
    private LruCache<String, Bitmap> mBitmapLruCache = new LruCache<String, Bitmap>(
            BITMAP_CACHE_SIZE) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than number of items.
            return bitmap.getByteCount();
        }
    };

    // for removing multiple duplicate accessibility events
    private AccessibilityEvent mLastProcessedEvent;

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

    private GmsWear mGmsWear;
    private DataConsumer mDataConsumer;

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

        mGmsWear = GmsWear.getInstance();
        mDataConsumer = new AbstractDataConsumer() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent) {
                Logger.i("onMessageReceived");
            }

            @Override
            public void onDataChanged(DataEvent event) {
                Logger.i("onDataChanged");
            }
        };

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
        mGmsWear.addWearConsumer(mDataConsumer);
        return START_STICKY;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        final AccessibilityNodeInfo rootNode = getRootInActiveWindow();
//        NodeUtils.printNodeTree(rootNode);

        // skip non app node
        if (!NodeUtils.isAppRootNode(this, rootNode)) {
            return;
        }

        /********** Preference Setting Functionality **********/
        if (mIsRunningPreferenceSetting) {
            Logger.v("app node: " + NodeUtils.getBriefNodeInfo(rootNode));
            mAppRootNodePkgName = rootNode.getPackageName().toString();
            mAppLeafNodesMap.clear();
            parseLeafNodes(rootNode);

            // TODO: 11/5/16 extract preference related sub view tree here for app building
        }

        // remove duplicate events
//        if (event.equals(mLastProcessedEvent)) {
//            Logger.v("duplicate events");
//            Logger.v("event : " + event);
//            Logger.v("root node: " + NodeUtils.getBriefNodeInfo(rootNode));
//            Logger.v("source node: " + NodeUtils.getBriefNodeInfo(event.getSource()));
//            return;
//        }
//        mLastProcessedEvent = event;

        // TODO: 11/10/16 support notification capture, code below or use NotifyService
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

        /********** Extracting Sub View Tree Based on App Preference  *********/
        String appPkgName = getNodePkgName(rootNode);
        boolean isAppEnabled = mEnabledAppListPreferences.getBoolean(appPkgName, false);
        if (!isAppEnabled) {
            return;
        }

        File preferenceFolder = new File(getFilesDir() + File.separator + appPkgName);
        if (!preferenceFolder.exists()) {
            Logger.i("%s pref not exists!", preferenceFolder.getPath());
            return;
        }

        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
                mAppNodes.clear();
                mPairAccessibilityNodeMap.clear();
                parseAppNodesToSet(rootNode);
            }
        });


        // read app preference xml file
        readAppPreferenceNodesAsync(preferenceFolder, new AppNodesReadyCallback() {
            @Override
            public void onAppNodesReady(String prefCacheKey, ArrayList<Pair<String, Rect>> nodes) {
                // decide whether the preference nodes are subset of current app nodes
                Logger.v("pref cache key: " + prefCacheKey);
                HashSet<Pair<String, Rect>> preferenceSet = new HashSet<>(nodes);
                if (!mAppNodes.containsAll(preferenceSet)) {
                    // root node from non preference screen, so skip
//                    Logger.v("node set: " + mAppNodes.toString());
//                    Logger.v("preferenceSet: " + preferenceSet.toString());
//                    Logger.v("onAppNodesReady skip");
                    return;
                }

                // begin extracting preference view tree info
                DataBundle dataBundle = new DataBundle(prefCacheKey);

                for (Pair<String, Rect> pair : nodes) {
                    Logger.i("id: " + pair.first + " rect: " + pair.second);
                    AccessibilityNodeInfo accNode = mPairAccessibilityNodeMap.get(pair);
                    DataNode dataNode = new DataNode(accNode);
                    Bitmap nodeBitmap = mBitmapLruCache.get(dataNode.getUniqueId());
                    if (nodeBitmap == null) {
                        Bundle bitmapBundle = new Bundle();
                        // TODO: 11/10/16 import android.jar to use new API
                        //  accNode.requestSnapshot(bitmapBundle);
                        nodeBitmap = (Bitmap) bitmapBundle.get("bitmap");
                        if (nodeBitmap == null) {
                            Logger.w("cannot get bitmap");
                        } else {
                            mBitmapLruCache.put(dataNode.getUniqueId(), nodeBitmap);
                        }
                    }
                    dataNode.setImage(nodeBitmap);
                    Logger.i(dataNode.toString());
                    dataBundle.add(dataNode);
                }

                Logger.i(dataBundle.toString());
                GmsWear.getInstance().sendMessage(CLICK_PATH, "test msg".getBytes());
                // send data nodes to wearable proxy with prefCacheKey
                byte[] data = AppUtil.marshall(dataBundle);
                Logger.i("data: " + data.length);
                mGmsWear.syncAsset(DATA_BUNDLE_PATH, DATA_BUNDLE_KEY, data, true);
//                sendDataBundleToWearableAsync(data);
            }
        });

    }

    // can transfer apk file
    private void sendFileToWearableAsync(final File fileName) {
        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
                // send to wearable
                FileTransfer fileTransferHighLevel = new FileTransfer.Builder()
                        .setFile(fileName).build();
                fileTransferHighLevel.startTransfer();
            }
        });
    }

    private void parseAppNodesToSet(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            Logger.v("null app root node ");
            return;
        }

        String id = rootNode.getViewIdResourceName();
        Rect rect = new Rect();
        rootNode.getBoundsInScreen(rect);
        Pair<String, Rect> pair = new Pair<>(id, rect);
        mAppNodes.add(pair);
        mPairAccessibilityNodeMap.put(pair, rootNode);
        int count = rootNode.getChildCount();

        for (int i = 0; i < count; i++) {
            parseAppNodesToSet(rootNode.getChild(i));
        }

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
    // support multi-screen preference
    private void persistAppPreferenceNodesAsync(final ArrayList<Rect> preferredNodes) {
        // ensure that mAppLeafNodesMap is not cleared
        final HashMap<Rect, String> savedMap = new HashMap<>(mAppLeafNodesMap);
        final String appPkgName = mAppRootNodePkgName;

        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
                // write xml file to obb file
                CharSequence date = DateFormat.format(TIME_FORMAT, new java.util.Date());
                File preferenceFile = new File(getFilesDir() + File.separator + appPkgName,
                        date + XML_EXT);
                try {
                    XmlUtils.serializeAppPreference(preferenceFile, preferredNodes, savedMap);
                } catch (IOException e) {
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

    private void readAppPreferenceNodesAsync(final File preferenceFolder,
            final AppNodesReadyCallback appNodesReadyCallback) {

        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
                for (File preferenceFile : preferenceFolder.listFiles()) {
                    Logger.v("pref path: " + preferenceFile.getPath());

                    String cacheKey = FileUtils.getParentName(preferenceFile) + File.separator
                            + FileUtils.getBaseName(preferenceFile);
                    Logger.v("cache key: " + cacheKey);

                    ArrayList<Pair<String, Rect>> nodes = mAppPreferenceNodesCache.get(cacheKey);

                    if (nodes == null) {
                        nodes = XmlUtils.deserializeAppPreference(
                                preferenceFile);
                        mAppPreferenceNodesCache.put(cacheKey, nodes);
                        Logger.v("from file");
                    } else {
                        Logger.v("from cache");
                    }

                    appNodesReadyCallback.onAppNodesReady(cacheKey, nodes);
                }

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
