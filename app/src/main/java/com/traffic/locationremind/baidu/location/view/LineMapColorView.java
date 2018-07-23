package com.traffic.locationremind.baidu.location.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.object.MarkObject;

import java.util.ArrayList;
import java.util.List;

public class LineMapColorView extends View {

    private static final String TAG = LineMapColorView.class.getSimpleName();

    public static final int ROWMAXCOUNT = 4;// 一行最多点

    public static final int MAXSCALE = 3;

    private static final long DOUBLE_CLICK_TIME_SPACE = 300;
    public final static int ROWCOLORHEIGHT = 80;
    private float windowWidth, windowHeight;

    private Bitmap mBitmap;
    private Paint mPaint;

    //private PointF mStartPoint, mapCenter;// mapCenter表示地图中心在屏幕上的坐标

    private Context context;

    private List<MarkObject> markList = new ArrayList<MarkObject>();

    public LineMapColorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public LineMapColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public LineMapColorView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
    }

    private void init(Context context) {
        this.context = context;

        // 获取屏幕的宽和高
        windowWidth = getResources().getDisplayMetrics().widthPixels-context.getResources().getDimension(R.dimen.btn_size)-
                context.getResources().getDimension(R.dimen.magin_left);
        MarkObject.rectSizewidth = (int)windowWidth/ROWMAXCOUNT/3;
        MarkObject.rectSizeHeight = (int)MarkObject.rectSizewidth/2;
        windowHeight = getResources().getDisplayMetrics().heightPixels;
        mPaint = new Paint();
    }

    public float getViewWidth() {
        return windowWidth;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        // 设置最小缩放为铺满屏幕，最大缩放为最小缩放的4倍
        /*mCurrentScaleMin = Math.min(windowHeight / mBitmap.getHeight(),
				windowWidth / mBitmap.getWidth());
		mCurrentScale = mCurrentScaleMin;
		mapCenter.set(mBitmap.getWidth() * mCurrentScale / 2,
				mBitmap.getHeight() * mCurrentScale / 2);*/
    }

    /**
     * 为当前地图添加标记
     *
     * @param object
     */
    public void addMark(MarkObject object) {
        markList.add(object);
    }

    // 处理点击标记的事件
    private void clickAction(MotionEvent event) {

        int clickX = (int) event.getX();
        int clickY = (int) event.getY();

        for (MarkObject object : markList) {
            Bitmap location = object.getmBitmap();
            // 判断当前object是否包含触摸点，在这里为了得到更好的点击效果，我将标记的区域放大了
            if (object.getX() - object.getCurrentsize() <= clickX
                    && object.getX() + location.getWidth() + object.getCurrentsize() >= clickX
                    && object.getY() + location.getHeight() + object.getCurrentsize() >= clickY
                    && object.getY() - object.getCurrentsize() < clickY) {
                if (object.getMarkListener() != null) {
                    object.getMarkListener().onMarkClick(clickX, clickY,
                            object);
                }
                break;
            }

        }

    }

    // 计算两个触摸点的距离
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
		/*switch (event.getAction()) {

		case MotionEvent.ACTION_POINTER_DOWN:

			break;
		case MotionEvent.ACTION_UP:
			clickAction(event);
		default:
			break;
		}*/
        clickAction(event);
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        try {
            if (canvas != null && mBitmap != null) {
                canvas.drawColor(Color.WHITE);

                canvas.drawBitmap(mBitmap, 0, 0, mPaint);
                mPaint.setStrokeWidth(1);
                int textSize = (int) context.getResources().getDimension(R.dimen.text_size);
                mPaint.setTextSize(textSize);
                mPaint.setColor(context.getResources().getColor(R.color.black));
                for (MarkObject object : markList) {
                    Bitmap location = object.getmBitmap();
                    canvas.drawBitmap(location, object.getX(), object.getY(), mPaint);
                    int length = (int) (object.getName().length())+4;
                    canvas.drawText(object.getLineid()+object.getName(), object.getX() - length, object.getY() + object.getCurrentsize() * 2f, mPaint);
                }
            }
        } catch (Exception e) {

        }
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
        markList.clear();
        postInvalidate();
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();
        releaseSource();
    }

}
