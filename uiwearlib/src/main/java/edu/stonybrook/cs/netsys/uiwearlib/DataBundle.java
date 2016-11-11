package edu.stonybrook.cs.netsys.uiwearlib;

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
            DataBundle dataBundle = new DataBundle(key);
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
    private String mKey;
    private ArrayList<DataNode> mDataNodes;

    public DataBundle(String key) {
        mKey = key;
        mDataNodes = new ArrayList<>();
    }

    public void add(DataNode node) {
        mDataNodes.add(node);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mKey);
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
        return "DataBundle{" + "mKey=" + mKey
                + ", Size=" + mDataNodes.size()
                + ", mDataNodes=" + sb.toString()
                + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataBundle)) return false;

        DataBundle that = (DataBundle) o;

        return mKey.equals(that.mKey) && mDataNodes.equals(that.mDataNodes);

    }

    @Override
    public int hashCode() {
        int result = mKey.hashCode();
        result = 31 * result + mDataNodes.hashCode();
        return result;
    }
}
