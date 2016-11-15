package com.spotify.music;

import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.DATA_NODES_KEY;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.INTENT_SUFFIX;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.PREF_ID_KEY;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;

import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;

import edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataNode;

public class MainActivity extends Activity {
    // resource receiver from wear proxy
    private ResReceiver mResReceiver;
    private GridViewPager mPager;
    private String[] mPreferenceIdArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPager = (GridViewPager) findViewById(R.id.gridViewPager);
        mPager.setAdapter(new LayoutAdapter(getFragmentManager()));

        mPreferenceIdArray = getResources().getStringArray(R.array.prefs);
    }

    private class LayoutAdapter extends FragmentGridPagerAdapter {
        private int[] mLayouts;

        LayoutAdapter(FragmentManager fm) {
            super(fm);
            mLayouts = getLayoutsArray(R.array.layouts);

        }

        int[] getLayoutsArray(int arrayResourceId) {
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
            // second column for open app on phone
            return 2;
        }

        @Override
        public Drawable getBackgroundForPage(int row, int column) {
            return super.getBackgroundForPage(row, column);
        }


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
                // each node contains clickId, viewId, text, image bytes
                ArrayList<DataNode> dataNodes = intent.getParcelableArrayListExtra(DATA_NODES_KEY);

                int pageIndex = Arrays.asList(mPreferenceIdArray).indexOf(prefId);
                Logger.i("prefId index : " + pageIndex);

                mPager.setCurrentItem(pageIndex, 0, false);

            }

            // TODO: 10/31/16 Monday parse data(bitmap etc.) from wear proxy in background thread

            // TODO: 10/31/16 Monday record listener id for control back
        }
    }

}
