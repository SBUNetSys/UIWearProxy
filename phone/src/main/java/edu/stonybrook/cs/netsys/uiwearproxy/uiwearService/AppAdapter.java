package edu.stonybrook.cs.netsys.uiwearproxy.uiwearService;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.ENABLED_APP_LIST_PREF_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.stonybrook.cs.netsys.uiwearlib.AppUtil;
import edu.stonybrook.cs.netsys.uiwearproxy.R;

class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private Context mContext;
    private List<String> mPkgNameList;
    private List<Boolean> mAppEnabledState;
    private SharedPreferences mSharedPreferences;

    AppAdapter(Context context, List<String> list) {
        mContext = context;
        mPkgNameList = list;
        mSharedPreferences = mContext.getSharedPreferences(ENABLED_APP_LIST_PREF_NAME, Context.MODE_PRIVATE);

        mAppEnabledState = new ArrayList<>();
        for (String pkg : mPkgNameList) {
            mAppEnabledState.add(mSharedPreferences.getBoolean(pkg, false));
        }
    }

    @Override
    public AppAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_app, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        // Get the current package name
        final String packageName = mPkgNameList.get(position);

        // Get the current app icon
        Drawable icon = AppUtil.getAppIconByPackageName(mContext, packageName);

        // Set the current app icon
        holder.mImageViewIcon.setImageDrawable(icon);

        // Get the current app label
        String label = AppUtil.getApplicationLabelByPackageName(mContext, packageName);

        // Set the current app label
        holder.mTextViewLabel.setText(label);


        // Set the  app check state
        holder.mSwitchSelected.setChecked(mAppEnabledState.get(position));

        holder.mSwitchSelected.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    Logger.i("enabled: " + packageName);
//                } else {
//                    Logger.i("disabled: " + packageName);
//                }
                mAppEnabledState.set(holder.getAdapterPosition(), b);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(packageName, b);
                editor.apply();
            }
        });
    }

    @Override
    public int getItemCount() {
        // Count the installed apps
        return mPkgNameList.size();
    }

    void setEnabledAll(boolean isEnabledAll) {
        mAppEnabledState.clear();
        for (int i = 0; i < mPkgNameList.size(); i++) {
            mAppEnabledState.add(isEnabledAll);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(mPkgNameList.get(i), isEnabledAll);
            editor.apply();
        }
        notifyDataSetChanged();
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
            mTextViewLabel = (TextView) v.findViewById(R.id.tv_app_label);
            mSwitchSelected = (Switch) v.findViewById(R.id.switch_select);
        }
    }
}
