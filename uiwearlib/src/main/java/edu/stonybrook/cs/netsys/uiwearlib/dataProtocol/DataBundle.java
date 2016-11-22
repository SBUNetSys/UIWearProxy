package edu.stonybrook.cs.netsys.uiwearlib.dataProtocol;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Data Bundle is the our UIWear data protocol for phone proxy to communicate with wear proxy
 *
 * This bundle contain:
 *
 * 1. a key, which has the information about preference file name and app packagename
 * e.g., com.spotify.music/2016-11-07-04_52_23
 *
 * 2. a list of app data nodes that corresponds to the preference file
 *
 * Created by qqcao on 11/10/2016.
 */

public class DataBundle implements Parcelable {
    public static final Creator<DataBundle> CREATOR = new Creator<DataBundle>() {
        @Override
        public DataBundle createFromParcel(Parcel parcel) {
            String key = parcel.readString();
            String preferenceId = parcel.readString();
            DataBundle dataBundle = new DataBundle(key, preferenceId);
            int count = parcel.readInt();
            for (int i = 0; i < count; i++) {
                DataNode node = DataNode.CREATOR.createFromParcel(parcel);
                dataBundle.add(node);
            }
            return dataBundle;
        }

        @Override
        public DataBundle[] newArray(int size) {
            return new DataBundle[size];
        }
    };

    private String mAppPkgName;

    private String mPreferenceId;

    private ArrayList<DataNode> mDataNodes;

    public DataBundle(String appPkgName, String preferenceId) {
        mAppPkgName = appPkgName;
        mPreferenceId = preferenceId;
        mDataNodes = new ArrayList<>();
    }

    public DataBundle(DataBundle dataBundle) {
        if (dataBundle != null) {
            mAppPkgName = dataBundle.getAppPkgName();
            mPreferenceId = dataBundle.getPreferenceId();
            mDataNodes = new ArrayList<>(dataBundle.getDataNodes());
        }
    }

    public void add(DataNode node) {
        mDataNodes.add(node);
    }
    public void remove(DataNode node) {
        mDataNodes.remove(node);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAppPkgName);
        dest.writeString(mPreferenceId);
        int count = mDataNodes.size();
        dest.writeInt(count);
        for (int i = 0; i < count; i++) {
            mDataNodes.get(i).writeToParcel(dest, flags);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (DataNode node : mDataNodes) {
            sb.append(node.toString());
            sb.append("; ");
        }
        return "DataBundle{" + "mAppPkgName=" + mAppPkgName
                + ", mPreferenceId=" + mPreferenceId
                + ", hash=" + Integer.toHexString(hashCode())
                + ", Size=" + mDataNodes.size()
                + ", mDataNodes=" + sb.toString()
                + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataBundle)) return false;

        DataBundle that = (DataBundle) o;

        return mAppPkgName.equals(that.mAppPkgName)
                && mPreferenceId.equals(that.mPreferenceId)
                && mDataNodes.equals(that.mDataNodes);

    }

    @Override
    public int hashCode() {
        int result = mAppPkgName.hashCode();
        result = 31 * result + mPreferenceId.hashCode();
        result = 31 * result + mDataNodes.hashCode();
        return result;
    }

    public String getAppPkgName() {
        return mAppPkgName;
    }

    public String getPreferenceId() {
        return mPreferenceId;
    }

    public ArrayList<DataNode> getDataNodes() {
        return mDataNodes;
    }

    public int size() {
        return mDataNodes.size();
    }
}
