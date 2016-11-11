package edu.stonybrook.cs.netsys.uiwearproxy;

import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_ALREADY_EXISTS;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_INTERNAL_ERROR;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_INVALID_APK;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_SUCCEEDED;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.CLICK_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.DATA_BUNDLE_PATH;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
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

import java.io.File;
import java.io.IOException;

import edu.stonybrook.cs.netsys.uiwearlib.AppUtil;
import edu.stonybrook.cs.netsys.uiwearlib.DataBundle;
import edu.stonybrook.cs.netsys.uiwearlib.WorkerThread;

/**
 * UIWear wear side proxy service, handling sub view tree info received from phone
 */
public class WearProxyService extends Service {

    private GmsWear mGmsWear;
    private DataConsumer mDataConsumer;
    private WorkerThread mWorkerThread;

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
                Logger.i("onMessageReceived");
                String msg = new String(messageEvent.getData());
                if (messageEvent.getPath().equals(CLICK_PATH)) {
                    Logger.d(msg);
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
            }
        };

    }

    private void parseDataBundleAsync(final Asset asset) {
        mWorkerThread.postTask(new Runnable() {
            @Override
            public void run() {
                Logger.d("parseDataBundleAsync");
                byte[] data = new byte[0];
                try {
                    data = mGmsWear.loadAssetSynchronous(asset);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Logger.d("bytes: " + data.length);
                DataBundle dataBundle = AppUtil.unmarshall(data, DataBundle.CREATOR);
                Logger.i(dataBundle.toString());
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGmsWear.addWearConsumer(mDataConsumer);
        Logger.i("start");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mGmsWear.removeWearConsumer(mDataConsumer);
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
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
