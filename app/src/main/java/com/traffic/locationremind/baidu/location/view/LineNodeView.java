package com.traffic.locationremind.baidu.location.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.traffic.location.remind.R;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.List;
import java.util.Map;

public class LineNodeView extends TextView implements View.OnClickListener {

    private final static String TAG = "SelectlineMap";
    private final int width = 150;
    private  int iconSize = 60;
    private final int paddingLine = 20;
    private final int lineHeight = 20;
    private final int norrowHeight = 2;
    private final int textSize = 50;
    private final int offset = 10;
    private Rect mBounds;
    // 定义画笔
    private Paint mPaint;
    // 用于获取文字的宽和高
    private Bitmap bitmap;
    private Bitmap start;

    private StationInfo mStationInfo;
    private int color = Color.LTGRAY;
    private Bitmap transFerBitmap;

    public StationInfo getStationInfo(){
        return mStationInfo;
    }

    public void setStartBitMap(Bitmap bitmap){
        if(this.start != null){
            this.start.recycle();
            this.start = null;
        }
        this.start = bitmap;
        invalidate();
    }

    public void setBitMap(Bitmap bitmap){
        if(this.bitmap != null){
            this.bitmap.recycle();
            this.bitmap = null;
        }
        this.bitmap = bitmap;
        invalidate();
    }

    public void setTransFerBitmap(Bitmap bitmap){
        if(this.transFerBitmap != null){
            this.transFerBitmap.recycle();
            this.transFerBitmap = null;
        }
        this.transFerBitmap = bitmap;
        invalidate();
    }


    public void setStation(StationInfo stationInfo){
        this.mStationInfo = stationInfo;
        color = stationInfo.colorId;
        iconSize = (int)getResources().getDimension(R.dimen.current_bitmap_siez);
        invalidate();
    }

    public LineNodeView(Context context){
        super(context);
        init();
    }

    public LineNodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        // 初始化画笔、Rect
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBounds = new Rect();
        // 本控件的点击事件
        setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.d(TAG,"onMeasure heightMeasureSpec = "+heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制一个填充色为蓝色的矩形
        int height = paddingLine;
        if(start != null){
            Matrix matrix = new Matrix();
            matrix.setScale(1.0f, 1.0f);
            matrix.postTranslate(getWidth()/2-start.getWidth()/2,height);
            canvas.drawBitmap(start,matrix,mPaint);
        }else if(bitmap != null){
            Matrix matrix = new Matrix();
            matrix.setScale(1.0f, 1.0f);
            matrix.postTranslate(getWidth()/2-bitmap.getWidth()/2,height);
            canvas.drawBitmap(bitmap,matrix,mPaint);
        }
        height+= iconSize;
        height+= 1.5*paddingLine;
        mPaint.setColor(color);
        mPaint.setStrokeWidth(lineHeight);
        canvas.drawLine(0,height,getWidth(),height,mPaint);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(norrowHeight);

        if(transFerBitmap != null){
            Matrix matrix = new Matrix();
            matrix.setScale(1.0f, 1.0f);
            matrix.postTranslate(getWidth()/2-transFerBitmap.getWidth()/2,height-transFerBitmap.getHeight()/2);
            canvas.drawBitmap(transFerBitmap,matrix,mPaint);
        }else{
            int narroWidth = getWidth()/11;
            int narroHeight = narroWidth/2;
            int start = getWidth()/2-narroWidth/4-offset;
            int end = start+narroWidth-offset;
            //方向箭头
            canvas.drawLine(start+narroWidth/2,height-narroHeight,end+offset,height+norrowHeight/2,mPaint);
            canvas.drawLine(start,height+norrowHeight/2,end+5,height+norrowHeight/2,mPaint);
            canvas.drawLine(start+narroWidth/2,height+narroHeight,end+offset,height+norrowHeight/2,mPaint);;
        }

        height+= 4*lineHeight;

        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(textSize);
        mPaint.setStrokeWidth(5);
        String text = mStationInfo.getCname();

        char s[] = text.toCharArray();
        int size = s.length;
        for(int i = 0 ;i < size ; i ++){
            String ss = Character.toString(s[i]);
            mPaint.getTextBounds(ss, 0, ss.length(), mBounds);
            int textWidth = mBounds.width();
            int textHeight = mBounds.height();
            canvas.drawText(ss, getWidth()/2-textWidth/2, height, mPaint);
            height+= paddingLine+textHeight;
        }
    }


    private void drawText(Canvas canvas,String text,float x,float y,int color,int textSize){
        mPaint.setColor(color);
        mPaint.setTextSize(textSize);
        mPaint.setStrokeWidth(5);
        canvas.drawText(text, x, y, mPaint);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(getContext(),mStationInfo.getCname(),Toast.LENGTH_SHORT).show();
        //invalidate();
    }

}

