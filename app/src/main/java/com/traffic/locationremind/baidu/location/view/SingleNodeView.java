package com.traffic.locationremind.baidu.location.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.traffic.location.remind.R;
import com.traffic.locationremind.manager.bean.StationInfo;

public class SingleNodeView extends TextView{

    private final static String TAG = "SelectlineMap";
    private final int width = 150;
    private  int iconSize = 60;
    private  int paddingLineTop = 20;
    private  int paddingLineBottom = 20;
    private  int lineHeight = 40;
    private Rect mBounds;
    // 定义画笔
    private Paint mPaint;
    // 用于获取文字的宽和高
    private Bitmap bitmap;

    private StationInfo mStationInfo;
    private int color = Color.LTGRAY;
    private Bitmap transFerBitmap;
    private int heightBitmap = 0;

    public void setColor(int color){
        this.color = color;
    }
    public StationInfo getStationInfo(){
        return mStationInfo;
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

    public SingleNodeView(Context context){
        super(context);
        init();
    }

    public SingleNodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        // 初始化画笔、Rect
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBounds = new Rect();
        iconSize = getContext().getResources().getDimensionPixelSize(R.dimen.single_node_bitmap_size);
        paddingLineTop = getContext().getResources().getDimensionPixelSize(R.dimen.single_node_padding_top);
        paddingLineBottom = getContext().getResources().getDimensionPixelSize(R.dimen.single_node_padding_bottom);
        lineHeight = getContext().getResources().getDimensionPixelSize(R.dimen.node_height);
        heightBitmap = getContext().getResources().getDimensionPixelSize(R.dimen.current_bitmap_node_height);
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
        mPaint.setStrokeWidth(lineHeight);
        int height = paddingLineTop;

        height+=heightBitmap;
        height+=paddingLineTop;
        canvas.drawLine(0,height,getWidth(),height,mPaint);
        int offert = 10;
        int center = getWidth()/2 -iconSize/2 +offert;
        if(transFerBitmap != null){
            Matrix matrix = new Matrix();
            matrix.setScale(1.0f, 1.0f);
            matrix.postTranslate(getWidth()/2-transFerBitmap.getWidth()/2,height-transFerBitmap.getHeight()/2);
            canvas.drawBitmap(transFerBitmap,matrix,mPaint);
        }else{
            canvas.drawCircle(center,height,iconSize,mPaint);
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle(center,height,iconSize/5*4,mPaint);
        }
        if(bitmap != null){
            Matrix matrix = new Matrix();
            matrix.setScale(1.0f, 1.0f);
            matrix.postTranslate(getWidth()/2-bitmap.getWidth()/2,paddingLineTop);
            canvas.drawBitmap(bitmap,matrix,mPaint);
        }
    }

}

