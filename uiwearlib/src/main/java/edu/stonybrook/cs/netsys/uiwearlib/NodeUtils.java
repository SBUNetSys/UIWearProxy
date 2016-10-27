package edu.stonybrook.cs.netsys.uiwearlib;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import com.orhanobut.logger.Logger;

/**
 * Created by qqcao on 10/22/16.
 * <p>
 * AccessibilityNodeInfo utils
 */

public class NodeUtils {
    public static void printNodeTree(AccessibilityNodeInfo node) {
        if (node == null) {
            Logger.v("printing null");
            return;
        }

        int count = node.getChildCount();
        if (count == 0) {
            Logger.v("printing " + node.toString());
        } else {
            for (int i = 0; i < count; i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                printNodeTree(child);
            }
        }
    }

    public static String getBriefNodeInfo(AccessibilityNodeInfo source) {
        if (source == null) {
            return "null";
        }
        Rect rectScreen = new Rect();
        source.getBoundsInScreen(rectScreen);

        return "rect: " + rectScreen.toString() + "; "
                + "viewID: " + source.getViewIdResourceName() + "; "
                + "class: " + source.getClassName() + "; "
                + "id: " + Integer.toHexString(source.hashCode()) + "; "
                + "text: " + source.getText() + "; "
                + "contentDesc: " + source.getContentDescription() + "; "
                + "click: " + source.isClickable();
    }
}
