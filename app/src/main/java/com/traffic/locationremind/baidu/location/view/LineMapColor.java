package com.traffic.locationremind.baidu.location.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.object.MarkObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LineMapColor extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = LineMapColor.class.getSimpleName();

	public static final int ROWMAXCOUNT = 4;// 一行最多点

	public static final int MAXSCALE = 3;

	private static final long DOUBLE_CLICK_TIME_SPACE = 300;

	private float mCurrentScaleMax;
	private float mCurrentScale;
	private float mCurrentScaleMin;

	private float windowWidth, windowHeight;

	private Bitmap mBitmap;
	private Paint mPaint;

	private PointF mStartPoint, mapCenter;// mapCenter表示地图中心在屏幕上的坐标
	private long lastClickTime;// 记录上一次点击屏幕的时间，以判断双击事件
	private Status mStatus = Status.NONE;

	private float oldRate = 1;
	private float oldDist = 1;
	private float offsetX, offsetY;

	private boolean isShu = true;
	private float pointDistance;
	private Context context;

	private enum Status {
		NONE, ZOOM, DRAG
	};

	private List<MarkObject> markList = new ArrayList<MarkObject>();

	public LineMapColor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public LineMapColor(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public LineMapColor(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		// 获取屏幕的宽和高
		windowWidth = getResources().getDisplayMetrics().widthPixels;
		windowHeight = getResources().getDisplayMetrics().heightPixels
				- getStatusBarHeight();
		pointDistance = windowWidth / ROWMAXCOUNT;
		mPaint = new Paint();

		mStartPoint = new PointF();
		mapCenter = new PointF();
	}

	public float getViewWidth() {
		return windowWidth;
	}

	public void setBitmap(Bitmap bitmap) {
		this.mBitmap = bitmap;
		// 设置最小缩放为铺满屏幕，最大缩放为最小缩放的4倍
		mCurrentScaleMin = Math.min(windowHeight / mBitmap.getHeight(),
				windowWidth / mBitmap.getWidth());
		mCurrentScale = mCurrentScaleMin;
		mCurrentScaleMax = mCurrentScaleMin * MAXSCALE;
		mapCenter.set(mBitmap.getWidth() * mCurrentScale / 2,
				mBitmap.getHeight() * mCurrentScale / 2);
		float bitmapRatio = mBitmap.getHeight() / mBitmap.getWidth();
		windowHeight = mBitmap.getHeight();
		float winRatio = windowHeight / windowWidth;
		// 判断屏幕铺满的情况，isShu为true表示屏幕横向被铺满，为false表示屏幕纵向被铺满
		if (bitmapRatio <= winRatio) {
			isShu = true;
		} else {
			isShu = false;
		}
		draw();
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

	}

	// 计算两个触摸点的距离
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	private void draw() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Canvas canvas = getHolder().lockCanvas();
				if (canvas != null && mBitmap != null) {
					canvas.drawColor(Color.WHITE);
					Matrix matrix = new Matrix();
					matrix.setScale(mCurrentScale, mCurrentScale,
							mBitmap.getWidth() / 2, mBitmap.getHeight() / 2);
					matrix.postTranslate(mapCenter.x - mBitmap.getWidth() / 2,
							mapCenter.y - mBitmap.getHeight() / 2);
					canvas.drawBitmap(mBitmap, matrix, mPaint);
					mPaint.setStrokeWidth(1);
					int textSize = (int) context.getResources().getDimension(R.dimen.text_size);
					mPaint.setTextSize(textSize);
					mPaint.setColor(Color.WHITE);
					for (MarkObject object : markList) {
						Bitmap location = object.getmBitmap();
						matrix.setScale(1.0f, 1.0f);
						// 使用Matrix使得Bitmap的宽和高发生变化，在这里使用的mapX和mapY都是相对值
						float x = mapCenter.x - location.getWidth() / 2
								- mBitmap.getWidth() * mCurrentScale
								/ 2 + mBitmap.getWidth()
								* object.getMapX() * mCurrentScale;
						float y = mapCenter.y - location.getHeight()
								- mBitmap.getHeight() * mCurrentScale
								/ 2 + mBitmap.getHeight()
								* object.getMapY() * mCurrentScale;
						matrix.postTranslate(x,y);
						object.setX(x);
						object.setY(y);
						canvas.drawBitmap(location, matrix, mPaint);
						int length = (int) ((object.getName().length()+object.getLineid())*2);
						canvas.drawText(object.getLineid()+object.getName(), x-length, y+object.getCurrentsize()*2f, mPaint);
					}

				}
				if (canvas != null) {
					getHolder().unlockCanvasAndPost(canvas);
				}
			}
		}).start();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction() & MotionEvent.ACTION_MASK) {

		case MotionEvent.ACTION_POINTER_DOWN:

			break;

		case MotionEvent.ACTION_MOVE:

			break;
		case MotionEvent.ACTION_UP:
			clickAction(event);
		default:
			break;
		}

		return true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		draw();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (mBitmap != null) {
			mBitmap.recycle();
		}
		for (MarkObject object : markList) {
			if (object.getmBitmap() != null) {
				object.getmBitmap().recycle();
			}
		}
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
