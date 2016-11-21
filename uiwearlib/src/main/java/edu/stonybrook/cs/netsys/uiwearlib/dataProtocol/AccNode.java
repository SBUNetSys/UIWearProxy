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

    public void removeAll() {
        mChildNodes.clear();
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

    public boolean matches(AccNode node) {
        return matches(node, 0.1);
    }

    /**
     * compare viewId, rect (resilient to position drift)
     *
     * @param node                the node to compare
     * @param rectDriftPercentage threshold for drift, 0 means exact match
     * @return whether two nodes match or not
     */
    public boolean matches(AccNode node, double rectDriftPercentage) {
        if (rectDriftPercentage < 0) {
            throw new IllegalArgumentException("threshold for drift should be non negative!");
        }

        String prefViewId = node.getViewId();

        boolean viewIdMatch;
        if (prefViewId == null) {
            viewIdMatch = (mViewId == null);
        } else {
            viewIdMatch = prefViewId.equals(mViewId);
        }

        Rect nodeRectInScreen = node.getRectInScreen();
        if (nodeRectInScreen.isEmpty()) {
            return false;
        }

        int nodeSize = nodeRectInScreen.height() * nodeRectInScreen.width();
        int size = mRectInScreen.height() * mRectInScreen.width();
        double diffRatio;
        int diffSize = Math.abs(size - nodeSize);
        // probably nodeSize will always be greater than 0
        if (nodeSize != 0) {
            diffRatio = diffSize / nodeSize * 1.0;
        } else {
            if (diffSize == 0) {
                diffRatio = 0;
            } else {
                // apparently not match
                return false;
            }
        }

        return viewIdMatch && diffRatio <= rectDriftPercentage;

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AccNode{").append("mViewId=").append(mViewId);
        sb.append(", mRectInScreen=").append(mRectInScreen);
        if (mChildNodes.size() > 0) {
            sb.append(", mChildNodes=").append(mChildNodes.size());
        }

        if (mId != 0) {
            sb.append(", mId=").append(mId);
        }

        if (mClassName != null) {
            sb.append(", mClassName=").append(mClassName);
        }

        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccNode)) return false;

        AccNode node = (AccNode) o;

        return mId == node.mId && mChildNodes.equals(node.mChildNodes)
                && (mViewId != null ? mViewId.equals(node.mViewId) : node.mViewId == null
                && mRectInScreen.equals(node.mRectInScreen)
                && (mClassName != null ? mClassName.equals(node.mClassName)
                : node.mClassName == null));

    }

    @Override
    public int hashCode() {
        int result = mChildNodes.hashCode();
        result = 31 * result + mId;
        result = 31 * result + (mViewId != null ? mViewId.hashCode() : 0);
        result = 31 * result + mRectInScreen.hashCode();
        result = 31 * result + (mClassName != null ? mClassName.hashCode() : 0);
        return result;
    }
}
