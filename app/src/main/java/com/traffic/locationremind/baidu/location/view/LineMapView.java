package com.traffic.locationremind.baidu.location.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainViewActivity;
import com.traffic.locationremind.baidu.location.object.MarkObject;
import com.traffic.locationremind.common.util.CommonFuction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LineMapView extends View {

    private static final String TAG = LineMapView.class.getSimpleName();

    public static final int ROWMAXCOUNT = 8;// 一行最多点

    public static final int MAXSCALE = 2;

    private static final long DOUBLE_CLICK_TIME_SPACE = 300;

    private float mCurrentScaleMax;
    private float mCurrentScale;
    private float mCurrentScaleMin;
    private float initScale;

    private float windowWidth, windowHeight;

    private Bitmap mBitmap;
    private Bitmap mBitmapStart;
    private Bitmap mBitmapEnd;
    private Bitmap mBitmapCurrent;
    private Bitmap mBitmapLocationCurrent;
    private Paint mPaint;

    private PointF mStartPoint, mapCenter;// mapCenter表示地图中心在屏幕上的坐标
    private long lastClickTime;// 记录上一次点击屏幕的时间，以判断双击事件
    private Status mStatus = Status.NONE;

    private float oldRate = 1;
    private float oldDist = 1;
    private float offsetX, offsetY;

    private boolean isShu = true;
    //private float pointDistance;

    private boolean full_screen = false;//全局图
    private Context context;
    private boolean isOnpause = false;
    private final int DIS_BAR_HEIGHT = 20;
    private final float Icon_size = 1.5f;

    private List<MarkObject> markList = new ArrayList<MarkObject>();

    private enum Status {
        NONE, ZOOM, DRAG
    }

    public void setMardStartState(boolean state,String cname){
        final int markList_size = markList.size();// Moved  markList.size() call out of the loop to local variable markList_size
        for(int n = 0; n< markList_size; n++){
            if(markList.get(n).mStationInfo.getCname().equals(cname)){
                markList.get(n).isStartStation = state;
            }else{
                markList.get(n).isStartStation = !state;
            }
        }
    }

    public void setMardEndState(boolean state,String cname){
        final int markList_size = markList.size();// Moved  markList.size() call out of the loop to local variable markList_size
        for(int n = 0; n< markList_size; n++){
            if(markList.get(n).mStationInfo.getCname().equals(cname)){
                markList.get(n).isEndStation = state;
            }else{
                markList.get(n).isEndStation = !state;
            }
        }
    }

    public void resetMardEndState(){
        final int markList_size = markList.size();// Moved  markList.size() call out of the loop to local variable markList_size
        for(int n = 0; n< markList_size; n++){
            markList.get(n).isEndStation = false;
            markList.get(n).isCurrentStation = false;
            markList.get(n).isStartStation = false;
            markList.get(n).isCurrentLocationStation = false;
        }
        postInvalidate();
    }

    public void setCurrentStation(String cname){
        final int markList_size = markList.size();// Moved  markList.size() call out of the loop to local variable markList_size
        for(int n = 0; n< markList_size; n++){
            if(markList.get(n).mStationInfo.getCname().equals(cname)){
                markList.get(n).isCurrentStation = true;
                continue;
            }
            markList.get(n).isCurrentStation = false;
        }
        postInvalidate();
    }

    public List<MarkObject> getMarkList(){
        return markList;
    }

    public void setPauseState(boolean isOnpause) {
        this.isOnpause = isOnpause;
    }

    public void setFullScree(boolean full_screen) {
        this.full_screen = full_screen;
    }

    public void setInitScale() {
        mCurrentScale = mCurrentScaleMin;
    }

    public boolean getFullScree() {
        return full_screen;
    }

    public LineMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public LineMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public LineMapView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        // 获取屏幕的宽和高
        windowWidth = getResources().getDisplayMetrics().widthPixels;
        MarkObject.size = (int)windowWidth/ROWMAXCOUNT/3;
        MarkObject.ROWHEIGHT = (int)MarkObject.size*4;
        windowHeight = (getResources().getDisplayMetrics().heightPixels
                - getStatusBarHeight())/2;
        //pointDistance = windowWidth / ROWMAXCOUNT;
        mPaint = new Paint();

        mStartPoint = new PointF();
        mapCenter = new PointF();
        mBitmapStart = CommonFuction.getbitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.cm_main_map_pin_start),
                (int)(MarkObject.size*Icon_size),(int)(MarkObject.size*Icon_size));
        mBitmapEnd = CommonFuction.getbitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.cm_main_map_pin_end),
                (int)(MarkObject.size*Icon_size),(int)(MarkObject.size*Icon_size));
        mBitmapCurrent = CommonFuction.getbitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.bus_station),
                (int)(MarkObject.size*Icon_size),(int)(MarkObject.size*Icon_size));
        mBitmapLocationCurrent = CommonFuction.getbitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.cm_main_map_pin_location),
                (int)(MarkObject.size*Icon_size),(int)(MarkObject.size*Icon_size));
    }

    public float getViewWidth() {
        return windowWidth;
    }

    public void setBitmap(Bitmap bitmap) {
        if (mBitmap != null) {
            mBitmap.recycle();
        }
        this.mBitmap = bitmap;
        // 设置最小缩放为铺满屏幕，最大缩放为最小缩放的4倍
        mCurrentScaleMin = Math.min(windowHeight / mBitmap.getHeight(),
                windowWidth / mBitmap.getWidth());
        mCurrentScale = mCurrentScaleMin;
        mCurrentScaleMax = mCurrentScaleMin * MAXSCALE;
        mapCenter.set(mBitmap.getWidth() * mCurrentScale / 2,
                mBitmap.getHeight() * mCurrentScale / 2+DIS_BAR_HEIGHT);
        float bitmapRatio = mBitmap.getHeight() / mBitmap.getWidth();
        float winRatio = windowHeight / windowWidth;
        // 判断屏幕铺满的情况，isShu为true表示屏幕横向被铺满，为false表示屏幕纵向被铺满
        if (bitmapRatio <= winRatio) {
            isShu = true;
        } else {
            isShu = false;
        }
        postInvalidate();
    }

    /**
     * 为当前地图添加标记
     *
     * @param object
     */
    public void addMark(MarkObject object) {
        markList.add(object);
    }

    public void clearMark() {
        markList.clear();
        mCurrentScale = mCurrentScaleMin;
        mStatus = Status.NONE;
        full_screen = false;//全局图
        postInvalidate();
    }

    /**
     * 地图放大
     */
    public void zoomIn() {
        mCurrentScale *= 1.5f;
        if (mCurrentScale > mCurrentScaleMax) {
            mCurrentScale = mCurrentScaleMax;
        }

    }

    /**
     * 地图缩小
     */
    public void zoomOut() {
        mCurrentScale /= 1.5f;
        if (mCurrentScale < mCurrentScaleMin) {
            mCurrentScale = mCurrentScaleMin;
        }
        if (isShu) {
            if (mapCenter.x - mBitmap.getWidth() * mCurrentScale / 2 > 0) {
                mapCenter.x = mBitmap.getWidth() * mCurrentScale / 2;
            } else if (mapCenter.x + mBitmap.getWidth() * mCurrentScale / 2 < windowWidth) {
                mapCenter.x = windowWidth - mBitmap.getWidth() * mCurrentScale
                        / 2;
            }
            if (mapCenter.y - mBitmap.getHeight() * mCurrentScale / 2 > 0) {
                mapCenter.y = mBitmap.getHeight() * mCurrentScale / 2;
            }
        } else {

            if (mapCenter.y - mBitmap.getHeight() * mCurrentScale / 2 > 0) {
                mapCenter.y = mBitmap.getHeight() * mCurrentScale / 2;
            } else if (mapCenter.y + mBitmap.getHeight() * mCurrentScale / 2 < windowHeight) {
                mapCenter.y = windowHeight - mBitmap.getHeight()
                        * mCurrentScale / 2;
            }

            if (mapCenter.x - mBitmap.getWidth() * mCurrentScale / 2 > 0) {
                mapCenter.x = mBitmap.getWidth() * mCurrentScale / 2;
            }
        }

    }

    // 处理拖拽事件
    private void drag(MotionEvent event) {
        if (mBitmap == null)
            return;
        PointF currentPoint = new PointF();
        currentPoint.set(event.getX(), event.getY());
        offsetX = currentPoint.x - mStartPoint.x;
        offsetY = currentPoint.y - mStartPoint.y;
        // 以下是进行判断，防止出现图片拖拽离开屏幕
        if (offsetX > 0
                && mapCenter.x + offsetX - mBitmap.getWidth() * mCurrentScale
                / 2 > 0) {
            offsetX = 0;
        }
        if (offsetX < 0
                && mapCenter.x + offsetX + mBitmap.getWidth() * mCurrentScale
                / 2 < windowWidth) {
            offsetX = 0;
        }
        if (offsetY > 0
                && mapCenter.y + offsetY - mBitmap.getHeight() * mCurrentScale
                / 2 > 0) {
            offsetY = 0;
        }
        if (offsetY < 0
                && mapCenter.y + offsetY + mBitmap.getHeight() * mCurrentScale
                / 2 < windowHeight) {
            offsetY = 0;
        }
        mapCenter.x += offsetX;
        mapCenter.y += offsetY;
        mStartPoint = currentPoint;

    }

    // 处理多点触控缩放事件
    private void zoomAction(MotionEvent event) {
        float newDist = spacing(event);
        if (newDist > 10.0f) {
            mCurrentScale = oldRate * (newDist / oldDist);
            if (mCurrentScale < mCurrentScaleMin) {
                mCurrentScale = mCurrentScaleMin;
            } else if (mCurrentScale > mCurrentScaleMax) {
                mCurrentScale = mCurrentScaleMax;
            }

            if (isShu) {
                if (mapCenter.x - mBitmap.getWidth() * mCurrentScale / 2 > 0) {
                    mapCenter.x = mBitmap.getWidth() * mCurrentScale / 2;
                } else if (mapCenter.x + mBitmap.getWidth() * mCurrentScale / 2 < windowWidth) {
                    mapCenter.x = windowWidth - mBitmap.getWidth()
                            * mCurrentScale / 2;
                }
                if (mapCenter.y - mBitmap.getHeight() * mCurrentScale / 2 > 0) {
                    mapCenter.y = mBitmap.getHeight() * mCurrentScale / 2;
                }
            } else {

                if (mapCenter.y - mBitmap.getHeight() * mCurrentScale / 2 > 0) {
                    mapCenter.y = mBitmap.getHeight() * mCurrentScale / 2;
                } else if (mapCenter.y + mBitmap.getHeight() * mCurrentScale
                        / 2 < windowHeight) {
                    mapCenter.y = windowHeight - mBitmap.getHeight()
                            * mCurrentScale / 2;
                }

                if (mapCenter.x - mBitmap.getWidth() * mCurrentScale / 2 > 0) {
                    mapCenter.x = mBitmap.getWidth() * mCurrentScale / 2;
                }
            }
        }


    }

    // 处理点击标记的事件
    private void clickAction(MotionEvent event) {

        int clickX = (int) event.getX();
        int clickY = (int) event.getY();
        try {
            for (MarkObject object : markList) {
                Bitmap location = object.getmBitmap();
                int objX = (int) (mapCenter.x - location.getWidth() / 2
                        - mBitmap.getWidth() * mCurrentScale / 2 + mBitmap
                        .getWidth() * object.getMapX() * mCurrentScale);
                int objY = (int) (mapCenter.y - location.getHeight()
                        - mBitmap.getHeight() * mCurrentScale / 2 + mBitmap
                        .getHeight() * object.getMapY() * mCurrentScale);
                // 判断当前object是否包含触摸点，在这里为了得到更好的点击效果，我将标记的区域放大了
                if (objX - location.getWidth() < clickX
                        && objX + location.getWidth() > clickX
                        && objY + location.getHeight() > clickY
                        && objY - location.getHeight() < clickY) {
                    if (object.getMarkListener() != null) {
                        object.getMarkListener().onMarkClick(clickX, clickY,
                                object);
                    }
                    break;
                }

            }
        } catch (Exception e) {

        }

    }

    // 计算两个触摸点的距离
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        try {
            if (canvas != null && mBitmap != null) {
                if (isOnpause) {
                    return;
                }
                Matrix matrix = new Matrix();
                canvas.drawColor(Color.WHITE);
                matrix.setScale(mCurrentScale, mCurrentScale,
                        mBitmap.getWidth() / 2, mBitmap.getHeight() / 2);
                matrix.postTranslate(mapCenter.x - mBitmap.getWidth() / 2,
                        mapCenter.y - mBitmap.getHeight() / 2);
                canvas.drawBitmap(mBitmap, matrix, mPaint);
                if(!full_screen) {
                    float x;
                    float y;
                    float px = 0;
                    boolean firstDraw = true;
                    int n = 0;
                    int prerow = 0;
                    mPaint.setStrokeWidth(5);

                    mPaint.setColor(markList.get(0).getColorId());
                    for (MarkObject object : markList) {
                        if (isOnpause) {
                            return;
                        }
                        Bitmap location = object.getmBitmap();
                        // 使用Matrix使得Bitmap的宽和高发生变化，在这里使用的mapX和mapY都是相对值
                        x = mapCenter.x - location.getWidth() / 2
                                - mBitmap.getWidth() * mCurrentScale / 2
                                + mBitmap.getWidth() * object.getMapX()
                                * mCurrentScale;
                        y = mapCenter.y - location.getHeight()
                                - mBitmap.getHeight() * mCurrentScale / 2
                                + mBitmap.getHeight() * object.getMapY()
                                * mCurrentScale+DIS_BAR_HEIGHT;
                       // y=y*2;
                        object.setX(x);
                        object.setY(y);
                        int curow = n / (LineMapView.ROWMAXCOUNT) + 1;
                        int cloume = n % (LineMapView.ROWMAXCOUNT);

                        if (prerow == curow) {
                            canvas.drawLine(px,
                                    y + object.getCurrentsize() / 2, x, y
                                            + object.getCurrentsize() / 2,
                                    mPaint);
                        }
                        if ((cloume == (LineMapView.ROWMAXCOUNT - 1))) {
                            canvas.drawLine(x, y + object.getCurrentsize() / 2,
                                    windowWidth, y + object.getCurrentsize()
                                            / 2, mPaint);
                        }
                        if (n == (markList.size() - 1)) {
                            canvas.drawLine(x, y + object.getCurrentsize() / 2,
                                    x + object.getCurrentsize() * 2,
                                    y + object.getCurrentsize() / 2, mPaint);
                        }
                        if (cloume == 0 || firstDraw) {
                            canvas.drawLine(0, y + object.getCurrentsize() / 2,
                                    x, y + object.getCurrentsize() / 2, mPaint);
                        }
                        firstDraw = false;
                        prerow = curow;
                        px = x;
                        n++;
                    }
                    mPaint.setStrokeWidth(1);
                    int textSize = (int) context.getResources().getDimension(R.dimen.text_size);
                    mPaint.setTextSize(textSize);
                    mPaint.setColor(context.getResources().getColor(R.color.black));
                    for (MarkObject object : markList) {
                        matrix.setScale(1.0f, 1.0f);
                        matrix.postTranslate(object.getX(), object.getY());
                        canvas.drawBitmap(object.getmBitmap(), matrix, mPaint);
                        matrix.setScale(1.0f, 1.0f);
                        matrix.postTranslate(object.getX()-object.getmBitmap().getWidth()/4, object.getY()-mBitmapStart.getHeight()/2-object.getmBitmap().getHeight()/3+DIS_BAR_HEIGHT/2);
                        if(object.isStartStation){
                            canvas.drawBitmap(mBitmapStart, matrix, mPaint);
                        }
                        if(object.isEndStation){
                            canvas.drawBitmap(mBitmapEnd, matrix, mPaint);
                        }
                        if(object.isCurrentStation){
                            canvas.drawBitmap(mBitmapCurrent, matrix, mPaint);
                        }

                        if(object.isCurrentLocationStation && !CommonFuction.getSharedPreferencesBooleanValue(context, CommonFuction.ISREMINDER)){
                            canvas.drawBitmap(mBitmapLocationCurrent, matrix, mPaint);
                        }
                        int length = (int) (object.getName().length() * 3);
                        canvas.drawText(object.getName(), object.getX() - length, object.getY() + object.getCurrentsize() * 1.8f, mPaint);

                    }

                }
            }
        } catch (Exception e) {

        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mStatus != Status.ZOOM) {
                    if (!full_screen)
                        clickAction(event);
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                float distance = spacing(event);
                if (distance > 10f) {
                    mStatus = Status.ZOOM;
                    oldDist = distance;
                }

                break;

            case MotionEvent.ACTION_MOVE:

                if (mStatus == Status.DRAG) {
                    drag(event);
                } else if (mStatus == Status.ZOOM) {
                    zoomAction(event);
                }
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:

                if (event.getPointerCount() == 1) {
                    // 如果两次点击时间间隔小于一定值，则默认为双击事件
                    if (event.getEventTime() - lastClickTime < DOUBLE_CLICK_TIME_SPACE) {
                        if (mCurrentScale < mCurrentScaleMax) {
                            zoomIn();
                        }else{
                            mCurrentScale = mCurrentScaleMin;
                            zoomOut();
                        }
                        return true;
                    } else {
                        mStartPoint.set(event.getX(), event.getY());
                        mStatus = Status.DRAG;
                    }
                }

                lastClickTime = event.getEventTime();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                oldRate = mCurrentScale;
                mStatus = Status.NONE;
                break;
            default:
                break;
        }
        postInvalidate();
        return true;
    }

    public void releaseSource() {
        if (mBitmap != null) {
            mBitmap.recycle();
        }
        for (MarkObject object : markList) {
            if (object.getmBitmap() != null) {
                object.getmBitmap().recycle();
            }
        }
        if (mBitmapCurrent != null) {
            mBitmapCurrent.recycle();
        }
        if (mBitmapEnd != null) {
            mBitmapEnd.recycle();
        }
        if (mBitmapStart != null) {
            mBitmapStart.recycle();
        }
        if (mBitmapLocationCurrent != null) {
            mBitmapLocationCurrent.recycle();
        }

        markList.clear();
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        if (mBitmap != null) {
            mBitmap.recycle();
        }

        for (MarkObject object : markList) {
            if (object.getmBitmap() != null) {
                object.getmBitmap().recycle();
            }
        }
        markList.clear();
    }


    public int getCurrentLineId(){
        if(markList != null && markList.size() >0){
            return markList.get(0).mStationInfo.lineid;
        }
        return 0;
    }
    // 获得状态栏高度
    private int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
            return 75;
        }
    }

}
