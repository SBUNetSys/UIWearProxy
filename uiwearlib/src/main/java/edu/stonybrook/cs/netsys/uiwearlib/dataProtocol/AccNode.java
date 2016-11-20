package edu.stonybrook.cs.netsys.uiwearlib.dataProtocol;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;

/**
 * Created by qqcao on 11/20/16.
 *
 * Persisted AccessibilityNodeInfo
 */

public class AccNode {

    private ArrayList<AccNode> mChildNodes = new ArrayList<>();
    private int mId;
    private String mViewId;
    private Rect mRectInScreen = new Rect();
    private String mClassName;

    public AccNode(AccessibilityNodeInfo nodeInfo) {
        AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain(nodeInfo);
        mId = node.hashCode();
        mViewId = node.getViewIdResourceName();
        node.getBoundsInScreen(mRectInScreen);
        mClassName = node.getClassName().toString();
        int count = node.getChildCount();
        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                mChildNodes.add(new AccNode(child));
            }
        }
    }

    public AccNode(String viewId, String rectString) {
        mViewId = viewId;
        mRectInScreen = Rect.unflattenFromString(rectString);
    }

    public AccNode(String viewId, Rect rectInScreen) {
        mViewId = viewId;
        mRectInScreen = rectInScreen;
    }

    public AccNode() {
    }

    public void addChild(AccNode node) {
        mChildNodes.add(node);
    }

    public void removeChild(AccNode node) {
        mChildNodes.remove(node);
    }

    public int getChildCount() {
        return mChildNodes.size();
    }

    public AccNode getChild(int index) {
        return mChildNodes.get(index);
    }

    public ArrayList<AccNode> getChildNodes() {
        return mChildNodes;
    }

    public void setChildNodes(
            ArrayList<AccNode> childNodes) {
        mChildNodes = childNodes;
    }

    public String getClassName() {
        return mClassName;
    }

    public void setClassName(String className) {
        mClassName = className;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public Rect getRectInScreen() {
        return mRectInScreen;
    }

    public void setRectInScreen(Rect rectInScreen) {
        mRectInScreen = rectInScreen;
    }

    public String getViewId() {
        return mViewId;
    }

    public void setViewId(String viewId) {
        mViewId = viewId;
    }

    @Override
    public String toString() {
        return "AccNode{" + "mChildNodes=" + mChildNodes.size()
                + ", mId=" + mId + ", mViewId='" + mViewId + '\''
                + ", mRectInScreen=" + mRectInScreen + ", mClassName='" + mClassName + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccNode)) return false;

        AccNode node = (AccNode) o;

        return mId == node.mId && mChildNodes.equals(node.mChildNodes)
                && mViewId.equals(node.mViewId)
                && mRectInScreen.equals(node.mRectInScreen)
                && (mClassName != null ? mClassName.equals(node.mClassName)
                : node.mClassName == null);

    }

    @Override
    public int hashCode() {
        int result = mChildNodes.hashCode();
        result = 31 * result + mId;
        result = 31 * result + mViewId.hashCode();
        result = 31 * result + mRectInScreen.hashCode();
        result = 31 * result + (mClassName != null ? mClassName.hashCode() : 0);
        return result;
    }
}
