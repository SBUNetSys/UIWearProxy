package edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import edu.stonybrook.cs.netsys.uiwearlib.AppUtil;
import edu.stonybrook.cs.netsys.uiwearproxy.R;

/**
 * For selecting apps that use UIWear service
 */
public class SettingEnabledAppsActivity extends Activity {

    private AppAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_enabled_apps);

        // Get the widgets reference from XML layout
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Define a layout for RecyclerView
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Initialize a new adapter for RecyclerView
        mAdapter = new AppAdapter(this, AppUtil.getInstalledPackages(this));

        // Set the adapter for RecyclerView
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_setting_enabled_apps, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_set_all:
                CheckBox checkBox = (CheckBox) item.getActionView();
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            mAdapter.setEnabledAll(true);
                        } else {
                            mAdapter.setEnabledAll(false);
                        }
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

