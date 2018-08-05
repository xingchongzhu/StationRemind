package com.traffic.locationremind.baidu.location.view;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.object.MarkObject;
import com.traffic.locationremind.manager.bean.LineInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemindLineColorView extends View {

    private static final String TAG = RemindLineColorView.class.getSimpleName();

    public static final int ROWMAXCOUNT = 3;// 一行最多点
    private static final int padding = 50;
    private static final int rectSize = 50;
    private static final int textSize = 60;
    private Rect mBound = new Rect();
    private Paint mPaint;

    Map<Integer, LineInfo> lineInfoMap;


    public RemindLineColorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
    }

    public void setLineInfoMap(Map<Integer, LineInfo> lineInfoMap) {
        this.lineInfoMap = lineInfoMap;
        invalidate();
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
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        if (lineInfoMap != null) {


            canvas.drawColor(Color.WHITE);
            int height = 0;
            mPaint.setTextSize(textSize);
            mPaint.setStrokeWidth(3);
            Rect rect = new Rect();
            int n = 0;
            for (Map.Entry<Integer, LineInfo> entry : lineInfoMap.entrySet()) {

                mPaint.setColor(entry.getValue().colorid);

                if (n % 2 == 0) {
                    height += padding;
                    rect.left = padding;
                    rect.top = height;
                    rect.right = rect.left + rectSize;
                    rect.bottom = rect.top + rectSize;
                    canvas.drawRect(rect, mPaint);
                } else {
                    rect.left = getWidth() / 2;
                    if (rect.width() + 2 * padding > getWidth() / 2) {
                        rect.left = rect.width() + 2 * padding;
                    }
                    rect.top = height;
                    rect.right = rect.left + rectSize;
                    rect.bottom = rect.top + rectSize;
                    canvas.drawRect(rect, mPaint);
                    height += rectSize;
                }
                mPaint.setColor(getContext().getResources().getColor(R.color.black));
                String text = String.format(getResources().getString(R.string.line_tail, entry.getKey() + "") + "(" + entry.getValue().getLinename() + ")");
                mPaint.getTextBounds(text, 0, text.length(), mBound);
                int textHeight = mBound.height();
                canvas.drawText(text, rect.right + padding / 2, rect.top + rectSize / 2 + textHeight / 3, mPaint);
                n++;
            }
            if (n % 2 == 0)
                height += padding;
            else
                height += 2 * padding;
            if (height != getHeight()) {
                ViewGroup.LayoutParams params = this.getLayoutParams();
                params.height = height;
                setLayoutParams(params);
                invalidate();
            }
        }
    }
}
