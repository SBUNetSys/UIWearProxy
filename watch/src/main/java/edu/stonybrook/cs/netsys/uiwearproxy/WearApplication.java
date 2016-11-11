package edu.stonybrook.cs.netsys.uiwearproxy;

import static edu.stonybrook.cs.netsys.uiwearlib.Constant.CAPABILITY;

import android.app.Application;
import android.content.Context;

import com.cscao.libs.gmswear.GmsWear;
import com.morgoo.droidplugin.PluginHelper;

/**
 * Created by qqcao on 10/27/16 Thursday.
 *
 * DroidPlugin hook
 */

public class WearApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PluginHelper.getInstance().applicationOnCreate(getBaseContext());
        GmsWear.initialize(this, CAPABILITY);
    }

    @Override
    protected void attachBaseContext(Context base) {
        PluginHelper.getInstance().applicationAttachBaseContext(base);
        super.attachBaseContext(base);
    }

}
