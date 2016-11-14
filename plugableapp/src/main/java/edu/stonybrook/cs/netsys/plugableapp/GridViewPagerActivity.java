package edu.stonybrook.cs.netsys.plugableapp;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;

public class GridViewPagerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
        GridViewPager gridViewPager = (GridViewPager) findViewById(R.id.gridViewPager);
        gridViewPager.setAdapter(new FragmentGridPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getFragment(int row, int col) {
                return CardFragment.create("The Ambient White Noise of Nature",
                        "Quiet Flames in the Dark",
                        R.drawable.ic_media_play);
            }

            @Override
            public int getRowCount() {
                return 2;
            }

            @Override
            public int getColumnCount(int rowNum) {
                return 2;
            }
        });
    }
}