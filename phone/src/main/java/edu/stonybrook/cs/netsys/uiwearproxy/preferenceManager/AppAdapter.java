package edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ENABLED_APPS_PREF_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Switch;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.logger.Logger;

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
        final String label = AppUtil.getApplicationLabelByPackageName(mContext, pkgName);
        Drawable icon = AppUtil.getAppIconByPackageName(mContext, pkgName);
        boolean isAppEnabled = mSharedPreferences.getBoolean(pkgName, false);
        final int position = viewHolder.getAdapterPosition();
        viewHolder.setImageDrawable(R.id.iv_icon, icon)
                .setText(R.id.tv_app_label, label)
                .setText(R.id.tv_app_pkg, pkgName)
                .setChecked(R.id.switch_select, isAppEnabled);

        final Switch enableSwitch = viewHolder.getView(R.id.switch_select);

        viewHolder.convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableSwitch.toggle();
                boolean isChecked = enableSwitch.isChecked();
                Logger.d("app click %s status %s at %d: ", label, isChecked, position);
                setAppStatus(pkgName, isChecked);
            }
        });

        enableSwitch.setClickable(false);


//        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Logger.d("app check change %s status %s at %d: ", label, isChecked, position);
//                setAppStatus(pkgName, isChecked);
//            }
//        });
    }

    void enableAllApps() {
        setAllApps(true);
    }

    void disableAllApps() {
        setAllApps(false);
    }

    private void setAllApps(boolean isEnabled) {
        List<String> pkgNames = getData();
        for (String pkgName : pkgNames) {
            setAppStatus(pkgName, isEnabled);
        }
    }

    private void setAppStatus(String pkgName, boolean isEnabled) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(pkgName, isEnabled);
        editor.commit();
    }
}
