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

public class RemindLineColorView extends View {

    private static final String TAG = RemindLineColorView.class.getSimpleName();

    public static final int ROWMAXCOUNT = 3;// 一行最多点

    private float windowWidth, windowHeight;

    private Paint mPaint;
    private Context context;

    private List<MarkObject> markList = new ArrayList<MarkObject>();

    public RemindLineColorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public RemindLineColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public RemindLineColorView(Context context) {
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

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
                canvas.drawColor(Color.WHITE);
                
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

}
