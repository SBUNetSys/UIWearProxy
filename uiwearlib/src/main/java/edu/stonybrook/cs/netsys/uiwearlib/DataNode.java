package edu.stonybrook.cs.netsys.uiwearlib;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Data Node is the our UIWear data protocol for phone proxy to communicate with wear proxy
 * Each node carries the following attributes of AccessibilityNodeInfo:
 *
 * mId (hashcode), viewId (mViewIdResourceName), mText (mText), mImage (bitmap requested using
 * augmented AccessibilityService) and action (support mClickable for now)
 *
 * Created by qqcao on 03/12/2016.
 * Modified on 11/10/2016.
 */

public class DataNode implements Parcelable {
    private int mId;
    private String mViewId;
    private String mText;
    private boolean mClickable;
    // FIXME: 11/10/16 possibly need to add bitmap cache
    private Bitmap mImage;

    public DataNode(AccessibilityNodeInfo node) {
        mId = node.hashCode();
        mViewId = node.getViewIdResourceName();
        if (node.getText() != null) {
            mText = node.getText().toString();
        } else if (node.getContentDescription() != null) {
            mText = node.getContentDescription().toString();
        } else {
            mText = "";
        }

        mClickable = node.isClickable();
    }

    public DataNode(int id, String viewId, String text, boolean clickable, Bitmap image) {
        mId = id;
        mViewId = viewId;
        mText = text;
        mClickable = clickable;
        mImage = image;
    }


    public static final Creator<DataNode> CREATOR = new Creator<DataNode>() {
        @Override
        public DataNode createFromParcel(Parcel parcel) {
            int id = parcel.readInt();
            String viewId = parcel.readString();
            String text = parcel.readString();
            boolean clickable = parcel.readByte() != 0;
            Bitmap image = Bitmap.CREATOR.createFromParcel(parcel);

            return new DataNode(id, viewId, text, clickable, image);
        }

        @Override
        public DataNode[] newArray(int size) {
            return new DataNode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mViewId);
        dest.writeString(mText);
        dest.writeByte((byte) (mClickable ? 1 : 0));
        mImage.writeToParcel(dest, flags);
    }

    public int getID() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
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

    public boolean isClickable() {
        return mClickable;
    }

    public void setClickable(boolean clickable) {
        mClickable = clickable;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public void setImage(Bitmap image) {
        mImage = image;
    }

    public String getUniqueId() {
        return mViewId + mId;
    }

    @Override
    public String toString() {
        return "DataNode{" +
                "mId=" + Integer.toHexString(mId) +
                ", mViewId=" + mViewId +
                ", mText=" + mText +
                ", mClickable=" + mClickable +
                ", mImage=" + (mImage == null ? "null" : mImage.getByteCount()) +
                "}";
    }
}
