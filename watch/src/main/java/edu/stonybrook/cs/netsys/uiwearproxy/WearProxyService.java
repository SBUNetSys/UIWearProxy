package edu.stonybrook.cs.netsys.uiwearproxy;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_REQUIRED_IMAGE_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.IMAGE_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.MSG_CAPABILITY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_RESOLUTION_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_RESOLUTION_REQUEST_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CLICK_ID_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CLICK_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.INTENT_PREFIX;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.INTENT_SUFFIX;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.PKG_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.marshall;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.unmarshall;
import static edu.stonybrook.cs.netsys.uiwearlib.helper.AppUtil.getImageCacheFolderPath;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.cscao.libs.gmsapi.GmsApi;
import com.google.android.gms.wearable.DataMap;
import com.orhanobut.logger.Logger;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataAction;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataBundle;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataNode;

//import com.cscao.libs.gmswear.GmsWear;

/**
 * UIWear wear side proxy service, handling sub view tree info received from phone
 */
public class WearProxyService extends Service {

    private GmsApi mGmsApi;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CLICK_PATH:
                    String pkgName = intent.getStringExtra(PKG_KEY);
                    Logger.d("action pkgName: " + pkgName);
                    int clickId = intent.getIntExtra(CLICK_ID_KEY, 0);
                    byte[] clickData = marshall(new DataAction(pkgName, clickId));
                    Log.d("BENCH", "action click from wear proxy before sending: " + clickId);
                    Logger.d("action data: " + clickData.length);
//                    mGmsWear.sendMessage(CLICK_PATH, clickData);
                    mGmsApi.sendMsg(CLICK_PATH, clickData, null);
                    break;
                default:
            }
        }
    };

    private ThreadPoolExecutor mThreadPool;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Logger.v("");
        return null;
    }

    @Override
    public void onCreate() {
        Logger.i("create");
        mGmsApi = new GmsApi(this, MSG_CAPABILITY);
        mGmsApi.setOnMessageReceivedListener(new GmsApi.OnMessageReceivedListener() {
            @Override
            public void onMessageReceived(GmsApi.MessageData messageEvent) {
                switch (messageEvent.getPath()) {
                    case IMAGE_PATH:
                        byte[] imageBytes = messageEvent.getData();
                        storeImageToDiskCache(imageBytes);
                        break;
                    case WATCH_RESOLUTION_REQUEST_PATH:
                        sendResolutionToPhone();
                        break;
                    case DATA_BUNDLE_PATH:
                        parseDataBundleAsync(messageEvent.getData());
                        break;
                    default:
                        Logger.w("unknown msg");
                }
            }
        });
        mGmsApi.setOnDataChangedListener(new GmsApi.OnDataChangedListener() {
            @Override
            public void onDataChanged(DataMap dataMap) {
//                Asset asset = dataMap.getAsset(DATA_BUNDLE_KEY);
//                parseDataBundleAsync(asset);
            }

            @Override
            public void onDataDeleted(DataMap dataMap) {

            }
        });
        IntentFilter intentFilter = new IntentFilter(CLICK_PATH);
        registerReceiver(mBroadcastReceiver, intentFilter);

        mThreadPool = new ThreadPoolExecutor(2, 4, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    private void parseDataBundleAsync(final byte[] data) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (data != null) {
                    Logger.t("data").d("new bytes: " + data.length);
                    DataBundle dataBundle = unmarshall(data, DataBundle.CREATOR);
                    Logger.t("data").d("new data bundle: " + dataBundle);
                    sendDataBundleToWearApp(dataBundle);
//                    mDataBundleLruCache.put(Integer.toHexString(dataBundle.hashCode()),
//                            data);
                } else {
                    Logger.w("data bundle null");
                }
            }
        });
    }

    private void sendDataBundleToWearApp(final DataBundle dataBundle) {
        if (dataBundle == null) {
            return;
        }

        String appPkgName = dataBundle.getAppPkgName();
        ArrayList<DataNode> nodes = dataBundle.getDataNodes();

        // save image from bytes and return Uri to avoid large intent data
        for (DataNode node : nodes) {
            Logger.d("new node normal: " + node);
            if (!isNodeImageValid(node)) {
                //request real image from phone proxy
                byte[] dataBundleBytes = marshall(dataBundle);
                mGmsApi.sendMsg(DATA_BUNDLE_REQUIRED_IMAGE_PATH, dataBundleBytes, null);
                return;
            }
        }
        // list nodes parsing
        ArrayList<DataNode[]> listNodes = dataBundle.getListNodes();
        for (DataNode[] list : listNodes) {
            for (DataNode node : list) {
                isNodeImageValid(node);
                Logger.d("new node list: " + node);
            }
        }

        Intent appIntent = new Intent(INTENT_PREFIX + appPkgName + INTENT_SUFFIX);
        Logger.i("filter : " + INTENT_PREFIX + appPkgName + INTENT_SUFFIX);
        Bundle bundle = new Bundle();
        bundle.putParcelable(DATA_BUNDLE_KEY, dataBundle);
        appIntent.putExtra(DATA_BUNDLE_KEY, bundle);
        sendBroadcast(appIntent);
        Logger.t("data").i("new send " + dataBundle.toString());
    }

    private boolean isNodeImageValid(DataNode node) {
        if (node == null) {
            Logger.w("node null");
            return false;
        }
        // read hash of image first
        String imageFileHash = node.getImageHash();
        if (imageFileHash == null) {
            // normally imageFileHash can't be null, the only one case is list view container
            return true;
        }

        String path = getImageCacheFolderPath();
        File imageFile = new File(path, imageFileHash);
        Logger.d("image path: " + imageFile.getPath());
        // if image does not exist in cache(disk), ask phone proxy send real image
        if (imageFile.exists()) {
            node.setImageHash(imageFile.getPath());
            return true;
        } else {
            return false;
        }

    }

    private void storeImageToDiskCache(final byte[] image) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                String imageHash = Integer.toHexString(Arrays.hashCode(image));
                File imageFile = new File(getImageCacheFolderPath(), imageHash);
                Logger.v("image path: " + imageFile.getPath());
                try {
                    FileUtils.writeByteArrayToFile(imageFile, image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, getText(R.string.service_started), Toast.LENGTH_SHORT).show();
        if (mGmsApi != null) {
            Logger.v("gms connect");
            mGmsApi.connect();
        }
        Logger.i("start");

        return START_STICKY;
    }

    private void sendResolutionToPhone() {
        Point size = new Point();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getSize(size);
        mGmsApi.sendMsg(WATCH_RESOLUTION_PATH, marshall(size), null);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, getText(R.string.service_stopped), Toast.LENGTH_SHORT).show();
        mThreadPool.shutdown();
        unregisterReceiver(mBroadcastReceiver);
        if (mGmsApi != null) {
            Logger.v("gms disconnect");
            mGmsApi.disconnect();
        }
        super.onDestroy();
    }
}
