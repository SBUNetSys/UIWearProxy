package edu.stonybrook.cs.netsys.plugableapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class PluginActivity extends Activity{
    ResReceiver resReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // we need to auto generate and replace layoutId based on wear proxy transferred id
        int layoutID = R.layout.card;

        UIWearFragment card = UIWearFragment.create(layoutID);
        fragmentTransaction.add(R.id.frame_layout_container, card);
        fragmentTransaction.commit();

    }

    // TODO: 10/31/16 Monday talk to wear proxy via broadcast is bette
    // click listener send id to wear proxy via LocalBroadcastManager

    @Override
    protected void onStart() {
        //Register BroadcastReceiver
        //to receive event from our service
        resReceiver = new ResReceiver();
        IntentFilter intentFilter = new IntentFilter();
        // TODO: 10/31/16 Monday register intent filter for talking to wear proxy
        registerReceiver(resReceiver, intentFilter);
        //Start our own service
//        Intent intent = new Intent(AndroidServiceTestActivity.this,
//                com.AndroidServiceTest.MyService.class);
//        startService(intent);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(resReceiver);
        super.onStop();
    }


    private class ResReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // TODO: 10/31/16 Monday receive card data(four info)  from wear proxy

            // TODO: 10/31/16 Monday parse data(bitmap etc.) from wear proxy in background thread

            // TODO: 10/31/16 Monday record listener id for control back
        }
    }

}
