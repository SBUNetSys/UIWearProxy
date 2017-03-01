package edu.stonybrook.cs.netsys.uiwearlib.dataProtocol;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Data Payload is used for transferring media data, currently it support image bytes.
 *
 * We may support sensor data in the future.
 *
 * Created by qqcao on 03/01/2017.
 */

public class DataPayload implements Parcelable {

    private String mBitmapHash;
    private byte[] mBitmapBytes;

    public DataPayload(String bitmapHash, byte[] bitmapBytes) {
        mBitmapHash = bitmapHash;
        mBitmapBytes = bitmapBytes;
    }

    public byte[] getBitmapBytes() {
        return mBitmapBytes;
    }

    public String getBitmapHash() {
        return mBitmapHash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataPayload)) return false;

        DataPayload that = (DataPayload) o;

        return mBitmapHash.equals(that.mBitmapHash)
                && Arrays.equals(mBitmapBytes, that.mBitmapBytes);

    }

    @Override
    public int hashCode() {
        int result = mBitmapHash.hashCode();
        result = 31 * result + Arrays.hashCode(mBitmapBytes);
        return result;
    }

    @Override
    public String toString() {
        return "DataPayload{" + "mBitmapHash=" + mBitmapHash
                + ", mBitmapBytes=" + mBitmapBytes.length + '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mBitmapHash);
        dest.writeByteArray(this.mBitmapBytes);
    }

    private DataPayload(Parcel in) {
        this.mBitmapHash = in.readString();
        this.mBitmapBytes = in.createByteArray();
    }

    public static final Parcelable.Creator<DataPayload> CREATOR =
            new Parcelable.Creator<DataPayload>() {
                @Override
                public DataPayload createFromParcel(Parcel source) {
                    return new DataPayload(source);
                }

                @Override
                public DataPayload[] newArray(int size) {
                    return new DataPayload[size];
                }
            };
}
