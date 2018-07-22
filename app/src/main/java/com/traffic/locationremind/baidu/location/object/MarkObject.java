package com.traffic.locationremind.baidu.location.object;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.traffic.location.remind.R;
import com.traffic.locationremind.manager.bean.StationInfo;

public class MarkObject {

	private Bitmap mBitmap;
	private float mapX;
	private float mapY;
	private float x;
	private float y;
	private String name;
	private int colorId;
	public static int size = 30;
	public static int rectSizeHeight = 30;
	public static int rectSizewidth = 50;
	public static int ROWHEIGHT = 100;

	public final static int DIFF = 5;
	private MarkClickListener listener;
	private int lineid;
	private int currentsize;

	public StationInfo mStationInfo;
	public boolean isStartStation = false;
	public boolean isEndStation = false;
	public boolean isCurrentStation = false;
	public boolean isCurrentLocationStation = false;
	
	public int getLineid() {
		return lineid;
	}

	public void setLineid(int lineid) {
		this.lineid = lineid;
	}

	public int getCurrentsize() {
		return currentsize;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setCurrentsize(int currentsize) {
		this.currentsize = currentsize;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	private float radius;
	public MarkObject() {

	}
	
	public void setScale(Context context,float scale){
		if(mBitmap != null){
			mBitmap.recycle();
		}
		radius = radius*scale;
		currentsize = (int) (currentsize*scale);
		mBitmap = drawNewBitmap(context,radius,currentsize,currentsize,colorId);
	}

	/**
	* 往图片上写入文字、图片等内容
	*/
	private Bitmap drawNewBitmap(Context context,float radius,int width,int hight,int colorId) {	
		Bitmap bitmap = Bitmap.createBitmap(width, hight,  Bitmap.Config.ARGB_8888);
		bitmap.eraseColor(context.getResources().getColor(R.color.white));
		Canvas canvas = new Canvas(bitmap);  
		Paint paint = new Paint();  
		paint.setColor(colorId);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(radius/2);
		paint.setStyle(Paint.Style.STROKE); 
		canvas.drawCircle(width/2, hight/2, radius, paint);
		return bitmap;
	}

	public MarkObject(Bitmap mBitmap, float mapX, float mapY) {
		super();
		this.mBitmap = mBitmap;
		this.mapX = mapX;
		this.mapY = mapY;
	}

	public MarkObject(Bitmap mBitmap, int row, int cloume,String name) {
		super();
		this.mBitmap = mBitmap;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getColorId() {
		return colorId;
	}

	public void setColorId(int colorId) {
		this.colorId = colorId;
	}

	/**
	 * @return the mBitmap
	 */
	public Bitmap getmBitmap() {
		return mBitmap;
	}

	/**
	 * @param mBitmap
	 *            the mBitmap to set
	 */
	public void setmBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}

	/**
	 * @return the mapX
	 */
	public float getMapX() {
		return mapX;
	}

	/**
	 * @param mapX
	 *            the mapX to set
	 */
	public void setMapX(float mapX) {
		this.mapX = mapX;
	}

	/**
	 * @return the mapY
	 */
	public float getMapY() {
		return mapY;
	}

	/**
	 * @param mapY
	 *            the mapY to set
	 */
	public void setMapY(float mapY) {
		this.mapY = mapY;
	}
	
	public MarkClickListener getMarkListener() {
		return listener;
	}

	public void setMarkListener(MarkClickListener listener) {
		this.listener = listener;
	}

	public interface MarkClickListener {
		public void onMarkClick(int x, int y, MarkObject markObject);
	}

}
