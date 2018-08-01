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
    private final int lineHeight = 40;
    private final int norrowHeight = 5;
    private final int textSize = 60;
    private final int offset = 10;
    private Rect mBounds;
    // 定义画笔
    private Paint mPaint;
    // 用于获取文字的宽和高
    private Bitmap bitmap;

    private StationInfo mStationInfo;
    private int color = Color.LTGRAY;

    public void setBitMap(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    public void setStation(StationInfo stationInfo){
        this.mStationInfo = stationInfo;
        color = stationInfo.colorId;
        iconSize = (int)getResources().getDimension(R.dimen.line_node_width);
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
        if(bitmap != null){
            Matrix matrix = new Matrix();
            matrix.setScale(1.0f, 1.0f);
            matrix.postTranslate(getWidth()/2-bitmap.getWidth()/2,height);
            canvas.drawBitmap(bitmap,matrix,mPaint);
        }
        height+= iconSize;

        height+= paddingLine;
        mPaint.setColor(color);
        mPaint.setStrokeWidth(lineHeight);
        canvas.drawLine(0,height,getWidth(),height,mPaint);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(norrowHeight);

        int narroWidth = getWidth()/11;
        int narroHeight = narroWidth/2;
        int start = getWidth()/2-narroWidth/4-offset;
        int end = start+narroWidth-offset;

        //方向箭头
        canvas.drawLine(start+narroWidth/2,height-narroHeight,end+offset,height+norrowHeight/2,mPaint);
        canvas.drawLine(start,height+norrowHeight/2,end+5,height+norrowHeight/2,mPaint);
        canvas.drawLine(start+narroWidth/2,height+narroHeight,end+offset,height+norrowHeight/2,mPaint);;

        height+= 3*lineHeight;

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

        //Log.d(TAG,"onDraw height = "+height+getHeight());
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

