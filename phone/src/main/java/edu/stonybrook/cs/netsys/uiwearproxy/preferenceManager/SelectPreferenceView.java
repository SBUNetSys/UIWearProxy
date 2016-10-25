package edu.stonybrook.cs.netsys.uiwearproxy.preferenceManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by qqcao on 10/9/16.
 * <p>
 * Customized view to capture user drawing area
 */

public class SelectPreferenceView extends View {
    public static final int STROKE_WIDTH = 2;
    public static final int DRAWING_COLOR = Color.RED;
//    public static final int UNSELECTED_COLOR = Color.TRANSPARENT;

    //    private Path drawPath;
    private Paint mPaint;//, canvasPaint;

    //    private Canvas drawCanvas;
//    private Bitmap canvasBitmap;
//
//    private float lastX;
//    private float lastY;
//
    private ArrayList<Rect> mPreferredNodes;

    public SelectPreferenceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    //get drawing area setup for interaction
    private void setupDrawing() {
//        drawPath = new Path();
        mPaint = new Paint();
        mPaint.setColor(DRAWING_COLOR);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(STROKE_WIDTH);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPreferredNodes = new ArrayList<>();
//        canvasPaint = new Paint(Paint.DITHER_FLAG);
//        preferRect = new Rect();
    }
//
//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//        drawCanvas = new Canvas(canvasBitmap);
//    }

    @Override
    protected void onDraw(Canvas canvas) {
//        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        for (Rect rect : mPreferredNodes) {
            canvas.drawRect(rect, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
////                Logger.d("down");
//                lastX = event.getX();
//                lastY = event.getY();
////                Logger.d("last x: " + lastX + "y: " + lastY);
////                drawPath.moveTo(lastX, lastY);
//                break;
//            case MotionEvent.ACTION_MOVE:
////                Logger.d("move");
//                float nowX = event.getX();
//                float nowY = event.getY();
//                int left = (int) Math.min(nowX, lastX);
//                int top = (int) Math.min(nowY, lastY);
//                int right = (int) Math.max(nowX, lastX);
//                int bottom = (int) Math.max(nowY, lastY);
//
//                preferRect.set(left, top, right, bottom);
////                Logger.d("prefer rect: " + preferRect);
//                drawPath.reset();
//                drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
////                Logger.d("now x: " + nowX + "y: " + nowY);
//
//                // draw rectangle
//                drawPath.moveTo(lastX, lastY);
//                drawPath.lineTo(lastX, nowY);
//
//                drawPath.moveTo(lastX, lastY);
//                drawPath.lineTo(nowX, lastY);
//
//                drawPath.moveTo(lastX, nowY);
//                drawPath.lineTo(nowX, nowY);
//
//                drawPath.moveTo(nowX, lastY);
//                drawPath.lineTo(nowX, nowY);
//
//                drawCanvas.drawPath(drawPath, mPaint);
//
//                break;
//            case MotionEvent.ACTION_UP:
////                Logger.d("up");
//                drawPath.reset();
//                drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                break;
//            default:
//                return false;
//        }
//        invalidate();
        return true;
    }

    //
//    public Rect getPreferRect() {
//        if (preferRect.isEmpty()) {
//            return null;
//        }
//        return preferRect;
//    }
//    public Path getDrawPath() {
//        return drawPath;
//    }

    public void removeNode(Rect preferRect) {
        mPreferredNodes.remove(preferRect);
        invalidate();
    }

    public void addNode(Rect preferRect) {
        mPreferredNodes.add(preferRect);
        invalidate();
    }

    public void removeAllNodes() {
        mPreferredNodes.clear();
        invalidate();
    }

    public ArrayList<Rect> getPreferredNodes() {
        return mPreferredNodes;
    }

    public boolean hasSelected(Rect rect) {
        return mPreferredNodes.contains(rect);
    }
}
