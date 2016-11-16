package edu.stonybrook.cs.netsys.uiwearlib.dataProtocol;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by qqcao on 11/15/16.
 *
 * Encapsulate action data, currently support click action
 */

public class DataAction implements Parcelable {

    // pkg name for background and foreground app distinction
    private String mPkgName;

    // hashCode of the original AccessibilityNodeInfo
    private int mActionId;

    public DataAction(String pkg, int actionId) {
        mPkgName = pkg;
        mActionId = actionId;
    }

//    public DataAction(byte[] bytes) {
//        String actionData = new String(bytes);
//
//    }

//    public byte[] toBytes() {
//        String actionData = mPkgName + "/" + mActionId;
//        return actionData.getBytes();
//    }

    private DataAction(Parcel in) {
        mPkgName = in.readString();
        mActionId = in.readInt();
    }

    public static final Creator<DataAction> CREATOR = new Creator<DataAction>() {
        @Override
        public DataAction createFromParcel(Parcel in) {
            return new DataAction(in);
        }

        @Override
        public DataAction[] newArray(int size) {
            return new DataAction[size];
        }
    };

    public String getPkgName() {
        return mPkgName;
    }

    public int getActionId() {
        return mActionId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPkgName);
        dest.writeInt(mActionId);
    }

    @Override
    public String toString() {
        return "DataAction{" + "mActionId=" + mActionId + ", mPkgName=" + mPkgName + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataAction)) return false;

        DataAction that = (DataAction) o;

        return mActionId == that.mActionId && mPkgName.equals(that.mPkgName);
    }

    @Override
    public int hashCode() {
        int result = mPkgName.hashCode();
        result = 31 * result + mActionId;
        return result;
    }
//    public DataAction(byte[] bytes) {
//        try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
//            try (ObjectInputStream o = new ObjectInputStream(b)) {
//                DataAction dataAction = (DataAction) o.readObject();
//                mActionId = dataAction.mActionId;
//                mPkgName = dataAction.mPkgName;
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public  byte[] toBytes(){
//        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
//            try (ObjectOutputStream o = new ObjectOutputStream(b)) {
//                o.writeObject(this);
//            }
//            return b.toByteArray();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }

}
