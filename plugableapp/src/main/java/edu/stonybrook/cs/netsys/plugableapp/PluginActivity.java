package edu.stonybrook.cs.netsys.plugableapp;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.WEAR_PLUGIN_KEY;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PluginActivity extends Activity implements WearableListView.ClickListener {
    ResReceiver resReceiver;
    ListAdapter adapter;

    private ArrayList<Pair<Drawable, String[]>> listItems = new ArrayList<>();
    private static ArrayList<Integer> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recentlyplayed_main);
        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra(WEAR_PLUGIN_KEY) != null) {
            String words = "say: " + intent.getStringExtra(WEAR_PLUGIN_KEY);
            Toast.makeText(this, words, Toast.LENGTH_SHORT).show();
        }

        WearableListView mListView = (WearableListView) findViewById(R.id.content_list);
        init();
        adapter = new ListAdapter(this);
        mListView.setAdapter(adapter);
        mListView.setClickListener(PluginActivity.this);
        mListView.setKeepScreenOn(true);

    }

//    public void goHost(View view) {
//        Logger.i("goHost");
//        if (isActionAvailable(this, WEAR_HOST_ACTION_MAIN)) {
//            Intent intent = new Intent(WEAR_HOST_ACTION_MAIN);
//            intent.putExtra(WEAR_HOST_KEY, "Hello, host!");
//            startActivity(intent);
//        } else {
//            Toast.makeText(this, "Failed to go host", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    public void setResult(View view) {
//        Logger.i("set result");
//        Intent resultIntent = new Intent();
//        resultIntent.putExtra(WEAR_HOST_KEY, "result from plugin");
//        setResult(Activity.RESULT_OK, resultIntent);
//        finish();
//    }

    public void init() {
        listItems.clear();
        Drawable d = getDrawable(R.drawable.hangouts_default_avatar);
        String[] defaultStr = {"TestDefault", "LastMsg"};
        Pair<Drawable, String[]> d1 = new Pair<>(d, defaultStr);
        listItems.add(d1);
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        int pos = viewHolder.getLayoutPosition();
        if (ids != null && ids.size() > 0) {
            // TODO: 10/31/16 Monday talk to wear proxy via broadcast is better
//            Intent intent = new Intent(getApplicationContext(),
//                    ViewTreeService.class);
//            intent.putExtra(Constants.HANGOUTS_PKG + "hangoutId", ids.get(pos));
//            startService(intent);
        }
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    @Override
    protected void onStart() {
        //Register BroadcastReceiver
        //to receive event from our service
        resReceiver = new ResReceiver();
        IntentFilter intentFilter = new IntentFilter();
        // TODO: 10/31/16 Monday register intent filter for talking to wear proxy
//        intentFilter.addAction(Constants.HANGOUTS_PKG);
//        intentFilter.addAction(Constants.WINDOW_CHANGE);
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

    public class ListAdapter extends WearableListView.Adapter {
        private final LayoutInflater mInflater;

        private ListAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WearableListView.ViewHolder(
                    mInflater.inflate(R.layout.recently_played_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int i) {

            Pair<Drawable, String[]> pair = listItems.get(i);

            Drawable drawable = pair.first;
            ImageView imgView = (ImageView) holder.itemView.findViewById(R.id.thumbnail);
            imgView.setBackground(drawable);

            TextView songTextView = (TextView) holder.itemView.findViewById(R.id.text1);
            songTextView.setText(pair.second[0]);
            TextView albumTextView = (TextView) holder.itemView.findViewById(R.id.text2);
            albumTextView.setText(pair.second[1]);

            holder.itemView.setTag(i);
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }
    }

    private class ResReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            // TODO: 10/31/16 Monday refresh data
//            if (Constants.WINDOW_CHANGE.equals(intent.getAction())) {
//                init();
//                adapter.notifyDataSetChanged();
//            }

            // TODO: 10/31/16 Monday receive data from wear proxy
//            if (Constants.HANGOUTS_PKG.equals(intent.getAction())) {

            listItems.clear();
//                ArrayList<byte[]> imgList = (ArrayList<byte[]>) intent.
//                        getSerializableExtra(Constants.HANGOUTS_PKG + "imgList");
//
//                ArrayList<String> nameList = (ArrayList<String>) intent.
//                        getSerializableExtra(Constants.HANGOUTS_PKG + "nameList");
//                ArrayList<String> msgList = (ArrayList<String>) intent.
//                        getSerializableExtra(Constants.HANGOUTS_PKG + "msgList");

            // TODO: 10/31/16 Monday parse data(bitmap etc.) from wear proxy in background thread
//                for (int i = 0; i < imgList.size(); i++) {
//                    byte[] imgBytes = imgList.get(i);
//                    Drawable drawable = new BitmapDrawable(getResources(),
//                            BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length));
//
//                    String[] text = {nameList.get(i),msgList.get(i)};
//                    Pair<Drawable, String[]> pair = new Pair<>(drawable, text);
//                    listItems.add(pair);
//                }

            // TODO: 10/31/16 Monday record listener id for control back
//                ids = intent.getIntegerArrayListExtra(Constants.HANGOUTS_PKG + "hangoutId");
            adapter.notifyDataSetChanged();
        }
    }

}
