package com.traffic.locationremind.baidu.location.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.traffic.location.remind.R;
import com.traffic.locationremind.manager.bean.StationInfo;

public class ColorNodeView extends TextView {

    private final static String TAG = "SelectlineMap";
    private int width;
    private int height;
    private int padding = 10;
    // 定义画笔
    private Paint mPaint;

    private int color = Color.LTGRAY;

    public void setColorId(int color){
        this.color = color;
    }

    public ColorNodeView(Context context){
        super(context);
        init();
    }

    public ColorNodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        // 初始化画笔、Rect
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        width = (int)getContext().getResources().getDimension(R.dimen.color_line_width);
        height = (int)getContext().getResources().getDimension(R.dimen.color_line_height);
        padding = (int)getResources().getDimension(R.dimen.single_node_padding_top);
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
        mPaint.setColor(color);
        mPaint.setStrokeWidth(height);
        canvas.drawLine(getWidth()/3,getHeight()/2-height/2+padding,getWidth()/3*2,getHeight()/2-height/2+padding,mPaint);
    }

}

