package edu.stonybrook.cs.netsys.uiwearproxy;

import android.app.Application;

import com.orhanobut.logger.Logger;

/**
 * Created by qqcao on 11/03/16 Thursday.
 *
 * Initialize GmsWear
 */

public class PhoneApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init("UIWearPhone");
    }
}
