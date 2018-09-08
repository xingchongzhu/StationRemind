package com.traffic.locationremind.baidu.location.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.traffic.location.remind.R;

public class FullMapView extends ImageView {

    private static final String TAG = "FullMapView";

    private PointF point0 = new PointF();
    private PointF pointM = new PointF();

    private final float ZOOM_MIN_SPACE = 10f;

    // 设定事件模式
    private final int NONE = 0;
    private final int DRAG = 1;
    private final int ZOOM = 2;
    private int mode = NONE;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    // 获取屏幕分辨率。以480*320为例
    private int displayHeight = 480;
    private int displayWidth = 320;

    private float minScale = 0.8f;
    private float maxScale = 5f;
    private float currentScale = 1f;
    private float oldDist;

    private int imgWidth;
    private int imgHeight;

    public FullMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public FullMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public FullMapView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }


    public void setBitmap(Bitmap bm,int width,int height) {
        displayHeight = height;
        displayWidth = width;
        init(bm);
    }

    private void init(Bitmap bm) {
        // 获取屏幕的宽和高
        if(bm == null){
            return;
        }
        //bm = rotatePicture(bm);
        imgWidth = bm.getWidth();
        imgHeight = bm.getHeight();
        maxScale = 5f;
        currentScale = 1f;
        setImageBitmap(bm);
        minScale = getMinScale();
        matrix.setScale(minScale, minScale);
        center();
        setImageMatrix(matrix);
    }

    //缩小图片
    private Bitmap rotatePicture(Bitmap bmp) {
        Matrix matrix = new Matrix();
        matrix.setRotate(90,bmp.getWidth()/2,bmp.getHeight()/2);
        Bitmap createBmp = Bitmap.createBitmap(bmp,0,0,(int)bmp.getWidth(), (int)bmp.getHeight(), matrix,true);
        if(bmp != createBmp)
            bmp.recycle();
        return createBmp;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                point0.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > ZOOM_MIN_SPACE) {
                    savedMatrix.set(matrix);
                    setMidPoint(event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                whenMove(event);
                break;

        }
        setImageMatrix(matrix);
        checkView();
        return true;
    }


    private void whenMove(MotionEvent event) {
        switch (mode) {
            case DRAG:
                matrix.set(savedMatrix);
                matrix.postTranslate(event.getX() - point0.x, event.getY()
                        - point0.y);
                break;
            case ZOOM:
                float newDist = spacing(event);
                if (newDist > ZOOM_MIN_SPACE) {
                    matrix.set(savedMatrix);
                    float sxy = newDist / oldDist;
                    matrix.postScale(sxy, sxy, pointM.x, pointM.y);
                }
                break;
        }
    }

    // 两个触点的距离
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void setMidPoint(MotionEvent event) {
        float x = event.getX(0) + event.getY(1);
        float y = event.getY(0) + event.getY(1);
        pointM.set(x / 2, y / 2);
    }

    // 图片居中
    private void center() {
        RectF rect = new RectF(0, 0, imgWidth, imgHeight);
        matrix.mapRect(rect);
        float width = rect.width();
        float height = rect.height();
        float dx = 0;
        float dy = 0;

        if (width < displayWidth)
            dx = displayWidth / 2 - width / 2 - rect.left;
        else if (rect.left > 0)
            dx = -rect.left;
        else if (rect.right < displayWidth)
            dx = displayWidth - rect.right;

        if (height < displayHeight)
            dy = displayHeight / 2 - height / 2 - rect.top;
        else if (rect.top > 0)
            dy = -rect.top;
        else if (rect.bottom < displayHeight)
            dy = displayHeight - rect.bottom;

        matrix.postTranslate(dx, dy);
    }

    // 获取最小缩放比例
    private float getMinScale() {
        float sx = (float) displayWidth / imgWidth;
        float sy = (float) displayHeight / imgHeight;
        float scale = sx < sy ? sx : sy;
        if (scale > 1) {
            scale = 1f;
        }
        return scale;
    }

    // 检查约束条件，是否居中，空间显示是否合理
    private void checkView() {
        currentScale = getCurrentScale();
        if (mode == ZOOM) {
            if (currentScale < minScale) {
                matrix.setScale(minScale, minScale);
            }
            if (currentScale > maxScale) {
                matrix.set(savedMatrix);
            }
        }
        center();
    }

    // 图片当前的缩放比例
    private float getCurrentScale() {
        float[] values = new float[9];
        matrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

}
