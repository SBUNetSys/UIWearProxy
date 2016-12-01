package edu.stonybrook.cs.netsys.uiwearlib.dataProtocol;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Data Node is the our UIWear data protocol for phone proxy to communicate with wear proxy
 * Each node carries the following attributes of AccessibilityNodeInfo:
 *
 * mClickId (hashcode), viewId (mViewIdResourceName), mText (mText), mImage (bitmap requested using
 * augmented AccessibilityService)
 *
 * Note: action (e.g. mClickable) is complex, depends on parent nodes and even grandparent nodes.
 * Currently we support this by setting action listeners on wearable side, and transfer
 * their mClickId (hashcode) to let phone proxy perform actions.
 *
 * Created by qqcao on 03/12/2016.
 * Modified on 11/10/2016.
 *
 * Updated on 11/30/2016 for lightweight data transfer
 */

public class DataNode implements Parcelable {

    private int mClickId;
    private String mViewId;
    private String mText;
    // image hash, on wear side, if hash exist,
    // wear proxy will process this file with real image path
    private String mImageHash;

    public DataNode(AccessibilityNodeInfo node) {
        mClickId = node.hashCode();
        mViewId = node.getViewIdResourceName();
        if (node.getText() != null) {
            mText = node.getText().toString();
        } else if (node.getContentDescription() != null) {
            mText = node.getContentDescription().toString();
        }
    }

    public DataNode(String viewId) {
        mViewId = viewId;
    }

    public int getClickId() {
        return mClickId;
    }

    public void setClickId(int id) {
        mClickId = id;
    }

    public String getViewId() {
        return mViewId;
    }

    public void setViewId(String viewId) {
        mViewId = viewId;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    @Override
    public String toString() {
        return "DataNode{"
                + "mClickId=" + Integer.toHexString(mClickId)
                + ", mViewId=" + mViewId
                + ", mText=" + mText
//                + ", mImage=" + (mImage == null ? "null" : mImage.length + " bytes")
                + ", mImageHash=" + mImageHash
                + ", hash=" + Integer.toHexString(hashCode()) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataNode)) return false;

        DataNode node = (DataNode) o;

        return mClickId == node.mClickId
                && (mViewId != null ? mViewId.equals(node.mViewId) : node.mViewId == null)
                && (mText != null ? mText.equals(node.mText) : node.mText == null)
                && (mImageHash != null ? mImageHash.equals(node.mImageHash)
                : node.mImageHash == null);

    }

    @Override
    public int hashCode() {
        int result = mClickId;
        result = 31 * result + (mViewId != null ? mViewId.hashCode() : 0);
        result = 31 * result + (mText != null ? mText.hashCode() : 0);
        result = 31 * result + (mImageHash != null ? mImageHash.hashCode() : 0);
        return result;
    }

    public void setImageHash(String imageHash) {
        mImageHash = imageHash;
    }

    public String getImageHash() {
        return mImageHash;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mClickId);
        dest.writeString(this.mViewId);
        dest.writeString(this.mText);
//        dest.writeByteArray(this.mImage);
        dest.writeString(this.mImageHash);
    }

    private DataNode(Parcel in) {
        this.mClickId = in.readInt();
        this.mViewId = in.readString();
        this.mText = in.readString();
//        this.mImage = in.createByteArray();
        this.mImageHash = in.readString();
    }

    public static final Creator<DataNode> CREATOR = new Creator<DataNode>() {
        @Override
        public DataNode createFromParcel(Parcel source) {
            return new DataNode(source);
        }

        @Override
        public DataNode[] newArray(int size) {
            return new DataNode[size];
        }
    };

}
