package edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ENABLED_APPS_PREF_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import edu.stonybrook.cs.netsys.uiwearlib.helper.AppUtil;
import edu.stonybrook.cs.netsys.uiwearproxy.R;

class AppAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    private Context mContext;
    private SharedPreferences mSharedPreferences;

    AppAdapter(Context context, List<String> appPkgList) {
        super(R.layout.item_app, appPkgList);
        mContext = context;
        mSharedPreferences = mContext.getSharedPreferences(ENABLED_APPS_PREF_NAME,
                Context.MODE_PRIVATE);
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, final String pkgName) {
        String label = AppUtil.getApplicationLabelByPackageName(mContext, pkgName);
        Drawable icon = AppUtil.getAppIconByPackageName(mContext, pkgName);
        boolean isAppEnabled = mSharedPreferences.getBoolean(pkgName, false);
        viewHolder.setImageDrawable(R.id.iv_icon, icon)
                .setText(R.id.tv_app_label, label)
                .setText(R.id.tv_app_pkg, pkgName)
                .setChecked(R.id.switch_select, isAppEnabled);

        Switch enableSwitch = viewHolder.getView(R.id.switch_select);
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAppStatus(isChecked, pkgName);
            }
        });

    }

    void enableAllApps() {
        setAllApps(true);
    }

    void disableAllApps() {
        setAllApps(false);
    }

    private void setAllApps(boolean value) {
        List<String> pkgNames = getData();
        for (String pkgName : pkgNames) {
            setAppStatus(value, pkgName);
        }
    }

    private void setAppStatus(boolean value, String pkgName) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(pkgName, value);
        editor.commit();
    }
}
