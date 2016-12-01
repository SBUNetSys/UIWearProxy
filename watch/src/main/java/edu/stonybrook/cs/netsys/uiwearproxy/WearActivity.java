package edu.stonybrook.cs.netsys.uiwearproxy;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.PERMISSIONS_REQUEST_CODE;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

public class WearActivity extends Activity {

    private Intent proxyServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.i("on create");
        proxyServiceIntent = new Intent(this, WearProxyService.class);
        checkPermissions();
    }

    public void startProxyService(View view) {
        startService(proxyServiceIntent);
    }

    public void stopProxyService(View view) {
        Logger.i("stopProxyService");
        stopService(proxyServiceIntent);
    }


    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Logger.i(String.format("requestCode=%s,resultCode=%s", requestCode, resultCode));
//        if (requestCode == 0) {
//            if (resultCode == RESULT_OK) {
//                Logger.i("code ok");
//                Toast.makeText(this, data.getStringExtra(WEAR_HOST_KEY),
//                        Toast.LENGTH_SHORT).show();
//            } else {
//                Logger.i("code bad");
//            }
//        }
//    }
    private void checkPermissions() {
        boolean writeExternalStoragePermissionGranted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;

        if (!writeExternalStoragePermissionGranted) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Logger.v("write storage permission granted");
                startService(proxyServiceIntent);
            } else {
                Toast.makeText(this, "Please grant write permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        Logger.i("on resume");
        super.onResume();
    }
}
