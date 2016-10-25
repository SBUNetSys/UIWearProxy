package edu.stonybrook.cs.netsys.uiwearlib;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qqcao on 10/24/16.
 *
 * Utils related to applications
 */

public class AppUtil {
    // Get a list of installed app
    public static List<String> getInstalledPackages(Context mContext){
        // Initialize a new Intent which action is main
        Intent intent = new Intent(Intent.ACTION_MAIN,null);

        // Set the newly created intent category to launcher
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        // Set the intent flags
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK|
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        );

        // Generate a list of ResolveInfo object based on intent filter
        List<ResolveInfo> resolveInfoList = mContext.getPackageManager().queryIntentActivities(intent,0);

        // Initialize a new ArrayList for holding non system package names
        List<String> packageNames = new ArrayList<>();

        // Loop through the ResolveInfo list
        for(ResolveInfo resolveInfo : resolveInfoList){
            // Get the ActivityInfo from current ResolveInfo
            ActivityInfo activityInfo = resolveInfo.activityInfo;

            // If this is not a system app package
            if(!isSystemPackage(resolveInfo)){
                // Add the non system package to the list
                packageNames.add(activityInfo.applicationInfo.packageName);
            }
        }

        return packageNames;

    }

    // Custom method to determine an app is system app
    public static boolean isSystemPackage(ResolveInfo resolveInfo){
        return ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    // Custom method to get application icon by package name
    public static Drawable getAppIconByPackageName(Context mContext,String packageName){
        Drawable icon = null;
        try{
            icon = mContext.getPackageManager().getApplicationIcon(packageName);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            // Get a default icon
        }
        return icon;
    }

    // Custom method to get application label by package name
    public static String getApplicationLabelByPackageName(Context mContext, String packageName) {
        PackageManager packageManager = mContext.getPackageManager();
        ApplicationInfo applicationInfo;
        String label = "Unknown";
        try {
            applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            if (applicationInfo != null) {
                label = (String) packageManager.getApplicationLabel(applicationInfo);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return label;
    }
}