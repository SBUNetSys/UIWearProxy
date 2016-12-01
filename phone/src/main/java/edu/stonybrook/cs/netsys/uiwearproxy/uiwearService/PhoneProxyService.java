package edu.stonybrook.cs.netsys.uiwearproxy.uiwearService;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ACCESSIBILITY_SETTING_INTENT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.AVAILABLE_NODES_PREFERENCE_SETTING_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_REQUIRED_IMAGE_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ENABLED_APPS_PREF_NAME;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.IMAGE_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.MSG_CAPABILITY;
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
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_PHONE_RESOLUTION_RATIO_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_PHONE_RESOLUTION_RATIO_PREF_NAME;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_RESOLUTION_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_RESOLUTION_REQUEST_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.XML_EXT;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CLICK_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.MAPPING_RULE_DIR;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.PREFERENCE_DIR;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.getResDir;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.marshall;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.unmarshall;
import static edu.stonybrook.cs.netsys.uiwearlib.helper.AppUtil.getBitmapBytes;
import static edu.stonybrook.cs.netsys.uiwearlib.helper.AppUtil.getImageCacheFolderPath;
import static edu.stonybrook.cs.netsys.uiwearlib.helper.NodeUtil.getBriefNodeInfo;
import static edu.stonybrook.cs.netsys.uiwearlib.helper.NodeUtil.getNodePkgName;

import android.accessibilityservice.AccessibilityService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.LruCache;
import android.util.SparseArray;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cscao.libs.gmsapi.GmsApi;
import com.google.android.gms.wearable.DataMap;
import com.orhanobut.logger.Logger;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.stonybrook.cs.netsys.uiwearlib.WorkerThread;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.AccNode;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataAction;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataBundle;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataNode;
import edu.stonybrook.cs.netsys.uiwearlib.helper.FileUtil;
import edu.stonybrook.cs.netsys.uiwearlib.helper.NodeUtil;
import edu.stonybrook.cs.netsys.uiwearlib.helper.XmlUtil;
import edu.stonybrook.cs.netsys.uiwearproxy.R;

public class PhoneProxyService extends AccessibilityService {

    private GmsApi mGmsApi;
    private int mPhoneWidth;
    private int mPhoneHeight;
    private NotificationManager mNotificationManager;

    // for preference setting, only need to parse once
    private boolean mIsRunningPreferenceSetting;
    private boolean mIsLoggingActionBenchmark;

    // either region or id alone is not enough for detecting the specific UI elements
    // so combined them and can cover most cases
    // must use region as key, since id can be the same
    private HashMap<Rect, AccNode> mAppNodesMapForPreferenceSetting = new HashMap<>();
    private String mAppRootNodePkgName;

    // for bitmap extracting and other heavy work
    // TODO: 11/13/16 refactor this use RxJava or EventBus
    private WorkerThread mWorkerThread;

    private ThreadPoolExecutor mThreadPool;

    // save parsed AccessibilityNodeInfo for performing action
    private SparseArray<AccessibilityNodeInfo> mActionNodes = new SparseArray<>();

    // main thread handler
    private Handler mMainThreadHandler;

    private SharedPreferences mEnabledAppsSharedPref;
    private SharedPreferences mWatchPhoneResolutionRatioSharedPref;

    // list of app preference mNodes, for each pair, the first is id, the second is rect
    private LruCache<String, HashSet<AccNode>> mAppPreferenceNodesCache =
            new LruCache<>(RUNNING_APP_PREF_CACHE_SIZE);

    private HashMap<AccNode, AccessibilityNodeInfo> mPairAccessibilityNodeMap =
            new HashMap<>();
    private Bundle mViewIdCountMap = new Bundle();

    // to avoid same data retransmission due to duplicate accessibility events
    private DataBundle mLastSentDataBundle;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case PREFERENCE_SETTING_STARTED:
                    Logger.t("pref").v("preference activity started");
                    sendLeafNodesToPreferenceSetting();
                    break;

                case PREFERENCE_SETTING_SAVE:
                    ArrayList<Rect> preferredRect =
                            intent.getParcelableArrayListExtra(PREFERENCE_NODES_KEY);
                    persistAppPreferenceNodesAsync(preferredRect);
                    // TODO: 11/5/16  read mapping rule and start build app process
                    Logger.t("pref").v("received preferredRect");
                    break;

                case PREFERENCE_SETTING_EXIT:

                    Logger.t("pref").v("setting exit ");
                    break;
                default:
            }
        }
    };

    // send prepared leaf mNodes to PreferenceSettingActivity
    private void sendLeafNodesToPreferenceSetting() {
        if (mAppNodesMapForPreferenceSetting.size() > 0) {
            Intent nodesIntent = new Intent(NODES_AVAILABLE);
            ArrayList<Rect> nodes = new ArrayList<>(mAppNodesMapForPreferenceSetting.keySet());
            nodesIntent.putParcelableArrayListExtra(AVAILABLE_NODES_PREFERENCE_SETTING_KEY,
                    nodes);
            LocalBroadcastManager.getInstance(this).sendBroadcast(nodesIntent);
        } else {
            Logger.t("pref").v("no available mNodes to send!");
        }
    }

    // save rect-ID pair of user's selected UI mNodes and persist them to app specific xml file
    // support multi-screen preference
    private void persistAppPreferenceNodesAsync(final ArrayList<Rect> preferredNodes) {
        // ensure that mAppNodesMapForPreferenceSetting is not cleared
        final HashMap<Rect, AccNode> savedMap = new HashMap<>(
                mAppNodesMapForPreferenceSetting);
        final String appPkgName = mAppRootNodePkgName;

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                // write xml file to obb file
                CharSequence date = DateFormat.format(TIME_FORMAT, new java.util.Date());
                File preferenceFile = new File(
                        getResDir(PREFERENCE_DIR, appPkgName),
                        date + XML_EXT);
                try {
                    XmlUtil.serializeAppPreference(preferenceFile, preferredNodes, savedMap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.t("pref").e(e.getMessage());
                }

                Message successMsg = mMainThreadHandler.obtainMessage();
                successMsg.what = PERSIST_PREFERENCE_NODES_SUCCESS;
                successMsg.obj = appPkgName;
                mMainThreadHandler.sendMessage(successMsg);
            }
        });

    }

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
                        Logger.t("pref").v(msg.obj + " preference saved");
                        break;
                    case READ_PREFERENCE_NODES_SUCCESS:
                        Logger.t("pref").v("preference read");

                    default:
                        Logger.e("unknown msg");
                }
            }
        };

        mEnabledAppsSharedPref = getSharedPreferences(ENABLED_APPS_PREF_NAME,
                Context.MODE_PRIVATE);
        mWatchPhoneResolutionRatioSharedPref = getSharedPreferences(
                WATCH_PHONE_RESOLUTION_RATIO_PREF_NAME,
                Context.MODE_PRIVATE);

        mWorkerThread = new WorkerThread("worker-thread");
        mWorkerThread.start();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        raiseRunningNotification();

        mGmsApi = new GmsApi(this, MSG_CAPABILITY);
        mGmsApi.setOnMessageReceivedListener(new GmsApi.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(GmsApi.MessageData messageEvent) {
                switch (messageEvent.getPath()) {
                    case CLICK_PATH:
                        byte[] actionData = messageEvent.getData();
                        DataAction dataAction = unmarshall(actionData, DataAction.CREATOR);
//                        Logger.i("action data received: " + dataAction);
                        Log.d("BENCH", "action received on phone, size : " + actionData.length);
                        mIsLoggingActionBenchmark = true;
                        performActionOnPhone(dataAction);
                        Log.d("BENCH", "action performed on phone");

                        break;
                    case WATCH_RESOLUTION_PATH:
                        byte[] watchResolution = messageEvent.getData();
                        Point size = unmarshall(watchResolution, Point.CREATOR);
                        Logger.i("watch resolution received: " + size);
                        SharedPreferences.Editor editor =
                                mWatchPhoneResolutionRatioSharedPref.edit();
                        int phoneSize = Math.max(mPhoneHeight, mPhoneWidth);
                        int watchSize = Math.min(size.x, size.y);
                        int ratio = phoneSize / watchSize;
                        editor.putInt(WATCH_PHONE_RESOLUTION_RATIO_KEY, ratio);
                        editor.apply();
                        break;
                    case DATA_BUNDLE_REQUIRED_IMAGE_PATH:
                        byte[] dataBundleBytes = messageEvent.getData();
                        DataBundle dataBundle = unmarshall(dataBundleBytes, DataBundle.CREATOR);
                        updateDataBundleImage(dataBundle);
                        Logger.i("new data bundle required image received: " + dataBundle);
                        break;
                    default:
                        Logger.w("unknown msg");
                }
            }

            private void updateDataBundleImage(DataBundle dataBundle) {
                // process data bundle with image data
                // FIXME: 11/30/16Wednesday need to load image from disk and send to wear
                // cannot happen usually, since all image data are immediately send to wear
                byte[] dataBundleBytes = marshall(dataBundle);
                mGmsApi.sendMsg(DATA_BUNDLE_PATH, dataBundleBytes, null);
            }
        });
        mGmsApi.setOnDataChangedListener(new GmsApi.OnDataChangedListener() {
            @Override
            public void onDataChanged(DataMap dataMap) {
            }

            @Override
            public void onDataDeleted(DataMap dataMap) {

            }
        });

        Point size = new Point();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getSize(size);
        mPhoneWidth = size.x;
        mPhoneHeight = size.y;

        mThreadPool = new ThreadPoolExecutor(2, 4, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    // TODO: 11/24/16 first find node in cached hash map, if not found, use getRootInActiveWindow
    private void performActionOnPhone(DataAction dataAction) {
        // FIXME: 11/15/16 how to use this?
//        String pkgName = dataAction.getPkgName();

        int actionId = dataAction.getActionId();
        AccessibilityNodeInfo node = mActionNodes.get(actionId);
        boolean hasPerformed = performActionUseAccessibility(node);
        if (hasPerformed) {
            Logger.i("performed success use acc");
        } else {
            Logger.w("performed failed");
        }
    }

    private boolean performActionUseAccessibility(AccessibilityNodeInfo node) {
        if (node != null) {
            AccessibilityNodeInfo parent = node;
            while (parent != null && !parent.isClickable()) {
                parent = parent.getParent();
            }
            if (parent != null) {
                Logger.i("perform acc: " + getBriefNodeInfo(parent));
                return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int startCode = intent.getIntExtra(PREFERENCE_SETTING_KEY, 0);
            if (startCode == PREFERENCE_SETTING_CODE) {
                mIsRunningPreferenceSetting = true;
                Logger.t("pref").v("start preference setting");
            }

            int stopCode = intent.getIntExtra(PREFERENCE_STOP_KEY, 0);
            if (stopCode == PREFERENCE_STOP_CODE) {
                mIsRunningPreferenceSetting = false;
                mAppNodesMapForPreferenceSetting.clear();
                Logger.t("pref").v("stop preference setting");
            }

        }
        // reset all cache here
        resetAllCacheHere();

        return START_STICKY;
    }

    private void resetAllCacheHere() {
        Logger.v("reset cache");
//        mBitmapLruCache.evictAll();
        mAppPreferenceNodesCache.evictAll();
        mLastSentDataBundle = null;
        mAppRootNodePkgName = null;
        mAppNodesMapForPreferenceSetting.clear();
        mPairAccessibilityNodeMap.clear();
        mViewIdCountMap.clear();
        mActionNodes.clear();
//        mDataBundleLruCache.evictAll();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (mIsLoggingActionBenchmark) {
            Log.d("BENCH", "action click trigger event");
            mIsLoggingActionBenchmark = false;
        }

        final AccessibilityNodeInfo rootNode = getRootInActiveWindow();

        // skip non app node
        if (!NodeUtil.isAppRootNode(this, rootNode)) {
            return;
        }

        AccessibilityNodeInfo sourceNode = event.getSource();
        Logger.t("event").v("event : " + event);
        Logger.t("event").v("root node: " + NodeUtil.getBriefNodeInfo(rootNode));
        Logger.t("event").v("source node: " + NodeUtil.getBriefNodeInfo(sourceNode));
        if (sourceNode == null) {
            return;
        }

        /********** Preference Setting Functionality **********/
        if (mIsRunningPreferenceSetting) {
            Logger.t("pref").v("app node: " + NodeUtil.getBriefNodeInfo(rootNode));
            mAppRootNodePkgName = rootNode.getPackageName().toString();
            mAppNodesMapForPreferenceSetting.clear();
            AccNode rootAccNode = new AccNode(rootNode);
            parseNodesForPreferenceSetting(rootAccNode);

            // TODO: 11/5/16 extract preference related sub view tree here for app building

            // when setting preference, won't extract sub view tree content
            return;
        }

        /********** Extracting Sub View Tree Based on App Preference  *********/
        final String appPkgName = getNodePkgName(rootNode);
        // register app for background processing
//        setAppBackgroundAlive(appPkgName);
        Log.i("STATS", event.toString() + sourceNode.toString());
        // even root node is app, if accessibility event is from non app node, then skip
        if (!appPkgName.equals(sourceNode.getPackageName())) {
            return;
        }

        boolean isAppEnabled = mEnabledAppsSharedPref.getBoolean(appPkgName, false);
        if (!isAppEnabled) {
            return;
        }

        File preferenceFolder = new File(getResDir(PREFERENCE_DIR, appPkgName));
        if (!preferenceFolder.exists()) {
            Logger.t("pref").v("%s pref not exists!", preferenceFolder.getPath());
            return;
        }

        File mappingRuleFolder = new File(getResDir(MAPPING_RULE_DIR, appPkgName));
        if (!mappingRuleFolder.exists()) {
            Logger.t("mapping").v("%s mapping rule not exists!", mappingRuleFolder.getPath());
            return;
        }

        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
//                mAppNodes.clear();
                mViewIdCountMap.clear();
                mPairAccessibilityNodeMap.clear();
//                NodeUtil.printNodeTree(rootNode);
                parseAppNodes(rootNode);
            }
        });

        // read app preference xml file and extract view tree content if nodes ready
        readAppPreferenceNodesAsync(preferenceFolder, new AppNodesReadyCallback() {
            @Override
            public void onAppNodesReady(String preferenceId, HashSet<AccNode> nodes) {
                // decide whether the preference mNodes are subset of current app mNodes
                Logger.v("pref id: " + preferenceId);
                Logger.v("pref nodes: " + nodes);

                // begin extracting preference view tree info
                DataBundle dataBundle = new DataBundle(appPkgName, preferenceId);
                parseNodeData(nodes, dataBundle);

                if (isDataBundleDuplicate(dataBundle)) {
                    // no need to further processing
                    return;
                } else {
                    pruneDataBundle(dataBundle);
                }
                sendDataBundleToWear(dataBundle);
            }

            private void sendDataBundleToWear(DataBundle dataBundle) {
                byte[] data = marshall(dataBundle);
                Logger.i("new data bundle:" + dataBundle);
                mGmsApi.sendMsg(DATA_BUNDLE_PATH, data, null);
            }
        });

    }

    // parse all UI nodes and save them to list mAppNodesMapForPreferenceSetting
    private void parseNodesForPreferenceSetting(AccNode rootNode) {
        if (rootNode == null) {
            Logger.t("pref").v("null root node ");
            return;
        }
        Rect region = rootNode.getRectInScreen();
        String id = rootNode.getViewId();
        //        if (!region.isEmpty())
        if (rootNode.getClassName().endsWith("Layout")) {
            // id cannot be null for container view like linear layout
            Logger.t("pref").v("parseNodes add layout: " + rootNode);
            mAppNodesMapForPreferenceSetting.put(region, rootNode);
        } else {
            if (id == null) {
                Logger.t("pref").v("parseNodes null id" + rootNode);
            } else {
                Logger.t("pref").v("parseNodes add leaf: " + rootNode);
                mAppNodesMapForPreferenceSetting.put(region, rootNode);
            }
        }
        int count = rootNode.getChildCount();
        for (int i = 0; i < count; i++) {
            parseNodesForPreferenceSetting(rootNode.getChild(i));
        }
    }

    private void parseAppNodes(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            Logger.t("parse").v("null app root node ");
            return;
        }

        String viewId = rootNode.getViewIdResourceName();
        Rect rect = new Rect();
        rootNode.getBoundsInScreen(rect);
        if (rect.isEmpty() || viewId == null || !rootNode.isVisibleToUser()) {
            Logger.t("parse").v("node: " + rect + " " + viewId);
        } else {
            AccNode node = new AccNode();
            int childCount = rootNode.getChildCount();
            if (childCount > 0 && childCount < 10) {
                node.setViewId(viewId);
                node.setRectInScreen(rect);
                ArrayList<AccNode> children = node.getChildNodes();
                node.setChildNodes(parseChildLeafNodes(children, rootNode));
                Logger.t("parse").v("node: norm " + node);

            } else if (childCount == 0) {
                // node has no children, so only set mId, mViewId, mRectInScreen and mClassName
                node = new AccNode(rootNode);
                Logger.t("parse").v("node: leaf " + node);
            } else {
                Logger.t("parse").v("node: too many children, too deep, not parse ");
            }

            int viewIdCount = mViewIdCountMap.getInt(viewId);
            mViewIdCountMap.putInt(viewId, ++viewIdCount);

            mPairAccessibilityNodeMap.put(node, rootNode);
        }

        int count = rootNode.getChildCount();

        for (int i = 0; i < count; i++) {
            parseAppNodes(rootNode.getChild(i));
        }

    }

    private ArrayList<AccNode> parseChildLeafNodes(ArrayList<AccNode> list,
            AccessibilityNodeInfo node) {
        if (node == null) {
            return list;
        }
        int count = node.getChildCount();

        if (count == 0) {
            // skip null viewId for leaf nodes
            if (node.getViewIdResourceName() != null) {
                // node has no children, so only set mId, mViewId, mRectInScreen and mClassName
                AccNode accNode = new AccNode(node);
                list.add(accNode);
            }
        } else {
            for (int i = 0; i < count; i++) {
                list = parseChildLeafNodes(list, node.getChild(i));
            }
        }

        return list;
    }

    private void readAppPreferenceNodesAsync(final File preferenceFolder,
            final AppNodesReadyCallback appNodesReadyCallback) {

        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
                for (File preferenceFile : preferenceFolder.listFiles()) {
                    Logger.t("pref").v("path: " + preferenceFile.getPath() + " name: "
                            + preferenceFile.getName());

                    String cacheKey = FileUtil.getParentName(preferenceFile) + File.separator
                            + FileUtil.getBaseName(preferenceFile);
                    Logger.t("pref").v("cache key: " + cacheKey);

                    HashSet<AccNode> prefNodesFromFile = mAppPreferenceNodesCache.get(cacheKey);

                    if (prefNodesFromFile == null) {
                        prefNodesFromFile = XmlUtil.deserializeAppPreference(
                                preferenceFile);
                        mAppPreferenceNodesCache.put(cacheKey, prefNodesFromFile);
                        Logger.t("pref").v("from file: " + prefNodesFromFile);
                    } else {
                        Logger.t("pref").v("from cache: " + prefNodesFromFile);
                    }

                    HashSet<AccNode> nodes = new HashSet<>(prefNodesFromFile);
                    if (!appNodesContainPreferenceNodes(nodes)) {
                        //root node from non preference screen, so skip
                        Logger.t("pref").v("not contain preference nodes, skip");
                        continue;
                    }
                    Logger.t("pref").v("from contain preference nodes: " + nodes);

                    appNodesReadyCallback.onAppNodesReady(FileUtil.getBaseName(preferenceFile),
                            nodes);
                }

//                Message nodesMsg = mMainThreadHandler.obtainMessage();
//                nodesMsg.what = READ_PREFERENCE_NODES_SUCCESS;
//                nodesMsg.obj = new Pair<>(appPkgName, mNodes);
//                mMainThreadHandler.sendMessage(nodesMsg);
            }
        });
    }

    private boolean appNodesContainPreferenceNodes(HashSet<AccNode> preferenceNodes) {
        ArrayList<AccNode> prefNodes = new ArrayList<>(preferenceNodes);
        ArrayList<AccNode> appNodes = new ArrayList<>(mPairAccessibilityNodeMap.keySet());
        Logger.v("node app  set: " + appNodes.toString());
        Logger.v("node pref set: " + prefNodes.toString());

        /*** compare viewId, if multiple nodes have the same id, then use rect size ***/
        boolean oneNodeMatched = true;
        // for node have the same id, only use oneNodeMatched is not enough, since it does not
        // break the loop after one match thus oneNodeMatched may become false later, but in fact
        // should be true in this situation
        boolean atLeastOneNodeMatched = false;
        for (AccNode prefNode : prefNodes) {
            for (AccNode appNode : appNodes) {
                int count = mViewIdCountMap.getInt(appNode.getViewId());
                //multiple nodes have the same id
                if (count > 1) {
                    oneNodeMatched = prefNode.matches(appNode, 0);
                    if (oneNodeMatched) {
                        atLeastOneNodeMatched = true;
                        Logger.v("node match: multiple app- " + appNode + " pref-" + prefNode);
                        // need to update the prefNode to appNode
                        updatePreferenceNode(preferenceNodes, prefNode, appNode);
                        // do not break here, need iterate all nodes that have the same viewID
                    }
                } else {
                    //no two nodes have the same id
                    oneNodeMatched = prefNode.getViewId().equals(appNode.getViewId());
                    if (oneNodeMatched) {
                        Logger.v("node match: single app- " + appNode + " pref- " + prefNode);
                        // need to update the prefNode to appNode
                        updatePreferenceNode(preferenceNodes, prefNode, appNode);
                        break;
                    }
                }
            }

            if (!oneNodeMatched && !atLeastOneNodeMatched) {
                // find one node that does not in appNodes
                Logger.d("node not match");
                return false;
            }
        }

        Logger.d("node matched");
        Logger.v("node matched app  set: " + appNodes.toString());
        Logger.v("node matched pref set: " + preferenceNodes.toString());
        return true;
    }

    private void updatePreferenceNode(HashSet<AccNode> preferenceNodes, AccNode prefNode,
            AccNode appNode) {
//        // remove the old pref node, update it to the matched app node
        preferenceNodes.remove(prefNode);
        AccNode newNode = new AccNode(appNode);

        // delete unnecessary nodes, i.e., those not in prefNodes
        for (int i = 0; i < newNode.getChildCount(); i++) {
            AccNode appChild = newNode.getChild(i);
            String appChildViewId = appChild.getViewId();
            boolean hasFoundChild = false;
            for (int j = 0; j < prefNode.getChildCount(); j++) {
                String prefChildViewId = prefNode.getChild(j).getViewId();
                if (appChildViewId.equals(prefChildViewId)) {
                    hasFoundChild = true;
                }
            }
            if (!hasFoundChild) {
                newNode.removeChild(appChild);
            }
        }

        preferenceNodes.add(newNode);
    }

    private void parseNodeData(HashSet<AccNode> accNodes, DataBundle dataBundle) {
        Logger.d("accNodes: " + accNodes);
        ArrayList<AccNode> listNodes = new ArrayList<>();
        for (AccNode accNode : accNodes) {
            Logger.i("accNode : " + accNode);
            if (accNode.getChildCount() > 0) {
                // this is a list item preference node
                listNodes.add(accNode);
            } else {
                // normal single node item
                AccessibilityNodeInfo nodeInfo = mPairAccessibilityNodeMap.get(accNode);
                if (nodeInfo == null) {
                    Logger.w("accNode norm child null for dataBundle: " + dataBundle);
                    return;
                }
                Logger.d("accNode norm child: " + getBriefNodeInfo(nodeInfo));
                DataNode dataNode = getDataNode(nodeInfo);
                dataBundle.add(dataNode);
            }
        }

        // need to sort list nodes based on the screen position
        Collections.sort(listNodes, new Comparator<AccNode>() {
            @Override
            public int compare(AccNode lhs, AccNode rhs) {
                // need to compare x and y coordinate
                int xDiff = lhs.getRectInScreen().centerX() - rhs.getRectInScreen().centerX();
                int yDiff = lhs.getRectInScreen().centerY() - rhs.getRectInScreen().centerY();

                if (xDiff == 0) {
                    return yDiff;
                } else {
                    return xDiff;
                }
            }
        });


        // need to add list view layout node to normal data nodes, set viewId info
        if (listNodes.size() > 0) {
            AccNode node = listNodes.get(0);
            DataNode listNode = new DataNode(node.getViewId());
            dataBundle.add(listNode);
        }

        // parse list nodes and set to dataBundle
        for (AccNode accNode : listNodes) {
            int count = accNode.getChildCount();
            DataNode[] dataNodes = new DataNode[count];
            for (int i = 0; i < count; i++) {
                AccNode node = accNode.getChild(i);
                // get AccessibilityNodeInfo based on node
                AccessibilityNodeInfo nodeInfo = mPairAccessibilityNodeMap.get(node);
                if (nodeInfo != null) {
                    Logger.d("accNode list child: " + getBriefNodeInfo(nodeInfo));
                    dataNodes[i] = getDataNode(nodeInfo);
                } else {
                    Logger.w("accNode list child null");
                }
            }
            dataBundle.add(dataNodes);
        }
    }

    @NonNull
    private DataNode getDataNode(@NonNull AccessibilityNodeInfo nodeInfo) {
        // save node for future perform action
        mActionNodes.put(nodeInfo.hashCode(), nodeInfo);

        DataNode dataNode = new DataNode(nodeInfo);
        String bitmapHash = getNodeBitmapHash(nodeInfo);
        dataNode.setImageHash(bitmapHash);
        Logger.i(dataNode.toString());
        return dataNode;
    }

    private String getNodeBitmapHash(AccessibilityNodeInfo accNode) {
        // FIXME: 11/12/16 based on mapping rule, not all mNodes need image/bitmap
        if ("android.widget.TextView".equals(accNode.getClassName())) {
            Logger.v("text view no need to extract bitmap");
            return null;
        }

        Bitmap nodeBitmap = requestBitmap(accNode);
        if (nodeBitmap == null) {
            Logger.w("cannot get bitmap");
            return null;
        }

        nodeBitmap = getScaledBitmap(nodeBitmap);
        final String bitmapPath = getImageCacheFolderPath();

        final byte[] imageBytes = getBitmapBytes(nodeBitmap);
        if (imageBytes != null) {
            Logger.i("image bytes: " + imageBytes.length);
        } else {
            Logger.w("image bytes null");
            return null;
        }

        final String imageHash = Integer.toHexString(Arrays.hashCode(imageBytes));
        // for the new nodes, instead of sending heavy image data, tell wear proxy
        // the hash value of image, if found not found real image data in wear's
        // cache, need notify phone proxy to send real data
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                // save to local disk cache repo
                try {
                    File imageFile = new File(bitmapPath, imageHash + ".png");
                    if (!imageFile.exists()) {
                        mGmsApi.sendMsg(IMAGE_PATH, imageBytes, null);
                        FileUtils.writeByteArrayToFile(imageFile, imageBytes);
                        Logger.v("image saved:" + imageFile);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        return imageHash;
    }

    private Bitmap getScaledBitmap(Bitmap nodeBitmap) {
        int width = nodeBitmap.getWidth();
        int height = nodeBitmap.getHeight();
        Logger.d("ScaledBitmap width: " + width + " height: " + height);

        // for small image, no need to scale
        if (width < 150 && width < 150) {
            Logger.d("bitmap bytes: " + nodeBitmap.getByteCount());
            return nodeBitmap;
        }

        int ratio = mWatchPhoneResolutionRatioSharedPref
                .getInt(WATCH_PHONE_RESOLUTION_RATIO_KEY, 1);
        if (ratio == 1) {
            Logger.v("requestWatchResolution ");
            requestWatchResolution();
        }
        Logger.d("ScaledBitmap ratio " + ratio);

        Logger.d("ScaledBitmap scaled width: " + (width / ratio) + " height: " + (height / ratio));
//        nodeBitmap = Bitmap.createScaledBitmap(nodeBitmap, width / ratio, height / ratio, true);
        nodeBitmap = ThumbnailUtils.extractThumbnail(nodeBitmap, width / ratio, height / ratio);
        return nodeBitmap;
    }

    private void requestWatchResolution() {
        mGmsApi.sendMsg(WATCH_RESOLUTION_REQUEST_PATH, null, null);
//        mGmsWear.sendMessage(WATCH_RESOLUTION_REQUEST_PATH, null);
    }

    private Bitmap requestBitmap(AccessibilityNodeInfo accNode) {
        Bitmap nodeBitmap;
        Bundle bitmapBundle = new Bundle();
        accNode.requestSnapshot(bitmapBundle);
        nodeBitmap = (Bitmap) bitmapBundle.get("bitmap");
        return nodeBitmap;
    }

    private boolean isDataBundleDuplicate(DataBundle dataBundle) {
        if (dataBundle.equals(mLastSentDataBundle)) {
            Logger.v("repeat curr bundle" + dataBundle);
            Logger.v("repeat last bundle" + mLastSentDataBundle);
            return true;
        } else {
            Logger.d(dataBundle.toString());
            return false;
        }
    }

    // prune data to save bandwidth if there are no changing nodes
    private void pruneDataBundle(DataBundle dataBundle) {
        Logger.v("new data new  bundle before pruning: " + dataBundle);
        Logger.v("new data last bundle before pruning: " + mLastSentDataBundle);

        DataBundle savedDataBundleBeforePruning = new DataBundle(dataBundle);
        if (mLastSentDataBundle != null) {
            // prune list nodes
            ArrayList<DataNode[]> listNodes = new ArrayList<>(dataBundle.getListNodes());
            ArrayList<DataNode> allLastListNodes = mLastSentDataBundle.getAllListNodes();
            for (DataNode[] nodes : listNodes) {
                for (DataNode node : nodes) {
                    if (allLastListNodes.contains(node)) {
                        Logger.d("new data bundle removed list node: " + node);
                        // should remove duplicate nodes that were sent last time
                        dataBundle.remove(node);
                    }
                }
            }

            // prune normal nodes
            ArrayList<DataNode> lastSentDataNodes = mLastSentDataBundle.getDataNodes();
            ArrayList<DataNode> dataNodes = new ArrayList<>(dataBundle.getDataNodes());
            if (listNodes.size() == 0) {
                for (DataNode node : dataNodes) {
                    if (lastSentDataNodes.contains(node)) {
                        Logger.d("new data bundle removed node: " + node);
                        dataBundle.remove(node);
                    }
                }
            }
        }

        mLastSentDataBundle = savedDataBundleBeforePruning;
        Logger.v("new data new  bundle after pruning: " + dataBundle);
        Logger.v("new data last bundle after pruning: " + mLastSentDataBundle);
    }

    @Override
    public void onAccessibilityEventForBackground(String s, AccessibilityEvent accessibilityEvent) {

    }

    // transfer apk file, mapping rules to wearable
//    private void sendFileToWearableAsync(final File fileName, final String requestId) {
//        mWorkerThread.postTask(new Runnable() {
//            @Override
//            public void run() {
//                // send to wearable
//                FileTransfer fileTransferHighLevel = new FileTransfer.Builder()
//                        .setFile(fileName).setRequestId(requestId).build();
//                fileTransferHighLevel.startTransfer();
//            }
//        });
//    }

    @Override
    public void onInterrupt() {
        Logger.v("");

    }

    @Override
    protected void onServiceConnected() {
        if (mGmsApi != null) {
            Logger.v("gms connect");
            mGmsApi.connect();
        }
    }

    @Override
    public void onDestroy() {
        Logger.v("");
        stopRunningNotification();
//        mGmsWear.removeWearConsumer(mDataConsumer);
//        mGmsWear.removeCapabilities(PHONE_CAPABILITY);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mBroadcastReceiver);
        if (mGmsApi != null) {
            Logger.v("gms disconnect");
            mGmsApi.disconnect();
        }
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
