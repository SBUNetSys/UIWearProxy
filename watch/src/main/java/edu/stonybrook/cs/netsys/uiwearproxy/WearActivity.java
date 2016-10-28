package edu.stonybrook.cs.netsys.uiwearproxy;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

import com.morgoo.droidplugin.pm.PluginManager;
import com.orhanobut.logger.Logger;

import java.io.File;

import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_ALREADY_EXISTS;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_INTERNAL_ERROR;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_FAILED_INVALID_APK;
import static com.morgoo.helper.compat.PackageManagerCompat.INSTALL_SUCCEEDED;
import static edu.stonybrook.cs.netsys.uiwearlib.AppUtil.isActionAvailable;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WEAR_HOST_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WEAR_PLUGIN_ACTION_MAIN;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WEAR_PLUGIN_KEY;

public class WearActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.i("on create");
        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra(WEAR_HOST_KEY) != null) {
            String words = "say: " + intent.getStringExtra(WEAR_HOST_KEY);
            Toast.makeText(this, words, Toast.LENGTH_SHORT).show();
        }
    }

    public void goPlugin(View view) {
        if (isActionAvailable(this, WEAR_PLUGIN_ACTION_MAIN)) {
            Intent intent = new Intent(WEAR_PLUGIN_ACTION_MAIN);
            intent.putExtra(WEAR_PLUGIN_KEY, "Hello, plugin!");
            Logger.i("goPlugin");
            startActivityForResult(intent, 0);
        } else {
            Toast.makeText(this, "Failed to go plugin", Toast.LENGTH_SHORT).show();
        }
    }

    public void installApk(View view) {
        Logger.i("installApk");
//        File files = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_DOWNLOADS);
//        String apkFile = files.listFiles()[0].getAbsolutePath();
//        try {
//            InstallResult res = VirtualCore.get().installApp(apkFile,
//                    InstallStrategy.UPDATE_IF_EXIST);
//            if (!res.isSuccess) {
//                VLog.e(getClass().getSimpleName(), "Warning: Unable to install app %s: %s.",apkFile, res.error);
//            } else {
//                Toast.makeText(this, "Install success", Toast.LENGTH_SHORT).show();
//            }
//        } catch (Throwable e) {
//            Toast.makeText(this, "Not ready", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }
        try {
            File files = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            String apkFile = files.listFiles()[0].getAbsolutePath();
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

    public void openApp(View view) {
        Logger.i("openApp");
//        Intent intent = VirtualCore.get().getLaunchIntent("com.example.testwearapk", 0);
//        intent.putExtra(WEAR_PLUGIN_KEY, "Hello, plugin!");
//        VActivityManager.get().startActivity(intent, 0);
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage("edu.stonybrook.cs.netsys.plugableapp");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.i(String.format("requestCode=%s,resultCode=%s", requestCode, resultCode));
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                Logger.i("code ok");
                Toast.makeText(this, data.getStringExtra(WEAR_HOST_KEY),
                        Toast.LENGTH_SHORT).show();
            } else {
                Logger.i("code bad");
            }
        }
    }

    @Override
    protected void onResume() {
        Logger.i("on resume");
        super.onResume();
    }
}
