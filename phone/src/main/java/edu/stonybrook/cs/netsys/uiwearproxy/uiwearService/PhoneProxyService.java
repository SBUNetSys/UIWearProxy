package edu.stonybrook.cs.netsys.uiwearproxy.uiwearService;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ACCESSIBILITY_SETTING_INTENT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.AVAILABLE_NODES_PREFERENCE_SETTING_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.BITMAP_CACHE_SIZE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATABUNDLE_CACHE_SIZE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_HASH_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_REQUIRED_PATH;
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
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_PHONE_RESOLUTION_RATIO_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_PHONE_RESOLUTION_RATIO_PREF_NAME;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_RESOLUTION_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.XML_EXT;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CLICK_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.BITMAP_DIR;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.MAPPING_RULE_DIR;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.PREFERENCE_DIR;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.getResDir;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.marshall;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.unmarshall;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.text.format.DateFormat;
import android.util.LruCache;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cscao.libs.gmswear.GmsWear;
import com.cscao.libs.gmswear.connectivity.FileTransfer;
import com.cscao.libs.gmswear.consumer.AbstractDataConsumer;
import com.cscao.libs.gmswear.consumer.DataConsumer;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageEvent;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.stonybrook.cs.netsys.uiwearlib.WorkerThread;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataAction;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataBundle;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataNode;
import edu.stonybrook.cs.netsys.uiwearlib.helper.AppUtil;
import edu.stonybrook.cs.netsys.uiwearlib.helper.FileUtil;
import edu.stonybrook.cs.netsys.uiwearlib.helper.NodeUtil;
import edu.stonybrook.cs.netsys.uiwearlib.helper.Shell;
import edu.stonybrook.cs.netsys.uiwearlib.helper.XmlUtil;
import edu.stonybrook.cs.netsys.uiwearproxy.R;

public class PhoneProxyService extends AccessibilityService {

    private NotificationManager mNotificationManager;

    // for preference setting, only need to parse once
    private boolean mIsRunningPreferenceSetting;

    // either region or id alone is not enough for detecting the specific UI elements
    // so combined them and can cover most cases
    // must use region as key, since id can be the same
    private HashMap<Rect, String> mAppLeafNodesMapForPreferenceSetting = new HashMap<>();
    private String mAppRootNodePkgName;

    // for bitmap extracting and other heavy work
    // TODO: 11/13/16 refactor this use RxJava or EventBus
    private WorkerThread mWorkerThread;

    // main thread handler
    private Handler mMainThreadHandler;

    private SharedPreferences mEnabledAppsSharedPref;
    private SharedPreferences mWatchPhoneResolutionRatioSharedPref;

    // list of app preference mNodes, for each pair, the first is id, the second is rect
    private LruCache<String, ArrayList<Pair<String, Rect>>> mAppPreferenceNodesCache =
            new LruCache<>(RUNNING_APP_PREF_CACHE_SIZE);

    private HashSet<Pair<String, Rect>> mAppNodes = new HashSet<>();
    private HashMap<Pair<String, Rect>, AccessibilityNodeInfo> mPairAccessibilityNodeMap =
            new HashMap<>();
    private LruCache<Integer, Bitmap> mBitmapLruCache = new LruCache<Integer, Bitmap>(
            BITMAP_CACHE_SIZE) {
        @Override
        protected int sizeOf(Integer key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than number of items.
            return bitmap.getByteCount();
        }
    };

    // to avoid same data retransmission due to duplicate accessibility events
    private DataBundle mLastSentDataBundle;
    private LruCache<String, byte[]> mDataBundleLruCache = new LruCache<String, byte[]>(
            DATABUNDLE_CACHE_SIZE) {
        @Override
        protected int sizeOf(String key, byte[] dataBundle) {
            return dataBundle.length;
        }
    };

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

    private GmsWear mGmsWear;
    private DataConsumer mDataConsumer;

    private int mPhoneWidth;
    private int mPhoneHeight;

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
                        Logger.t("pref").v(msg.obj + "preference saved");
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

        mGmsWear = GmsWear.getInstance();
        mDataConsumer = new AbstractDataConsumer() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent) {
                switch (messageEvent.getPath()) {
                    case CLICK_PATH:
                        Logger.i("CLICK_PATH");
                        byte[] actionData = messageEvent.getData();
                        DataAction dataAction = unmarshall(actionData, DataAction.CREATOR);
                        Logger.d(dataAction);
                        performActionOnPhone(dataAction);
                        break;
                    case WATCH_RESOLUTION_PATH:
                        Logger.i("WATCH_RESOLUTION_PATH");
                        byte[] watchResolution = messageEvent.getData();
                        Point size = unmarshall(watchResolution, Point.CREATOR);
                        SharedPreferences.Editor editor =
                                mWatchPhoneResolutionRatioSharedPref.edit();
                        int phoneSize = Math.max(mPhoneHeight, mPhoneWidth);
                        int watchSize = Math.min(size.x, size.y);
                        int ratio = phoneSize / watchSize;
                        editor.putInt(WATCH_PHONE_RESOLUTION_RATIO_KEY, ratio);
                        editor.apply();
                        break;
                    case DATA_BUNDLE_REQUIRED_PATH:
                        Logger.i("DATA_BUNDLE_REQUIRED_PATH");
                        byte[] hashStringBytes = messageEvent.getData();
                        String hashString = new String(hashStringBytes);
                        sendRealDataBundleAsync(hashString);
                        break;
                    default:
                        Logger.w("unknown msg");
                }
            }

            @Override
            public void onDataChanged(DataEvent event) {
                Logger.i("onDataChanged");
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    // DataItem changed
                    DataItem item = event.getDataItem();
                    Logger.i(item.getUri().getPath());
                    if (item.getUri().getPath().equals(DATA_BUNDLE_PATH)) {
                        Logger.i("DATA_BUNDLE_PATH");
                    }
                }
            }
        };

        Point size = new Point();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getSize(size);
        mPhoneWidth = size.x;
        mPhoneHeight = size.y;
    }

    private void sendRealDataBundleAsync(final String hashString) {
        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
                byte[] bundleBytes = mDataBundleLruCache.get(hashString);
                mGmsWear.syncAsset(DATA_BUNDLE_PATH, DATA_BUNDLE_KEY, bundleBytes, true);
            }
        });
    }

    private void performActionOnPhone(DataAction dataAction) {
        // FIXME: 11/15/16 how to use this?
        String pkgName = dataAction.getPkgName();

        int actionId = dataAction.getActionId();
        AccessibilityNodeInfo node = findNodeOnCurrentWindowById(actionId);
        boolean hasPerformed = performActionUseAccessibility(node);
        if (hasPerformed) {
            Logger.i("performed success use acc");
        } else {
            if (performActionUseAdbShel(node)) {
                Logger.i("performed success use adb");
            } else {
                Logger.w("performed failed");
            }
        }
    }

    private AccessibilityNodeInfo findNodeOnCurrentWindowById(int id) {
        AccessibilityNodeInfo root = getRootInActiveWindow();
        return findNodeById(root, id);
    }

    private AccessibilityNodeInfo findNodeById(AccessibilityNodeInfo root, int id) {
        AccessibilityNodeInfo node = null;
        if (root == null) {
            return null;
        }

        if (root.hashCode() == id) {
            return root;
        }

        int count = root.getChildCount();
        for (int i = 0; i < count; i++) {
            if (node == null) {
                node = findNodeById(root.getChild(i), id);
            } else {
                return node;
            }
        }

        return node;
    }

    private boolean performActionUseAdbShel(AccessibilityNodeInfo node) {
        if (node != null) {
            Rect rect = new Rect();
            node.getBoundsInScreen(rect);
            Logger.i("perform adb: " + getBriefNodeInfo(node));
            String cmd = "input tap " + rect.centerX() + " " + rect.centerY();
            Logger.d("cmd is: " + cmd);
            if (Shell.isSuAvailable()) {
                return Shell.runCommand(cmd);
            }
        }
        return false;
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
                mAppLeafNodesMapForPreferenceSetting.clear();
                Logger.t("pref").v("stop preference setting");
            }

        }
        mGmsWear.addWearConsumer(mDataConsumer);
        // reset all cache here
        resetAllCacheHere();

        return START_STICKY;
    }

    private void resetAllCacheHere() {
        Logger.v("reset cache");
        mBitmapLruCache.evictAll();
        mAppPreferenceNodesCache.evictAll();
        mLastSentDataBundle = null;
        mAppRootNodePkgName = null;
        mAppLeafNodesMapForPreferenceSetting.clear();
        mPairAccessibilityNodeMap.clear();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

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
            mAppLeafNodesMapForPreferenceSetting.clear();
            parseLeafNodesForPreferenceSetting(rootNode);

            // TODO: 11/5/16 extract preference related sub view tree here for app building

            // when setting preference, won't extract sub view tree content
            return;
        }

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
        final String appPkgName = getNodePkgName(rootNode);

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
                mAppNodes.clear();
                mPairAccessibilityNodeMap.clear();
                NodeUtil.printNodeTree(rootNode);
                parseAppNodes(rootNode);
            }
        });

        // read app preference xml file
        readAppPreferenceNodesAsync(preferenceFolder, new AppNodesReadyCallback() {
            @Override
            public void onAppNodesReady(String preferenceId, ArrayList<Pair<String, Rect>> nodes) {
                // decide whether the preference mNodes are subset of current app mNodes
                Logger.v("pref id: " + preferenceId);
                if (!appNodesContainPreferenceNodes(nodes)) {
//                     root node from non preference screen, so skip
                    Logger.v("not contain preference nodes, skip");
                    return;
                }
                // begin extracting preference view tree info
                DataBundle dataBundle = new DataBundle(appPkgName, preferenceId);
                parseNodeData(nodes, dataBundle);

                if (dataBundle.equals(mLastSentDataBundle)) {
                    Logger.v("repeat curr bundle" + dataBundle);
                    Logger.v("repeat last bundle" + mLastSentDataBundle);
                    return;
                } else {
                    Logger.v("new data new  bundle before pruning: " + dataBundle);
                    Logger.v("new data last bundle before pruning: " + mLastSentDataBundle);

                    DataBundle savedDataBundleBeforePruning = new DataBundle(dataBundle);
                    if (mLastSentDataBundle != null) {
                        ArrayList<DataNode> lastSentDataNodes = mLastSentDataBundle.getDataNodes();
                        ArrayList<DataNode> dataNodes = new ArrayList<>(dataBundle.getDataNodes());
                        for (DataNode node : dataNodes) {
                            if (lastSentDataNodes.contains(node)) {
                                Logger.d("new data bundle removed node: " + node);
                                dataBundle.remove(node);
                            }
                        }
                    }

                    mLastSentDataBundle = savedDataBundleBeforePruning;
                    Logger.v("new data new  bundle after pruning: " + dataBundle);
                    Logger.v("new data last bundle after pruning: " + mLastSentDataBundle);
                }

                Logger.d(dataBundle.toString());

                // send hash of data bundle first, if wear has the data bundle, done
                // if not, send the real data bundle
                int dataBundleHash = dataBundle.hashCode();
                String dataBundleHashString = Integer.toString(dataBundleHash);
                mGmsWear.sendMessage(DATA_BUNDLE_HASH_PATH, dataBundleHashString.getBytes());
                byte[] data = marshall(dataBundle);
                Logger.i("new data bundle: " + data.length);
                mDataBundleLruCache.put(dataBundleHashString, data);
            }
        });

    }

    private boolean appNodesContainPreferenceNodes(ArrayList<Pair<String, Rect>> preferenceNodes) {
        /** for strict matching of both id and rect ***/
        HashSet<Pair<String, Rect>> preferenceSet = new HashSet<>(preferenceNodes);
        Logger.v("node set: " + mAppNodes.toString());
        Logger.v("pref set: " + preferenceNodes.toString());
        return mAppNodes.containsAll(preferenceSet);

        /*** only compare viewId, (change pair.first to pair.second to only compare rect) ***/
//        ArrayList<String> preferenceNodeIdList = new ArrayList<>();
//        ArrayList<String> appNodeIdList = new ArrayList<>();
//
//        ArrayList<Pair<String, Rect>> appNodes = new ArrayList<>(mAppNodes);
//        for (Pair<String, Rect> pair : appNodes) {
//            appNodeIdList.add(pair.first);
//        }
//
//        for (Pair<String, Rect> pair : preferenceNodes) {
//            preferenceNodeIdList.add(pair.first);
//        }
//
//        return appNodeIdList.containsAll(preferenceNodeIdList);
    }

    private void parseNodeData(ArrayList<Pair<String, Rect>> nodes, DataBundle dataBundle) {
        // currently only use id to extract preference nodes info
        for (Pair<String, Rect> pair : nodes) {
            Logger.i("id: " + pair.first + " rect: " + pair.second);
            AccessibilityNodeInfo accNode = mPairAccessibilityNodeMap.get(pair);
            DataNode dataNode = new DataNode(accNode);
            int uniqueId = dataNode.getUniqueId();
            Logger.v("unique id: " + uniqueId);
            Bitmap nodeBitmap = getNodeBitmap(accNode, dataNode);
            dataNode.setImage(nodeBitmap);
            Logger.i(dataNode.toString());
            dataBundle.add(dataNode);
        }
    }

    private Bitmap getNodeBitmap(AccessibilityNodeInfo accNode, DataNode dataNode) {
        // FIXME: 11/12/16 based on mapping rule, not all mNodes need image/bitmap
        if ("android.widget.TextView".equals(accNode.getClassName())) {
            Logger.v("text view");
            return null;
        }

        Bitmap nodeBitmap = mBitmapLruCache.get(dataNode.getUniqueId());
        if (nodeBitmap != null) {
            Logger.v("bitmap from cache: " + nodeBitmap.getByteCount() + " bytes");
            return nodeBitmap;
        }

        nodeBitmap = requestBitmap(accNode);
        if (nodeBitmap == null) {
            Logger.w("cannot get bitmap");
            return null;
        }

        String bitmapPath = getResDir(BITMAP_DIR, accNode.getPackageName().toString());
        AppUtil.storeBitmapAsync(nodeBitmap, bitmapPath, dataNode.getFriendlyName(nodeBitmap));

        nodeBitmap = getScaledBitmap(nodeBitmap);
        mBitmapLruCache.put(dataNode.getUniqueId(), nodeBitmap);

        return nodeBitmap;
    }

    private Bitmap getScaledBitmap(Bitmap nodeBitmap) {
        int width = nodeBitmap.getWidth();
        int height = nodeBitmap.getHeight();
        Logger.d("ScaledBitmap width: " + width + " height: " + height);

        int ratio = mWatchPhoneResolutionRatioSharedPref
                .getInt(WATCH_PHONE_RESOLUTION_RATIO_KEY, 1);
        Logger.d("ScaledBitmap ratio " + ratio);

        Logger.d("ScaledBitmap scaled width: " + (width / ratio) + " height: " + (height / ratio));
//        nodeBitmap = Bitmap.createScaledBitmap(nodeBitmap, width / ratio, height / ratio, true);
        nodeBitmap = ThumbnailUtils.extractThumbnail(nodeBitmap, width / ratio, height / ratio);
        return nodeBitmap;
    }

    private Bitmap requestBitmap(AccessibilityNodeInfo accNode) {
        Bitmap nodeBitmap;
        Bundle bitmapBundle = new Bundle();
        accNode.requestSnapshot(bitmapBundle);
        nodeBitmap = (Bitmap) bitmapBundle.get("bitmap");
        // FIXME: 11/13/16 compress and scale bitmap before sending to wear
        return nodeBitmap;
    }

    @Override
    public void onAccessibilityEventForBackground(String s, AccessibilityEvent accessibilityEvent) {

    }

    // transfer apk file, mapping rules to wearable
    private void sendFileToWearableAsync(final File fileName, final String requestId) {
        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
                // send to wearable
                FileTransfer fileTransferHighLevel = new FileTransfer.Builder()
                        .setFile(fileName).setRequestId(requestId).build();
                fileTransferHighLevel.startTransfer();
            }
        });
    }

    private void parseAppNodes(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            Logger.v("null app root node ");
            return;
        }

        String viewId = rootNode.getViewIdResourceName();
        Rect rect = new Rect();
        rootNode.getBoundsInScreen(rect);
        Pair<String, Rect> pair = new Pair<>(viewId, rect);
        // for preference comparison
        mAppNodes.add(pair);
        // for finding the node based on pair
        mPairAccessibilityNodeMap.put(pair, rootNode);


        int count = rootNode.getChildCount();

        for (int i = 0; i < count; i++) {
            parseAppNodes(rootNode.getChild(i));
        }

    }

    // parse all UI leaf mNodes and save them to list mAppLeafNodesMapForPreferenceSetting
    private void parseLeafNodesForPreferenceSetting(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            Logger.t("pref").v("null root node ");
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
                    Logger.t("pref").v("add: " + NodeUtil.getBriefNodeInfo(rootNode));
                    mAppLeafNodesMapForPreferenceSetting.put(region, id);
                }
            }
            rootNode.recycle();
        } else {
            for (int i = 0; i < count; i++) {
                parseLeafNodesForPreferenceSetting(rootNode.getChild(i));
            }
        }
    }

    // send prepared leaf mNodes to PreferenceSettingActivity
    private void sendLeafNodesToPreferenceSetting() {
        if (mAppLeafNodesMapForPreferenceSetting.size() > 0) {
            Intent nodesIntent = new Intent(NODES_AVAILABLE);
            ArrayList<Rect> nodes = new ArrayList<>(mAppLeafNodesMapForPreferenceSetting.keySet());
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
        // ensure that mAppLeafNodesMapForPreferenceSetting is not cleared
        final HashMap<Rect, String> savedMap = new HashMap<>(mAppLeafNodesMapForPreferenceSetting);
        final String appPkgName = mAppRootNodePkgName;

        mWorkerThread.postTask(new Runnable() {
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
//        Logger.v();("mapped mNodes: " + mAppLeafNodesMapForPreferenceSetting.keySet().toString());
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
                    Logger.t("pref").v("path: " + preferenceFile.getPath() + " name: "
                            + preferenceFile.getName());

                    String cacheKey = FileUtil.getParentName(preferenceFile) + File.separator
                            + FileUtil.getBaseName(preferenceFile);
                    Logger.t("pref").v("cache key: " + cacheKey);

                    ArrayList<Pair<String, Rect>> nodes = mAppPreferenceNodesCache.get(cacheKey);

                    if (nodes == null) {
                        nodes = XmlUtil.deserializeAppPreference(
                                preferenceFile);
                        mAppPreferenceNodesCache.put(cacheKey, nodes);
                        Logger.t("pref").v("from file");
                    } else {
                        Logger.t("pref").v("from cache");
                    }

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
