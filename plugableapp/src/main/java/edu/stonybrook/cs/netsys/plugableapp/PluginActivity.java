package edu.stonybrook.cs.netsys.plugableapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import static edu.stonybrook.cs.netsys.uiwearlib.AppUtil.isActionAvailable;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WEAR_HOST_ACTION_MAIN;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WEAR_HOST_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WEAR_PLUGIN_KEY;

public class PluginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra(WEAR_PLUGIN_KEY) != null) {
            String words = "say: " + intent.getStringExtra(WEAR_PLUGIN_KEY);
            Toast.makeText(this, words, Toast.LENGTH_SHORT).show();
        }
    }

    public void goHost(View view) {
        Logger.i("goHost");
        if (isActionAvailable(this, WEAR_HOST_ACTION_MAIN)) {
            Intent intent = new Intent(WEAR_HOST_ACTION_MAIN);
            intent.putExtra(WEAR_HOST_KEY, "Hello, host!");
            startActivity(intent);
        } else {
            Toast.makeText(this, "Failed to go host", Toast.LENGTH_SHORT).show();
        }
    }

    public void setResult(View view) {
        Logger.i("set result");
        Intent resultIntent = new Intent();
        resultIntent.putExtra(WEAR_HOST_KEY, "result from plugin");
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
