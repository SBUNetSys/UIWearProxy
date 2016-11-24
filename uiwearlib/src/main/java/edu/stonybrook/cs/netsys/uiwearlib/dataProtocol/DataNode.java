package edu.stonybrook.cs.netsys.uiwearlib.dataProtocol;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.ByteArrayOutputStream;
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

    private int mClickId;
    private String mViewId;
    private String mText;
    private byte[] mImage;

    // only for wear side use
    private String mImageFile;

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
        mImageFile = "";
        mImage = new byte[0];
    }

    public DataNode(String viewId) {
        mClickId = 0;
        mViewId = viewId;
        mText = "";
        mImageFile = "";
        mImage = new byte[0];
    }

//    public DataNode(int id, String viewId, String text, Bitmap image) {
//        mClickId = id;
//        mViewId = viewId;
//        mText = text;
//        mImage = getBitmapBytes(image);
//    }
//
//    public DataNode(int id, String viewId, String text, String imageFile, byte[] image) {
//        mClickId = id;
//        mViewId = viewId;
//        mText = text;
//        mImageFile = imageFile;
//        mImage = image;
//
//    }

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
                + Integer.toHexString(Arrays.hashCode(getBitmapBytes(bitmap)));
    }

    @Override
    public String toString() {
        return "DataNode{"
                + "mClickId=" + Integer.toHexString(mClickId)
                + ", mViewId=" + mViewId
                + ", mText=" + mText
                + ", mImage=" + (mImage == null ? "null" : mImage.length + " bytes")
                + ", mImageFile=" + mImageFile
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

    public void setImageFile(String imageFile) {
        mImageFile = imageFile;
    }

    public String getImageFile() {
        return mImageFile;
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
        dest.writeByteArray(this.mImage);
        dest.writeString(this.mImageFile);
    }

    private DataNode(Parcel in) {
        this.mClickId = in.readInt();
        this.mViewId = in.readString();
        this.mText = in.readString();
        this.mImage = in.createByteArray();
        this.mImageFile = in.readString();
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

    private static byte[] getBitmapBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (bitmap.getByteCount() > 50 * 1024) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        } else {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        }
        return stream.toByteArray();
    }
}
