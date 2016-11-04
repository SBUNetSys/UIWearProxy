package edu.stonybrook.cs.netsys.uiwearproxy;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.MSG_CAPABILITY;

import android.app.Application;

import com.cscao.libs.GmsWear.GmsWear;

/**
 * Created by qqcao on 11/03/16 Thursday.
 *
 * Initialize GmsWear
 */

public class PhoneApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        GmsWear.initialize(this, MSG_CAPABILITY);
    }
}
