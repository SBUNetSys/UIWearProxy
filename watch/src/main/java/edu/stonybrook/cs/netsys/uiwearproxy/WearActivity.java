package edu.stonybrook.cs.netsys.uiwearproxy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.orhanobut.logger.Logger;

public class WearActivity extends Activity {

    private Intent proxyServiceIntent ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.i("on create");
        proxyServiceIntent = new Intent(this, WearProxyService.class);
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

    @Override
    protected void onResume() {
        Logger.i("on resume");
        super.onResume();
    }
}
