package edu.stonybrook.cs.netsys.uiwearlib.dataProtocol;

import static edu.stonybrook.cs.netsys.uiwearlib.helper.AppUtil.getBitmapBytes;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityNodeInfo;

import com.orhanobut.logger.Logger;

import java.util.Arrays;

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
 */

public class DataNode implements Parcelable {
    public static final Creator<DataNode> CREATOR = new Creator<DataNode>() {
        @Override
        public DataNode createFromParcel(Parcel parcel) {
            int id = parcel.readInt();
            String viewId = parcel.readString();
            String text = parcel.readString();
            int length = parcel.readInt();
            byte[] image = new byte[length];
            if (length > 0) {
                parcel.readByteArray(image);
            } else {
                image = null;
                Logger.v("bitmap null");
            }

            return new DataNode(id, viewId, text, image);
        }

        @Override
        public DataNode[] newArray(int size) {
            return new DataNode[size];
        }
    };

    private int mClickId;
    private String mViewId;
    private String mText;
    private byte[] mImage;

    public DataNode(AccessibilityNodeInfo node) {
        mClickId = node.hashCode();
        mViewId = node.getViewIdResourceName();
        if (node.getText() != null) {
            mText = node.getText().toString();
        } else if (node.getContentDescription() != null) {
            mText = node.getContentDescription().toString();
        } else {
            mText = "";
        }
    }

    public DataNode(int id, String viewId, String text, Bitmap image) {
        mClickId = id;
        mViewId = viewId;
        mText = text;
        mImage = getBitmapBytes(image);
    }

    public DataNode(int id, String viewId, String text, byte[] image) {
        mClickId = id;
        mViewId = viewId;
        mText = text;
        mImage = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mClickId);
        dest.writeString(mViewId);
        dest.writeString(mText);
        if (mImage != null) {
            dest.writeInt(mImage.length);
            dest.writeByteArray(mImage);
        } else {
            dest.writeInt(0);
        }
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

    public byte[] getImageBytes() {
        return mImage;
    }

    public void setImage(byte[] image) {
        mImage = image;
    }

    public void setImage(Bitmap image) {
        mImage = getBitmapBytes(image);
    }

    public int getUniqueId() {
        return (mViewId + mText + mClickId).hashCode();
    }

    public String getFriendlyName(Bitmap bitmap) {
        return (mViewId).replaceAll("[^a-zA-Z0-9.-]", "_")
                + Arrays.hashCode(getBitmapBytes(bitmap));
    }

    @Override
    public String toString() {
        return "DataNode{"
                + "mClickId=" + Integer.toHexString(mClickId)
                + ", mViewId=" + mViewId
                + ", mText=" + mText
                + ", mImage=" + (mImage == null ? "null" : mImage.length + " bytes")
                + ", hash=" + Integer.toHexString(hashCode()) + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataNode)) return false;

        DataNode node = (DataNode) o;

        return mClickId == node.mClickId
                && mViewId.equals(node.mViewId)
                && mText.equals(node.mText)
                && Arrays.equals(mImage, node.mImage);

    }

    @Override
    public int hashCode() {
        int result = mClickId;
        result = 31 * result + mViewId.hashCode();
        result = 31 * result + mText.hashCode();
        result = 31 * result + Arrays.hashCode(mImage);
        return result;
    }
}
