package edu.stonybrook.cs.netsys.uiwearproxy.uiwearService;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.stonybrook.cs.netsys.uiwearlib.AppUtil;
import edu.stonybrook.cs.netsys.uiwearproxy.R;

/**
 * For selecting apps that use UIWear service
 */
public class PreferredAppsSettingActivity extends Activity {

    private AppAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_setting);

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
        inflater.inflate(R.menu.menu_app_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.set_all:
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

class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private Context mContext;
    private List<String> pkgNameList;
    private List<Boolean> appEnabledStated;
    private SharedPreferences mSharedPreferences;

    AppAdapter(Context context, List<String> list) {
        mContext = context;
        pkgNameList = list;
        mSharedPreferences = mContext.getSharedPreferences("UIWearServingAppList", Context.MODE_PRIVATE);

        appEnabledStated = new ArrayList<>();
        for (String pkg : pkgNameList) {
            appEnabledStated.add(mSharedPreferences.getBoolean(pkg, false));
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView mCardView;
        ImageView mImageViewIcon;
        TextView mTextViewLabel;
        Switch mSwitchSelected;

        ViewHolder(View v) {
            super(v);
            // Get the widgets reference from custom layout
            mCardView = (CardView) v.findViewById(R.id.card_view);
            mImageViewIcon = (ImageView) v.findViewById(R.id.iv_icon);
            mTextViewLabel = (TextView) v.findViewById(R.id.app_label);
            mSwitchSelected = (Switch) v.findViewById(R.id.select_switch);
        }
    }

    @Override
    public AppAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.app_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        // Get the current package name
        final String packageName = pkgNameList.get(position);

        // Get the current app icon
        Drawable icon = AppUtil.getAppIconByPackageName(mContext, packageName);

        // Set the current app icon
        holder.mImageViewIcon.setImageDrawable(icon);

        // Get the current app label
        String label = AppUtil.getApplicationLabelByPackageName(mContext, packageName);

        // Set the current app label
        holder.mTextViewLabel.setText(label);


        // Set the  app check state
        holder.mSwitchSelected.setChecked(appEnabledStated.get(position));

        holder.mSwitchSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    Logger.i("enabled: " + packageName);
//                } else {
//                    Logger.i("disabled: " + packageName);
//                }
                appEnabledStated.set(holder.getAdapterPosition(), b);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(packageName, b);
                editor.apply();
            }
        });
    }

    @Override
    public int getItemCount() {
        // Count the installed apps
        return pkgNameList.size();
    }

    void setEnabledAll(boolean isEnabledAll) {
        appEnabledStated.clear();
        for (int i = 0; i < pkgNameList.size(); i++) {
            appEnabledStated.add(isEnabledAll);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(pkgNameList.get(i), isEnabledAll);
            editor.apply();
        }
        notifyDataSetChanged();
    }
}
