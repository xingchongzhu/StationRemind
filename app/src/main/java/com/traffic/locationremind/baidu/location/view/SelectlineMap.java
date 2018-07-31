package com.traffic.locationremind.baidu.location.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.traffic.locationremind.manager.bean.StationInfo;

import java.util.List;

public class SelectlineMap extends TextView implements View.OnClickListener {

    private final static String TAG = "SelectlineMap";
    // 定义画笔
    private Paint mPaint;
    // 用于获取文字的宽和高
    private Rect mBounds;

    private List<StationInfo> list = null;
    private int TEXTHEIGHT = 3;
    private int PADDLEFT = 200;


    public void setStationList(List<StationInfo> list){
        this.list = list;
/*        int height = 0;
        if(list != null && list.size() >0){
            for(StationInfo stationInfo:list){
                String text = stationInfo.getCname();
                mPaint.getTextBounds(text, 0, text.length(), mBounds);
                float textHeight = mBounds.height()*3;
                height+= textHeight;
            }

            ViewGroup.LayoutParams params = this.getLayoutParams();
            params.height = height+30;
            setLayoutParams(params);
        }*/

        invalidate();
        //Log.d(TAG,"setStationList height = "+height+getHeight());
    }

    public SelectlineMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 初始化画笔、Rect
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBounds = new Rect();
        // 本控件的点击事件
        setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG,"onMeasure heightMeasureSpec = "+heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = 0;
        // 绘制一个填充色为蓝色的矩形
        //canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
        if(list != null && list.size() >0){
            mPaint.setColor(Color.BLACK);
            mPaint.setTextSize(50);
            mPaint.setStrokeWidth(5);

            for(StationInfo stationInfo:list){
                String text = stationInfo.getCname();
                mPaint.getTextBounds(text, 0, text.length(), mBounds);
                float textWidth = mBounds.width();
                float textHeight = mBounds.height()*3;
                height+= textHeight;
                Log.d(TAG,"onDraw height = "+height+" name "+stationInfo.getCname());
                // 绘制字符串
                canvas.drawText(text, PADDLEFT+30, height-textHeight/2, mPaint);
            }
            height+=30;
            mPaint.setStrokeWidth(20);
            mPaint.setColor(Color.BLUE);
            canvas.drawLine(PADDLEFT,0,PADDLEFT,height,mPaint);
            if(height != getHeight()){
                ViewGroup.LayoutParams params = this.getLayoutParams();
                params.height = height;
                setLayoutParams(params);
                invalidate();
            }
        }

        Log.d(TAG,"onDraw height = "+height+getHeight());
        //mPaint.setColor(Color.YELLOW);
    }

    @Override
    public void onClick(View v) {

        invalidate();
    }

}

