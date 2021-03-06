package edu.stonybrook.cs.netsys.uiwearproxy.uiwearService;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ACCESSIBILITY_SETTING_INTENT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.AVAILABLE_NODES_PREFERENCE_SETTING_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.CACHE_DISABLED_CODE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.CACHE_ENABLED_CODE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.CACHE_STATUS_PATH;
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
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PROXY_STARTED;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PROXY_STATUS_PREF;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PURGE_CACHE_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PURGE_CACHE_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.READ_PREFERENCE_NODES_SUCCESS;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.RESET_DIFF_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.RUNNING_APP_PREF_CACHE_SIZE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.TIME_FORMAT;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_HEIGHT_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_RESOLUTION_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_RESOLUTION_PREF_NAME;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_RESOLUTION_REQUEST_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_WIDTH_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.XML_EXT;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CACHE_DISABLED_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CACHE_ENABLED_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CLICK_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.PREFERENCE_DIR;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.getResDir;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.marshall;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.unmarshall;
import static edu.stonybrook.cs.netsys.uiwearlib.helper.AppUtil.getBitmapBytes;
import static edu.stonybrook.cs.netsys.uiwearlib.helper.AppUtil.getImageCacheFolderPath;
import static edu.stonybrook.cs.netsys.uiwearlib.helper.AppUtil.hashBitmap;
import static edu.stonybrook.cs.netsys.uiwearlib.helper.AppUtil.purgeImageCache;
import static edu.stonybrook.cs.netsys.uiwearlib.helper.NodeUtil.getBriefNodeInfo;
import static edu.stonybrook.cs.netsys.uiwearlib.helper.NodeUtil.getNodeIdText;
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
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.LruCache;
import android.util.SparseArray;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

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
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.stonybrook.cs.netsys.uiwearlib.WorkerThread;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.AccNode;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataAction;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataBundle;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataNode;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataPayload;
import edu.stonybrook.cs.netsys.uiwearlib.helper.FileUtil;
import edu.stonybrook.cs.netsys.uiwearlib.helper.NodeUtil;
import edu.stonybrook.cs.netsys.uiwearlib.helper.Shell;
import edu.stonybrook.cs.netsys.uiwearlib.helper.XmlUtil;
import edu.stonybrook.cs.netsys.uiwearproxy.R;

public class PhoneProxyService extends AccessibilityService {

    private GmsApi mGmsApi;
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

    private boolean mIsCacheEnabled;

    // for debouncing
    private long mLastEventTimestamp;

    private long mBeginTime;

    public static long mIdx = 1;

    private SparseArray<String> mImageHashCache = new SparseArray<>();

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
        mWatchPhoneResolutionRatioSharedPref = getSharedPreferences(WATCH_RESOLUTION_PREF_NAME,
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
//                        int phoneSize = Math.max(mPhoneHeight, mPhoneWidth);
//                        int watchSize = Math.min(size.x, size.y);
//                        int ratio = phoneSize / watchSize;
//                        editor.putInt(WATCH_RESOLUTION_KEY, ratio);
                        editor.putInt(WATCH_HEIGHT_KEY, size.y);
                        editor.putInt(WATCH_WIDTH_KEY, size.y);
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

            private void updateDataBundleImage(final DataBundle dataBundle) {
                // process data bundle with image data
                // cannot happen usually, since all image data are immediately send to wear
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<DataNode> nodes = dataBundle.getDataNodes();
                        for (DataNode node : nodes) {
                            if (!processDataNodeImageAndSendToWear(node)) {
                                Logger.w("new no required image on phone: %s ", node);
                                return;
                            }
                        }

                        ArrayList<ArrayList<DataNode>> listNodes = dataBundle.getListNodes();
                        for (ArrayList<DataNode> list : listNodes) {
                            for (DataNode node : list) {
                                if (!processDataNodeImageAndSendToWear(node)) {
                                    Logger.w("new no required list image on phone: %s ", node);
                                    return;
                                }
                            }
                        }
                        byte[] dataBundleBytes = marshall(dataBundle);
                        mGmsApi.sendMsg(DATA_BUNDLE_PATH, dataBundleBytes, null);
                    }

                    private boolean processDataNodeImageAndSendToWear(DataNode node) {
                        String imageHash = node.getImageHash();
                        if (imageHash != null) {
                            byte[] imageBytes = getImageInDisk(imageHash);
                            Logger.d("new send required image hash %s, len: %d ", imageHash,
                                    imageBytes != null ? imageBytes.length : 0);
                            if (imageBytes == null || imageBytes.length <= 0) {
                                return false;
                            }
                            DataPayload dataPayload = new DataPayload(imageHash, imageBytes);
                            Logger.d("bitmap payload update: %s", dataPayload);
                            byte[] bitmapPayload = marshall(dataPayload);

                            mGmsApi.sendMsg(IMAGE_PATH, bitmapPayload, null);
                            return true;
                        } else {
                            return false;
                        }
                    }

                    private byte[] getImageInDisk(String imageHash) {
                        String imagePath = getImageCacheFolderPath();
                        File imageFile = new File(imagePath, imageHash + ".png");
                        try {
                            return FileUtils.readFileToByteArray(imageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });

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

        mThreadPool = new ThreadPoolExecutor(2, 4, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    private void performActionOnPhone(DataAction dataAction) {
        // FIXME: 11/15/16 how to use this?
//        String pkgName = dataAction.getPkgName();

        int actionId = dataAction.getActionId();
        AccessibilityNodeInfo node = mActionNodes.get(actionId);
        if (node == null) {
            Logger.w("perform on null node");
            return;
        }
        Logger.d("action node: " + getBriefNodeInfo(node));
        boolean hasPerformed = performActionUseAccessibility(node);
        if (hasPerformed) {
            Logger.i("perform success use acc");
        } else {
            if (performActionUseAdbShel(node)) {
                Logger.i("perform success use adb tap");
            } else {
                Logger.w("perform failed");
            }
        }
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
        if (mGmsApi != null) {
            Logger.v("gms connect");
            mGmsApi.connect();
        }

        if (intent != null) {
            int startCode = intent.getIntExtra(PREFERENCE_SETTING_KEY, 0);
            if (startCode == PREFERENCE_SETTING_CODE) {
                mIsRunningPreferenceSetting = true;
                Toast.makeText(this, "Preference setting started", Toast.LENGTH_SHORT).show();
                Logger.t("pref").v("start preference setting");
            }

            int stopCode = intent.getIntExtra(PREFERENCE_STOP_KEY, 0);
            if (stopCode == PREFERENCE_STOP_CODE) {
                mIsRunningPreferenceSetting = false;
                mAppNodesMapForPreferenceSetting.clear();
                Toast.makeText(this, "Preference setting exited", Toast.LENGTH_SHORT).show();
                Logger.t("pref").v("stop preference setting");
            }

            boolean purgeCache = intent.getBooleanExtra(PURGE_CACHE_KEY, false);
            if (purgeCache) {
                purgeCache();
                Toast.makeText(this, "Cache Purged", Toast.LENGTH_SHORT).show();
            }

            int cacheEnabled = intent.getIntExtra(CACHE_ENABLED_KEY, 0);
            if (cacheEnabled == CACHE_ENABLED_CODE) {
                mIsCacheEnabled = true;
                Toast.makeText(this, "Cache enabled ", Toast.LENGTH_SHORT).show();
                byte[] cacheStatusBytes = new byte[]{(byte) (1)};
                mGmsApi.sendMsg(CACHE_STATUS_PATH, cacheStatusBytes, null);

            }

            int cacheDisabled = intent.getIntExtra(CACHE_DISABLED_KEY, 0);
            if (cacheDisabled == CACHE_DISABLED_CODE) {
                mIsCacheEnabled = false;
                Toast.makeText(this, "Cache disabled", Toast.LENGTH_SHORT).show();
                byte[] cacheStatusBytes = new byte[]{(byte) (0)};
                mGmsApi.sendMsg(CACHE_STATUS_PATH, cacheStatusBytes, null);
            }

            boolean resetDiff = intent.getBooleanExtra(RESET_DIFF_KEY, false);
            if (resetDiff) {
                Logger.v("reset diff");
                mLastSentDataBundle = null;
                mPairAccessibilityNodeMap.clear();
                mAppPreferenceNodesCache.evictAll();
                mAppRootNodePkgName = null;
                mViewIdCountMap.clear();
                mImageHashCache.clear();
                mActionNodes.clear();
                Toast.makeText(this, "Diff reset", Toast.LENGTH_SHORT).show();
            }
        }

        return START_STICKY;
    }

    private void purgeCache() {
        // notify wear proxy to purge cache
        mGmsApi.sendMsg(PURGE_CACHE_PATH, null, null);
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                mAppPreferenceNodesCache.evictAll();
                purgeImageCache(getApplicationContext());
            }
        });
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        if (mIsLoggingActionBenchmark) {
            Log.d("BENCH", "action click trigger event");
            mIsLoggingActionBenchmark = false;
        }
        final AccessibilityNodeInfo rootNode = getRootInActiveWindow();



        final String bgAppName = "com.spotify.music";

        setAppBackgroundAlive("com.uramaks.music.player");
        setAppBackgroundAlive("com.anghami");
        setAppBackgroundAlive("com.bandlab.bandlab");
        setAppBackgroundAlive("com.northpark.drinkwater");
        setAppBackgroundAlive("com.itunestoppodcastplayer.app");
        setAppBackgroundAlive("com.clearchannel.iheartradio.controller");
        setAppBackgroundAlive("com.contacts1800.ecomapp");
        setAppBackgroundAlive("com.pfizer.us.QuittersCircle");
        setAppBackgroundAlive("com.skimble.workouts");
        setAppBackgroundAlive("com.spotify.music");
        setAppBackgroundAlive("com.bravetheskies.ghostracer");
        setAppBackgroundAlive("com.kiss.countit");
        setAppBackgroundAlive("com.appgeneration.itunerfree");
        setAppBackgroundAlive("biz.mobidev.mdplayer");
        setAppBackgroundAlive("com.google.android.music");
        setAppBackgroundAlive("com.endomondo.android");
        setAppBackgroundAlive("com.musicplayer.player.mp3player.white");
        setAppBackgroundAlive("com.aplicaciones.listacompra");
        setAppBackgroundAlive("com.codium.hydrocoach");
        setAppBackgroundAlive("com.SearingMedia.Parrot");

        // TODO: Add bgAppName into the dict for filtering check

        final String eventPkgName =
                event.getPackageName() == null ? "" : event.getPackageName().toString();
        Log.i("event", "event package name is " + eventPkgName);
        final AccessibilityNodeInfo sourceNode = event.getSource();
        Log.d("event", "event : " + event);
        Logger.t("event").v("root node: " + NodeUtil.getBriefNodeInfo(rootNode));
        Logger.t("event").v("source node: " + NodeUtil.getBriefNodeInfo(sourceNode));
        if (sourceNode == null) {
            return;
        }

        if (eventPkgName.equals(bgAppName)) {
            Log.d("event", "This event is the background app");

        } else if (rootNode != null && rootNode.getPackageName() != null
                && rootNode.getPackageName().toString().equals(bgAppName)) {
            Log.d("event", "This rootNode belongs to the background app");

        } else if (!NodeUtil.isAppRootNode(this, rootNode)) {
            // skip non app node
            Log.d("event", "Skip the event as it is neither the background nor the app");
            return;
        }

        /********** Preference Setting Functionality **********/
        if (mIsRunningPreferenceSetting) {
            Logger.t("pref").v("app node: " + NodeUtil.getBriefNodeInfo(rootNode));
            mAppRootNodePkgName = rootNode.getPackageName().toString();
            mAppNodesMapForPreferenceSetting.clear();
            AccNode rootAccNode = new AccNode(rootNode);
            parseNodesForPreferenceSetting(rootAccNode);
            // when setting preference, won't extract sub view tree content
            return;
        }

        /********** Extracting Sub View Tree Based on App Preference  *********/
        final String appPkgName = getNodePkgName(rootNode);
        // register app for background processing

        if (bgAppName.equals(sourceNode.getPackageName())) {
            Log.d("event", "Proceeding as background app");
        } else if (!appPkgName.equals(sourceNode.getPackageName())) {
            // even root node is app, if accessibility event is from non app node, then skip
            return;
        }

        boolean isAppEnabled = mEnabledAppsSharedPref.getBoolean(appPkgName, false);
        if (bgAppName.equals(sourceNode.getPackageName())) {

        } else if (!isAppEnabled) {
            Log.d("event", "This app: " + appPkgName + " is not enabled in Preference");
            return;
        }

        //File preferenceFolder = new File(getResDir(PREFERENCE_DIR, appPkgName));
        File preferenceFolder = new File(getResDir(PREFERENCE_DIR, eventPkgName));
        if (bgAppName.equals(sourceNode.getPackageName())) {

        } else if (!preferenceFolder.exists()) {
            Log.d("event", "%s pref not exists!" + preferenceFolder.getPath());
            return;
        }

//        File mappingRuleFolder = new File(getResDir(MAPPING_RULE_DIR, appPkgName));
//        if (!mappingRuleFolder.exists()) {
//            Logger.t("mapping").v("%s mapping rule not exists!", mappingRuleFolder.getPath());
//            return;
//        }
        // TODO: 3/1/17Wednesday  need to remove unrelated events, use better mechanism

        // debounce
        long currentTimestamp = SystemClock.uptimeMillis();
        if (currentTimestamp - mLastEventTimestamp < 200) {
            Log.w("event", "acc event too fast, skip ");
            return;
        }
        mLastEventTimestamp = currentTimestamp;

        AccessibilityNodeInfo parentSource = sourceNode;
        while (parentSource.getParent() != null) {
            parentSource = parentSource.getParent();
        }
        final AccessibilityNodeInfo rootSource = parentSource;
        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
                Log.i("STATS", event.toString() + sourceNode.toString());
                mBeginTime = SystemClock.currentThreadTimeMillis();
                mViewIdCountMap.clear();
                mPairAccessibilityNodeMap.clear();
//                NodeUtil.printNodeTree(rootNode);
                Log.i("BENCH", "parseAppNodes begin:");
                //parseAppNodes(rootNode);
                parseAppNodes(rootSource);
                Log.i("BENCH", "parseAppNodes end:");
            }
        });

        // read app preference xml file and extract view tree content if nodes ready
        readAppPreferenceNodesAsync(preferenceFolder, new AppNodesReadyCallback() {
            @Override
            public void onAppNodesReady(String preferenceId, HashSet<AccNode> nodes) {
                // decide whether the preference mNodes are subset of current app mNodes
                Logger.v("pref id: " + preferenceId);
                Logger.v("pref nodes: " + nodes);

                Log.i("BENCH", "    parseNodeData begin:");
                // begin extracting preference view tree info
                DataBundle dataBundle = new DataBundle(eventPkgName, preferenceId);
                parseNodeData(nodes, dataBundle);
                Log.i("BENCH", "    parseNodeData end:");
                if (isDataBundleDuplicate(dataBundle)) {
                    // no need to further processing
                    return;
                } else {
                    pruneDataBundle(dataBundle);
                }
                Log.i("BENCH", "    marshallNodeData begin: " + dataBundle);
                sendDataBundleToWear(dataBundle);
            }

            private void sendDataBundleToWear(DataBundle dataBundle) {
                byte[] data = marshall(dataBundle);
                Logger.i("new data bundle: " + dataBundle);
                Log.i("BENCH", "    marshallNodeData end: " + dataBundle);
                long duration = SystemClock.currentThreadTimeMillis() - mBeginTime;
                Log.i("MICRO", "phone local parse time: " + duration);
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

                    String cacheKey = new File(
                            new File(preferenceFile.getParent()).getParent()).getName()
                            + File.separator
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

                    // TODO: 12/5/16 here, parse mapping rule to decide if text or image is needed
                    HashSet<AccNode> nodes = new HashSet<>(prefNodesFromFile);
                    if (!appNodesContainPreferenceNodes(nodes)) {
                        //root node from non preference screen, so skip
                        Log.d("pref", "not contain preference nodes, skip");
                        continue;
                    }
                    Log.d("pref", "from contain preference nodes: " + nodes);

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
                    if (prefNode.getChildCount() > 0) {
                        oneNodeMatched = prefNode.matches(appNode);
                        if (oneNodeMatched) {
                            atLeastOneNodeMatched = true;
                            Log.d("event",
                                    "node match: multiple app- " + appNode + " pref-" + prefNode);
                            // need to update the prefNode to appNode
                            updatePreferenceNode(preferenceNodes, prefNode, appNode);
                            // do not break here, need iterate all nodes that have the same viewID
                        }
                    } else {
                        // single node
                        oneNodeMatched = prefNode.matches(appNode);
                        if (oneNodeMatched) {
                            Log.d("event", "node match: multiple nodes single meet app- " + appNode
                                    + " pref- " + prefNode);
                            // need to update the prefNode to appNode
                            updatePreferenceNode(preferenceNodes, prefNode, appNode);
                            break;
                        }
                    }
                } else {
                    //no two nodes have the same id
                    // detect viewId mapping N(phone) to 1(wear) case
                    List<String> prefViewIdList = Arrays.asList(prefNode.getViewId()
                            .split(" \\| "));
                    // TODO: 12/6/16 need to support & case e.g. endomondo app, two buttons overlap
                    oneNodeMatched = prefViewIdList.indexOf(appNode.getViewId()) != -1;
                    Log.d("event", "node id list: " + prefViewIdList + " app node: "
                            + appNode.getViewId() + " index: " + prefViewIdList.indexOf(
                            appNode.getViewId()));
                    if (oneNodeMatched) {
                        Log.d("event", "node match: single app- " + appNode + " pref- " + prefNode);
                        // need to update the prefNode to appNode
                        updatePreferenceNode(preferenceNodes, prefNode, appNode);
                        break;
                    }
                }
            }

            if (!oneNodeMatched && !atLeastOneNodeMatched) {
                // find one node that does not in appNodes
                Log.w("event", "node not match");
                return false;
            }
        }

        Log.d("event", "node matched");
        Log.v("event", "node matched app set: " + appNodes.toString());
        Log.v("event", "node matched pref set: " + preferenceNodes.toString());
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
        Log.i("BENCH", "        parseNodeData normal begin: " + dataBundle);
        ArrayList<AccNode> listNodes = new ArrayList<>();
        for (AccNode accNode : accNodes) {
            Log.i("event", "accNode : " + accNode);
            if (accNode.getChildCount() > 0) {
                // this is a list item preference node
                listNodes.add(accNode);
            } else {
                Log.i("BENCH", "            normal single node begin: " + getNodeIdText(accNode));
                // normal single node item
                AccessibilityNodeInfo nodeInfo = mPairAccessibilityNodeMap.get(accNode);
                if (nodeInfo == null) {
                    Logger.w("accNode norm child null for accNode: " + accNode);
                    return;
                }
                nodeInfo.refresh();
                Logger.d("accNode norm child: " + getBriefNodeInfo(nodeInfo));
                DataNode dataNode = getDataNode(nodeInfo);
                dataNode.setViewId(accNode.getViewId());

                dataBundle.add(dataNode);
                Log.i("BENCH", "            normal single node end: " + getNodeIdText(accNode));

            }
        }
        Log.i("BENCH", "        parseNodeData normal end: " + dataBundle);
        Log.i("BENCH", "        parseNodeData list begin: " + dataBundle);
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
            ArrayList<DataNode> nodes = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                AccNode node = accNode.getChild(i);
                // get AccessibilityNodeInfo based on node
                AccessibilityNodeInfo nodeInfo = mPairAccessibilityNodeMap.get(node);
                if (nodeInfo != null) {
                    Logger.d("accNode list child: " + getBriefNodeInfo(nodeInfo));
                    nodes.add(getDataNode(nodeInfo));
                } else {
                    Logger.w("accNode list child null");
                }
            }
            dataBundle.add(nodes);
        }
        Log.i("BENCH", "        parseNodeData list end:" + dataBundle);
    }

    @NonNull
    private DataNode getDataNode(@NonNull AccessibilityNodeInfo nodeInfo) {
        // save node for future perform action
        mActionNodes.put(nodeInfo.hashCode(), nodeInfo);

        DataNode dataNode = new DataNode(nodeInfo);
        Log.i("BENCH", "                getNodeBitmapHash begin: " + getNodeIdText(nodeInfo));
        String bitmapHash = getNodeBitmapHash(nodeInfo);
        Log.i("BENCH", "                getNodeBitmapHash end: " + getNodeIdText(nodeInfo));
        dataNode.setImageHash(bitmapHash);
        Logger.i(dataNode.toString());
        return dataNode;
    }

    private String getNodeBitmapHash(final AccessibilityNodeInfo accNode) {
        // FIXME: 11/12/16 based on mapping rule, not all mNodes need image/bitmap
        if ("android.widget.TextView".equals(accNode.getClassName())) {
            Logger.v("text view no need to extract bitmap");
            return null;
        }

        final Bitmap nodeBitmap = requestBitmap(accNode);
        if (nodeBitmap == null) {
            Logger.w("cannot get bitmap");
            return null;
        }

        // FIXME: 2/23/17Thursday use other ways to calculate hashcode of bitmap
        final String bitmapHash = hashBitmap(nodeBitmap);
        Logger.d("bitmapHash: %s, node: %s", bitmapHash, getNodeIdText(accNode));
        // for new image data, phone proxy will send image bytes in addition to bitmap hash.
        // wear proxy also maintain the same key-value cache, i.e., bitmap hash as key and
        // image bytes as value.
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                processNodeBitmap(bitmapHash, accNode, nodeBitmap);
            }
        });

        return bitmapHash;
    }

    private void processNodeBitmap(String bitmapHash, AccessibilityNodeInfo accNode,
            Bitmap nodeBitmap) {
        try {
            File imageFile = new File(getImageCacheFolderPath(), bitmapHash + ".png");
            if (imageFile.exists() && mIsCacheEnabled) {
                return;
            }

            final byte[] imageBytes = getBytesFromNodeBitmap(accNode, nodeBitmap);

            DataPayload dataPayload = new DataPayload(bitmapHash, imageBytes);
            Logger.d("bitmap payload first time: %s", dataPayload);

            byte[] bitmapPayload = marshall(dataPayload);
            mGmsApi.sendMsg(IMAGE_PATH, bitmapPayload, null);
//            if (!mIsCacheEnabled) {
//                return;
//            }

            // save to local disk cache repo
            FileUtils.writeByteArrayToFile(imageFile, imageBytes);
            Logger.v("image saved: " + imageFile);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private byte[] getBytesFromNodeBitmap(AccessibilityNodeInfo accNode, Bitmap nodeBitmap) {
        Log.i("BENCH", "                    getScaledBitmap begin: " + getNodeIdText(accNode));
        Bitmap scaledBitmap = getScaledBitmap(nodeBitmap);
        Log.i("BENCH", "                    getScaledBitmap end: " + getNodeIdText(accNode));

        Log.i("BENCH", "                    getBitmapBytes begin: " + getNodeIdText(accNode));
        final byte[] imageBytes = getBitmapBytes(scaledBitmap);
        Log.i("BENCH", "                    getBitmapBytes end: " + getNodeIdText(accNode));

        if (imageBytes != null) {
            Logger.i("image bytes: " + imageBytes.length);
        } else {
            Logger.w("image bytes null");
        }
        return imageBytes;
    }

    private Bitmap getScaledBitmap(Bitmap nodeBitmap) {
        int width = nodeBitmap.getWidth();
        int height = nodeBitmap.getHeight();
        Logger.d("ScaledBitmap: " + nodeBitmap.getByteCount()
                + " bytes, width: " + width + " height: " + height);
        int watchWidth = mWatchPhoneResolutionRatioSharedPref.getInt(WATCH_WIDTH_KEY, 0);
        int watchHeight = mWatchPhoneResolutionRatioSharedPref.getInt(WATCH_HEIGHT_KEY, 0);
        if (watchWidth == 0 || watchHeight == 0) {
            Logger.v("requestWatchResolution ");
            requestWatchResolution();
        } else {
            if (watchHeight < height && watchWidth < width) {
//         nodeBitmap = Bitmap.createScaledBitmap(nodeBitmap, width / ratio, height / ratio, true);
                nodeBitmap = ThumbnailUtils.extractThumbnail(nodeBitmap, watchWidth, watchHeight);
            }

            // for small image, no need to scale
            if (nodeBitmap.getByteCount() > 80 * 1024) {
                nodeBitmap = ThumbnailUtils.extractThumbnail(nodeBitmap, watchWidth, watchHeight);
            }
        }
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
        Log.i("debugging", "storing bitmap");
        //FileUtil.storeBitmap(nodeBitmap, "bitmaps", String.valueOf(mIdx));
        mIdx++;
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
            ArrayList<ArrayList<DataNode>> listNodes = new ArrayList<>(dataBundle.getListNodes());
            ArrayList<ArrayList<DataNode>> allLastListNodes = mLastSentDataBundle.getListNodes();
            for (ArrayList<DataNode> nodes : listNodes) {
                if (allLastListNodes.contains(nodes)) {
                    Logger.d("new data bundle should not removed list node: " + nodes);
                    // should remove duplicate nodes that were sent last time
//                    dataBundle.remove(nodes);
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
        SharedPreferences settings = getSharedPreferences(PROXY_STATUS_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PROXY_STARTED, true);
        editor.commit();
        Logger.v("onServiceConnected");

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.v("onUnbind");
        stopRunningNotification();
//        mGmsWear.removeWearConsumer(mDataConsumer);
//        mGmsWear.removeCapabilities(PHONE_CAPABILITY);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .unregisterReceiver(mBroadcastReceiver);
        if (mGmsApi != null) {
            Logger.v("gms disconnect");
            mGmsApi.disconnect();
        }
        SharedPreferences settings = getSharedPreferences(PROXY_STATUS_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PROXY_STARTED, false);
        editor.commit();
        stopSelf(START_NOT_STICKY);
        return super.onUnbind(intent);
    }

    @Override
    public void onLowMemory() {
        Logger.v("onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        Logger.v("onDestroy");
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
