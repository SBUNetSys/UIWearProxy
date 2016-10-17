package edu.stonybrook.cs.netsys.uiwearproxy;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;

import com.orhanobut.logger.Logger;

import edu.stonybrook.cs.netsys.uiwearproxy.PreferenceManager.TransparentActivity;

public class PhoneActivity extends Activity {

    private static int NOTIFY_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button set = (Button) findViewById(R.id.set_btn);
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                raiseNotification();
                finish();
            }
        });
    }

    private void raiseNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("UIWear is running")
                        .setContentText("Setting preference");
        Intent resultIntent = new Intent(this, TransparentActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(NOTIFY_ID, mBuilder.build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.d("onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.d("onRestart");
    }

    @Override
    protected void onResume() {
        Logger.d("onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Logger.d("onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Logger.d("onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Logger.d("onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onUserLeaveHint() {
        Logger.d("onUserLeaveHint");
        super.onUserLeaveHint();
    }

    @Override
    public void onAttachedToWindow() {
        Logger.d("onAttachedToWindow");
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        Logger.d("onDetachedFromWindow");
        super.onDetachedFromWindow();
    }
}
