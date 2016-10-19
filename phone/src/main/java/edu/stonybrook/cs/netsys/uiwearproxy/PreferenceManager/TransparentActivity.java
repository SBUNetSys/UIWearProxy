package edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import edu.stonybrook.cs.netsys.uiwearproxy.R;

public class TransparentActivity extends Activity {
    private CaptureView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);

        drawView = (CaptureView) findViewById(R.id.drawing);

        drawView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });


    }

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            Toast.makeText(this, R.string.exit_preference_setting, Toast.LENGTH_SHORT).show();
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Rect preferRect = drawView.getPreferRect();
        if (preferRect == null) {
            Toast.makeText(this, R.string.null_back, Toast.LENGTH_SHORT).show();
        } else {
            Logger.d(preferRect);
            Toast.makeText(this, R.string.saved_back, Toast.LENGTH_SHORT).show();
        }


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
