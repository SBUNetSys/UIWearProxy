package edu.stonybrook.cs.netsys.uiwearlib.dataProtocol;

import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.IMAGE_DIR_NAME;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.PREF_DIR_NAME;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.RES_DIR_NAME;
import static edu.stonybrook.cs.netsys.uiwearlib.dataProtocol.DataConstant.RULE_DIR_NAME;

import android.content.Context;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * Created by qqcao on 11/14/16.
 *
 * Util class for operations on UIWear data
 */

public class DataUtil {
    public static final int BITMAP_DIR = 1;
    public static final int PREFERENCE_DIR = 2;
    public static final int MAPPING_RULE_DIR = 3;
    public static final int RESOURCE_DIR = 4;

    public static String getResDir(Context context, int dirType, String appPkgName) {
        String dirName = getDirName(dirType);
        return context.getObbDir() + File.separator + appPkgName + File.separator + dirName;
    }

    public static String getResDir(int dirType, String appPkgName) {
        String dirName = getDirName(dirType);
        File sdcard = Environment.getExternalStorageDirectory();
        return sdcard.getPath() + File.separator + "UIWear" + File.separator + appPkgName
                + File.separator
                + dirName;
    }

    private static String getDirName(int dirType) {
        String dirName;
        switch (dirType) {
            case BITMAP_DIR:
                dirName = IMAGE_DIR_NAME;
                break;
            case PREFERENCE_DIR:
                dirName = PREF_DIR_NAME;
                break;
            case MAPPING_RULE_DIR:
                dirName = RULE_DIR_NAME;
                break;
            case RESOURCE_DIR:
                dirName = RES_DIR_NAME;
                break;
            default:
                dirName = "temp";
        }
        return dirName;
    }

    public static byte[] marshall(Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // This is extremely important!
        return parcel;
    }

    public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshall(bytes);
        T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }
}
