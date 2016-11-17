package edu.stonybrook.cs.netsys.uiwearproxy;

import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_ALREADY_EXISTS;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_INTERNAL_ERROR;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_INVALID_APK;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_SUCCEEDED;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATABUNDLE_CACHE_SIZE;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_HASH_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_REQUIRED_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.TRANSFER_APK_REQUEST;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.TRANSFER_MAPPING_RULES_REQUEST;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WATCH_RESOLUTION_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CLICK_ID_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CLICK_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.DATA_NODES_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.IMAGE_DIR_NAME;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.INTENT_PREFIX;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.INTENT_SUFFIX;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.PKG_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.PREF_ID_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.marshall;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataUtil.unmarshall;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.LruCache;
import android.view.WindowManager;
import android.widget.Toast;

import com.cscao.libs.gmswear.GmsWear;
import com.cscao.libs.gmswear.consumer.AbstractDataConsumer;
import com.cscao.libs.gmswear.consumer.DataConsumer;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.morgoo.droidplugin.pm.PluginManager;
import com.orhanobut.logger.Logger;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import edu.stonybrook.cs.netsys.uiwearlib.WorkerThread;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataAction;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataBundle;
import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataNode;

/**
 * UIWear wear side proxy service, handling sub view tree info received from phone
 */
public class WearProxyService extends Service {

    private GmsWear mGmsWear;
    private DataConsumer mDataConsumer;
    private WorkerThread mWorkerThread;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CLICK_PATH:
                    String pkgName = intent.getStringExtra(PKG_KEY);
                    int clickId = intent.getIntExtra(CLICK_ID_KEY, 0);
                    byte[] clickData = marshall(new DataAction(pkgName, clickId));
                    mGmsWear.sendMessage(CLICK_PATH, clickData);
                    break;
                default:
            }
        }
    };
    private LruCache<String, byte[]> mDataBundleLruCache = new LruCache<String, byte[]>(
            DATABUNDLE_CACHE_SIZE) {
        @Override
        protected int sizeOf(String key, byte[] dataBundle) {
            return dataBundle.length;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Logger.v("");
        return null;
    }

    @Override
    public void onCreate() {
        Logger.i("create");
        mWorkerThread = new WorkerThread("worker-thread");
        mWorkerThread.start();

        mGmsWear = GmsWear.getInstance();
        mDataConsumer = new AbstractDataConsumer() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent) {
                switch (messageEvent.getPath()) {
                    case DATA_BUNDLE_HASH_PATH:
                        Logger.i("DATA_BUNDLE_HASH_PATH");
                        byte[] hashStringBytes = messageEvent.getData();
                        String hashString = new String(hashStringBytes);
                        handleDataBundleHash(hashString);
                        break;
                    default:
                        Logger.w("unknown msg");
                }
            }

            @Override
            public void onDataChanged(DataEvent event) {
                Logger.i("data:");
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    // DataItem changed
                    DataItem item = event.getDataItem();
                    Logger.i(item.getUri().getPath());
                    if (item.getUri().getPath().equals(DATA_BUNDLE_PATH)) {
                        DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                        Asset asset = dataMap.getAsset(DATA_BUNDLE_KEY);
                        parseDataBundleAsync(asset);
                    }
                }
            }

            @Override
            public void onFileReceivedResult(int statusCode, String requestId, File savedFile,
                    String originalName) {
                Logger.d(
                        "File Received: status=%d, requestId=%s, savedLocation=%s, originalName=%s",
                        statusCode, requestId, savedFile.getAbsolutePath(), originalName);
                // for apk file transfer
                if (TRANSFER_APK_REQUEST.equals(requestId)) {
                    processApk(savedFile);
                }

                if (TRANSFER_MAPPING_RULES_REQUEST.equals(requestId)) {
                    processMappingRule(savedFile);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(CLICK_PATH);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void handleDataBundleHash(String hashString) {
        byte[] dataBundleBytes = mDataBundleLruCache.get(hashString);
        if (dataBundleBytes != null) {
            Logger.v("use data bundle in cache ");
            DataBundle bundle = unmarshall(dataBundleBytes, DataBundle.CREATOR);
            sendDataBundleToWearAppAsync(bundle);
        } else {
            Logger.v("DATA_BUNDLE_REQUIRED on wear");
            mGmsWear.sendMessage(DATA_BUNDLE_REQUIRED_PATH, hashString.getBytes());
        }
    }

    private void parseDataBundleAsync(final Asset asset) {
        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
                Logger.t("data").d("parseDataBundleAsync");
                byte[] data = new byte[0];
                try {
                    data = mGmsWear.loadAssetSynchronous(asset);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Logger.t("data").d("bytes: " + data.length);
                DataBundle dataBundle = unmarshall(data, DataBundle.CREATOR);
                sendDataBundleToWearAppAsync(dataBundle);
                mDataBundleLruCache.put(Integer.toString(dataBundle.hashCode()), data);
            }
        });

    }

    private void sendDataBundleToWearAppAsync(final DataBundle dataBundle) {
        if (dataBundle == null) {
            return;
        }

        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
                String appPkgName = dataBundle.getAppPkgName();
                String preferenceId = dataBundle.getPreferenceId();
                ArrayList<DataNode> nodes = dataBundle.getDataNodes();

                // save image from bytes and return Uri to avoid large intent data
                for (DataNode node : nodes) {
                    byte[] image = node.getImageBytes();
                    if (image != null && image.length > 0) {
                        String imageFile = convertImageBytesToUri(appPkgName, image);
                        node.setImageFile(imageFile);
                    }
                    // clear image bytes in original data node
                    image = new byte[0];
                    node.setImage(image);
                }

                Intent appIntent = new Intent(INTENT_PREFIX + appPkgName + INTENT_SUFFIX);
                Logger.i("filter : " + INTENT_PREFIX + appPkgName + INTENT_SUFFIX);

                appIntent.putExtra(PREF_ID_KEY, preferenceId);
                appIntent.putParcelableArrayListExtra(DATA_NODES_KEY, nodes);
                sendBroadcast(appIntent);

                Logger.t("data").i(dataBundle.toString());
            }
        });
    }

    private String convertImageBytesToUri(String appPkgName, byte[] image) {
        File imageFile = new File(getObbDir().getPath() + File.separator
                + appPkgName + File.separator + IMAGE_DIR_NAME + File.separator
                + Integer.toHexString(Arrays.hashCode(image)));

        Logger.v("image path: " + imageFile.getPath());

        try {
            FileUtils.writeByteArrayToFile(imageFile, image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile.getPath();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGmsWear.addWearConsumer(mDataConsumer);
        Logger.i("start");
        Point size = new Point();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getSize(size);
        mGmsWear.sendMessage(WATCH_RESOLUTION_PATH, marshall(size));

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mGmsWear.removeWearConsumer(mDataConsumer);
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private void processMappingRule(File mappingRuleFile) {

    }

    private void processApk(File apkFile) {

    }

    // install app from apk
    private void installApp(String apkFile) {
        if (apkFile == null) {
            Logger.e("apkFile path null");
            return;
        }

        //        File files = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_DOWNLOADS);
//        String apkFile = files.listFiles()[0].getAbsolutePath();
//        try {
//            InstallResult res = VirtualCore.get().installApp(apkFile,
//                    InstallStrategy.UPDATE_IF_EXIST);
//            if (!res.isSuccess) {
//                VLog.e(getClass().getSimpleName(), "Warning: Unable to install app %s: %s.",
// apkFile, res.error);
//            } else {
//                Toast.makeText(this, "Install success", Toast.LENGTH_SHORT).show();
//            }
//        } catch (Throwable e) {
//            Toast.makeText(this, "Not ready", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }
        try {
//            File files = Environment.getExternalStoragePublicDirectory(
//                    Environment.DIRECTORY_DOWNLOADS);
//             = files.listFiles()[0].getAbsolutePath();
            Logger.i(apkFile);
            int result = PluginManager.getInstance().installPackage(apkFile, 0);
            switch (result) {
                case INSTALL_SUCCEEDED:
                    Toast.makeText(this, "Install success", Toast.LENGTH_SHORT).show();
                    break;
                case INSTALL_FAILED_ALREADY_EXISTS:
                    Toast.makeText(this, "INSTALL_FAILED_ALREADY_EXISTS",
                            Toast.LENGTH_SHORT).show();
                    break;
                case INSTALL_FAILED_INTERNAL_ERROR:
                    Toast.makeText(this, "INSTALL_FAILED_INTERNAL_ERROR",
                            Toast.LENGTH_SHORT).show();
                    break;
                case INSTALL_FAILED_INVALID_APK:
                    Toast.makeText(this, "INSTALL_FAILED_INVALID_APK",
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, "Install failed", Toast.LENGTH_SHORT).show();
            }

        } catch (RemoteException e) {
            Toast.makeText(this, "Not ready", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
