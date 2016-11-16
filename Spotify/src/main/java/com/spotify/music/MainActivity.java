package com.spotify.music;

import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CLICK_ID_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.CLICK_PATH;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.DATA_NODES_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.INTENT_SUFFIX;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.PKG_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.PREF_ID_KEY;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataNode;

public class MainActivity extends Activity {
    // resource receiver from wear proxy
    private ResReceiver mResReceiver;
    private GridViewPager mPager;
    private String[] mPreferenceIdArray;
    private int[] mWearViewIdIndexArray;
    private int[] mPhoneViewIdIndexArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPager = (GridViewPager) findViewById(R.id.gridViewPager);
        mPager.setAdapter(new LayoutAdapter(getFragmentManager()));

        mPreferenceIdArray = getResources().getStringArray(R.array.prefs);
        mWearViewIdIndexArray = getResourceId(R.array.ids);
        mPhoneViewIdIndexArray = getResourceId(R.array.phone_ids);
    }

    private class LayoutAdapter extends FragmentGridPagerAdapter {
        private int[] mLayouts;

        LayoutAdapter(FragmentManager fm) {
            super(fm);
            mLayouts = getResourceId(R.array.layouts);

        }

        @Override
        public Fragment getFragment(int row, int col) {
            int layoutId = mLayouts[row];
            return UIWearFragment.create(layoutId);
        }

        @Override
        public int getRowCount() {
            return mLayouts.length;
        }

        @Override
        public int getColumnCount(int i) {
            // currently set to only 2 column,
            // first column for wear app content
            // TODO: 11/15/16 second column for open app on phone
            return 2;
        }

        @Override
        public Drawable getBackgroundForPage(int row, int column) {
            return super.getBackgroundForPage(row, column);
        }


    }

    int[] getResourceId(int arrayResourceId) {
        TypedArray typedArray =
                getResources().obtainTypedArray(arrayResourceId);
        int[] result = new int[typedArray.length()];

        for (int i = 0; i < typedArray.length(); i++) {
            result[i] = typedArray.getResourceId(i, -1);
        }
        typedArray.recycle();
        return result;
    }

    @Override
    protected void onStart() {
        mResReceiver = new ResReceiver();
        IntentFilter intentFilter = new IntentFilter();

        Logger.v("filter : " + getPackageName() + INTENT_SUFFIX);
        intentFilter.addAction(getPackageName() + INTENT_SUFFIX);
        registerReceiver(mResReceiver, intentFilter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mResReceiver);
        super.onStop();
    }


    private class ResReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                // prefId : e.g., play_song_pref, recent_list_pref
                String prefId = intent.getStringExtra(PREF_ID_KEY);
                Logger.i("prefId: " + prefId);
                int pageIndex = Arrays.asList(mPreferenceIdArray).indexOf(prefId);
                Logger.i("prefId index : " + pageIndex);
                if (pageIndex == -1) {
                    // no suitable preference id for rendering layout
                    Logger.w("no preference layout found!");
                    return;
                }

                mPager.setCurrentItem(pageIndex, 0, false);
                // each node contains clickId, phoneViewId, text, image bytes
                ArrayList<DataNode> dataNodes = intent.getParcelableArrayListExtra(DATA_NODES_KEY);

                int wearIdIndex = mWearViewIdIndexArray[pageIndex];
                // get wear view id array of current page
                int[] wearViewIds = getResourceId(wearIdIndex);

                int phoneIdIndex = mPhoneViewIdIndexArray[pageIndex];
                String[] phoneViewIds = getResources().getStringArray(phoneIdIndex);

                for (DataNode node : dataNodes) {
                    parseData(context, wearViewIds, phoneViewIds, node);
                }

            }
        }
    }

    private void parseData(Context context, int[] wearViewIds, String[] phoneViewIds,
            DataNode node) {
        String phoneViewId = node.getViewId();
        Logger.i("phoneViewId: " + phoneViewId);

        int index = Arrays.asList(phoneViewIds).indexOf(phoneViewId);
        View nodeView = mPager.findViewById(wearViewIds[index]);

        setViewListener(node, nodeView);
        renderView(context, node, nodeView);

    }

    private void setViewListener(DataNode node, View nodeView) {
        final int clickId = node.getClickId();
        nodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent clickIntent = new Intent(CLICK_PATH);
                clickIntent.putExtra(PKG_KEY, getPackageName());
                clickIntent.putExtra(CLICK_ID_KEY, clickId);
                sendBroadcast(clickIntent);
            }
        });
    }

    private void renderView(Context context, DataNode node, View nodeView) {
        Logger.d("render node: " + node);
        Logger.d("render view: " + nodeView.toString());
        // use mapping rule field to set text and image info
        if (hasTextInfo(node)) {
            if (nodeView instanceof TextView) {
                ((TextView) nodeView).setText(node.getText());
            }
        }

        if (hasImageInfo(node)) {
            File imageFile = new File(node.getImageFile());
//            Uri imageUri = Uri.parse(node.getImageFile());
            Logger.v("image file: "+ imageFile);
            Glide.with(context).load(imageFile).asBitmap().into(getViewTarget(context, nodeView));
        }
    }

    private SimpleTarget<Bitmap> getViewTarget(final Context context, final View nodeView) {
        return new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource,
                    GlideAnimation<? super Bitmap> glideAnimation) {
                Drawable drawable = new BitmapDrawable(context.getResources(), resource);
                nodeView.setBackground(drawable);
            }
        };
    }

    private boolean hasTextInfo(DataNode node) {
        return node.getText() != null && !node.getText().isEmpty();
    }

    private boolean hasImageInfo(DataNode node) {
        return node.getImageFile() != null && !node.getImageFile().isEmpty();
    }

}
