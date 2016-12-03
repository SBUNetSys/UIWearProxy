package edu.stonybrook.cs.netsys.uiwearlib.dataProtocol;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Data Bundle is the our UIWear data protocol for phone proxy to communicate with wear proxy
 *
 * This bundle contain:
 *
 * 1. data bundle description,  preference id  and app package name
 *
 * 2. normal view data nodes and possible list view data nodes that corresponds to the preference
 * file
 *
 * Created by qqcao on 11/10/2016.
 */

public class DataBundle implements Parcelable {

    public static final Creator<DataBundle> CREATOR = new Creator<DataBundle>() {
        @Override
        public DataBundle createFromParcel(Parcel source) {
            return new DataBundle(source);
        }

        @Override
        public DataBundle[] newArray(int size) {
            return new DataBundle[size];
        }
    };

    private String mAppPkgName;
    private String mPreferenceId;
    private ArrayList<DataNode> mDataNodes;
    private ArrayList<ArrayList<DataNode>> mListNodes;

    public DataBundle(String appPkgName, String preferenceId) {
        mAppPkgName = appPkgName;
        mPreferenceId = preferenceId;
        mDataNodes = new ArrayList<>();
        mListNodes = new ArrayList<>();
    }


    public DataBundle(DataBundle dataBundle) {
        if (dataBundle != null) {
            mAppPkgName = dataBundle.mAppPkgName;
            mPreferenceId = dataBundle.mPreferenceId;
            mDataNodes = new ArrayList<>(dataBundle.mDataNodes);
            mListNodes = new ArrayList<>(dataBundle.mListNodes);
        }
    }

    private DataBundle(Parcel in) {
        this.mAppPkgName = in.readString();
        this.mPreferenceId = in.readString();
        this.mDataNodes = in.createTypedArrayList(DataNode.CREATOR);
        int listCount = in.readInt();
        this.mListNodes = new ArrayList<>(listCount);
        for (int i = 0; i < listCount; i++) {
            this.mListNodes.add(in.createTypedArrayList(DataNode.CREATOR));
        }
    }

    public void add(DataNode node) {
        mDataNodes.add(node);
    }

    public void add(ArrayList<DataNode> nodes) {
        mListNodes.add(nodes);
    }

    public void remove(DataNode node) {
        mDataNodes.remove(node);
    }

    public void remove(ArrayList<DataNode> node) {
        mListNodes.remove(node);
    }

    public void clearNormalData() {
        mDataNodes.clear();
    }

    public void clearListData() {
        mListNodes.clear();
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

    public ArrayList<ArrayList<DataNode>> getListNodes() {
        return mListNodes;
    }

//    public ArrayList<DataNode> getAllListNodes() {
//        ArrayList<DataNode> nodes = new ArrayList<>(listSize());
//        for (ArrayList<DataNode> listNodes : mListNodes) {
//            Collections.addAll(nodes, listNodes);
//        }
//        return nodes;
//    }

    public int normalSize() {
        return mDataNodes.size();
    }

    public int listSize() {
        int itemCount = 0;
        for (ArrayList<DataNode> nodes : mListNodes) {
            itemCount += nodes.size();
        }
        return itemCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataBundle)) return false;

        DataBundle bundle = (DataBundle) o;

//        Logger.d("equals mAppPkgName: " + mAppPkgName.equals(bundle.mAppPkgName));
//        Logger.d("equals mPreferenceId: " + mPreferenceId.equals(bundle.mPreferenceId));
//        Logger.d("equals mDataNodes: " + mDataNodes.equals(bundle.mDataNodes));
//        Logger.d("equals mListNodes: " + mListNodes.equals(bundle.mListNodes));

        return mAppPkgName.equals(bundle.mAppPkgName) && mPreferenceId.equals(bundle.mPreferenceId)
                && mDataNodes.equals(bundle.mDataNodes) && mListNodes.equals(bundle.mListNodes);

    }

    @Override
    public int hashCode() {
        int result = mAppPkgName.hashCode();
        result = 31 * result + mPreferenceId.hashCode();
        result = 31 * result + mDataNodes.hashCode();
        result = 31 * result + mListNodes.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (DataNode node : mDataNodes) {
            sb.append(node);
            sb.append("; ");
        }

        StringBuilder sbList = new StringBuilder();
        for (ArrayList<DataNode> node : mListNodes) {
            sbList.append(node);
            sbList.append("; ");
        }

        return "DataBundle{" + "mAppPkgName=" + mAppPkgName
                + ", mPreferenceId=" + mPreferenceId
                + ", hash=" + Integer.toHexString(hashCode())
                + ", size=[norm: " + mDataNodes.size() + ",list: " + this.listSize() + "]"
                + ", mDataNodes=" + sb.toString()
                + ", mListNodes=" + sbList.toString()
                + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mAppPkgName);
        dest.writeString(this.mPreferenceId);
        dest.writeTypedList(this.mDataNodes);
        int listCount = this.mListNodes.size();
        dest.writeInt(listCount);
        for (ArrayList<DataNode> nodes : this.mListNodes) {
            dest.writeTypedList(nodes);
        }
    }


}
