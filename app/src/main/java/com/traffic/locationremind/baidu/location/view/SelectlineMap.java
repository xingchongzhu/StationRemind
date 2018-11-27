package com.traffic.locationremind.baidu.location.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.object.LineObject;
import com.traffic.locationremind.common.util.CommonFuction;
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import com.traffic.locationremind.manager.database.DataManager;
//import org.apache.poi.hssf.util.HSSFColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectlineMap extends TextView implements View.OnClickListener {

    private final static String TAG = "SelectlineMap";
    // 定义画笔
    private Paint mPaint;
    // 用于获取文字的宽和高
    private Rect mBounds;

    private List<StationInfo> list = null;
    private Map<Integer, LineInfo> mLineInfoList;
    private LineObject lineObject;
    private int PADDLEFT = 0;
    private int LINEPADDLEFT = 0;
    private int paddingTop = 0;
    private int radio = 10;
    private int textPadding = 100;
    ///private String lineTial = "";
    private int HEIGHTSCALE = 3;
    private int normalTextSize = 45;
    private int changetTextSize = 45;
    private int hintTextSize = 50;
    private List<StationInfo> transferList = new ArrayList<>();
    private Map<Integer, String> lineDirection = new HashMap<>();
    DataManager dataManager;
    StationInfo preStationInfo;

    public void setStationList(LineObject lineObject) {
        if (lineObject.lineidList.size() > 0) {
            this.lineObject = lineObject;
            transferList = CommonFuction.getTransFerList(lineObject);
            int number = 0;
            int lineid = lineObject.lineidList.get(number);
            for (StationInfo stationInfo : lineObject.stationList) {
                if (preStationInfo != null && CommonFuction.containTransfer(transferList, stationInfo)) {
                    if (preStationInfo.pm < stationInfo.pm) {
                        lineDirection.put(preStationInfo.lineid, dataManager.getLineInfoList().get(preStationInfo.lineid).getReverse());
                    } else {
                        lineDirection.put(preStationInfo.lineid, dataManager.getLineInfoList().get(preStationInfo.lineid).getForwad());
                    }
                    if(number+1 < lineObject.lineidList.size()){
                        number++;
                    }
                    lineid = lineObject.lineidList.get(number);
                }
                StationInfo stationInfo1 = dataManager.getAllstations().get(stationInfo.getCname());
                if(stationInfo1.containLineid(lineid)){
                    stationInfo.lineid = lineid;
                }
                preStationInfo = stationInfo;
            }
            this.list = lineObject.stationList;

        }
        invalidate();
    }

    public LineObject getDataList() {
        return lineObject;
    }

    public void setLineInfoList(Map<Integer, LineInfo> mLineInfoList) {
        this.mLineInfoList = mLineInfoList;
    }

    public SelectlineMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 初始化画笔、Rect
        dataManager = DataManager.getInstance(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBounds = new Rect();
        PADDLEFT = (int) context.getResources().getDimension(R.dimen.remind_line_text_padding_left);

        LINEPADDLEFT = (int) context.getResources().getDimension(R.dimen.remind_line_map_padding_left);
        paddingTop = (int) context.getResources().getDimension(R.dimen.remind_line_map_padding_top);
        //lineTial = getResources().getString(R.string.line_tail);
        // 本控件的点击事件
        setOnClickListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //Log.d(TAG,"onMeasure heightMeasureSpec = "+heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        normalTextSize = (int)getContext().getResources().getDimension(R.dimen.normalTextSize);
        changetTextSize = normalTextSize;
        hintTextSize = (int)getContext().getResources().getDimension(R.dimen.hintTextSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int height = 0;
        // 绘制一个填充色为蓝色的矩形
        if (list != null && list.size() > 0) {
            Log.d(TAG, "------------start---------------");
            StationInfo preStationInfo = null;
            int size = list.size();

            for (int i = 0; i < size; i++) {
                StationInfo stationInfo = list.get(i);
                Log.d(TAG, "onDraw stationInfo.lineid = " + stationInfo.lineid + " name = " + stationInfo.getCname());
                String text = stationInfo.getCname();
                mPaint.setStrokeWidth(normalTextSize);
                mPaint.getTextBounds(text, 0, text.length(), mBounds);
                int textWidth = mBounds.width();
                int textHeight = mBounds.height() * HEIGHTSCALE;

                if (i == 0) {
                    height += 0.5 * textHeight;
                    //起点画圆
                    mPaint.setColor(getResources().getColor(R.color.green));
                    canvas.drawCircle(LINEPADDLEFT, height, radio, mPaint);
                    String start = getContext().getResources().getString(R.string.start_station);
                    //起点 绘制字符串
                    drawText(canvas, start + text + " (" + String.format(getResources().getString(R.string.station_number), size) + ")",
                            LINEPADDLEFT + radio * 2, height - radio / 2 + mBounds.height() / 2, getResources().getColor(R.color.green), changetTextSize);

                    String direction = lineDirection.get(stationInfo.lineid) + getResources().getString(R.string.direction);
                    //起点中间断开提醒
                    height += 0.5 * textHeight;
                    drawLine(canvas, LINEPADDLEFT, height, LINEPADDLEFT, height + textHeight, mLineInfoList.get(stationInfo.getLineid()).colorid);
                    drawText(canvas, dataManager.getLineInfoList().get(stationInfo.lineid).linename + " " + direction,
                            PADDLEFT + textPadding, height + textHeight / 2 + mBounds.height() / 2, getResources().getColor(R.color.blue), normalTextSize);
                    height += 0.7 * textHeight;
                } else {
                    height += textHeight;
                }
                if (preStationInfo == null) {
                    //第一个站绘制
                    drawLine(canvas, LINEPADDLEFT, height, LINEPADDLEFT, height + textHeight, mLineInfoList.get(stationInfo.getLineid()).colorid);
                    drawText(canvas, stationInfo.getCname(), PADDLEFT + textPadding, height + textHeight / 2 + mBounds.height() / 2, Color.BLACK, normalTextSize);
                    // }else if(preStationInfo.lineid == stationInfo.lineid){
                } else if (i != size-1 && stationInfo.canTransfer() && CommonFuction.containTransfer(transferList, stationInfo)) {
                    //换乘中间断开
                    drawLine(canvas, LINEPADDLEFT, height, LINEPADDLEFT, height + textHeight, mLineInfoList.get(preStationInfo.getLineid()).colorid);
                    drawText(canvas, stationInfo.getCname(), PADDLEFT + textPadding, height + textHeight / 2 + mBounds.height() / 2, Color.BLACK, normalTextSize);
                    height += 1.5 * textHeight;

                    //换乘上一条线
                    String string = getResources().getString(R.string.change_next_line);
                    drawText(canvas, stationInfo.getCname() + " " + String.format(string, dataManager.getLineInfoList().get(stationInfo.lineid).linename),
                            LINEPADDLEFT, height, getResources().getColor(R.color.brown), hintTextSize);
                    height += 0.5 * textHeight;

                    mPaint.setStrokeWidth(normalTextSize);
                    mPaint.getTextBounds(text, 0, text.length(), mBounds);
                    //换乘线路提醒
                    drawLine(canvas, LINEPADDLEFT, height, LINEPADDLEFT, height + textHeight, mLineInfoList.get(stationInfo.getLineid()).colorid);
                    String direction = lineDirection.get(stationInfo.lineid) + getResources().getString(R.string.direction);
                    drawText(canvas, dataManager.getLineInfoList().get(stationInfo.lineid).linename + " " + direction, PADDLEFT + textPadding,
                            height + textHeight / 2 + mBounds.height() / 2, getResources().getColor(R.color.blue), normalTextSize);
                    height += textHeight;

                    //换乘当前线路
                    drawLine(canvas, LINEPADDLEFT, height, LINEPADDLEFT, height + textHeight, mLineInfoList.get(stationInfo.getLineid()).colorid);
                    drawText(canvas, stationInfo.getCname(), PADDLEFT + textPadding, height + textHeight / 2 + mBounds.height() / 2, Color.BLACK, normalTextSize);
                } else {
                    //不用换乘直接画
                    drawLine(canvas, LINEPADDLEFT, height, LINEPADDLEFT, height + textHeight, mLineInfoList.get(stationInfo.getLineid()).colorid);
                    drawText(canvas, stationInfo.getCname(), PADDLEFT + textPadding, height + textHeight / 2 + mBounds.height() / 2, Color.BLACK, normalTextSize);
                }
                if (i == size - 1) {
                    //终点画圆
                    String end = getContext().getResources().getString(R.string.end_station);
                    height += 2 * textHeight;
                    mPaint.setColor(getResources().getColor(R.color.red));
                    canvas.drawCircle(LINEPADDLEFT, height, radio, mPaint);
                    // 绘制字符串
                    drawText(canvas, end + stationInfo.getCname(), LINEPADDLEFT + radio * 2,
                            height - radio / 2 + mBounds.height() / 2, getResources().getColor(R.color.red), changetTextSize);
                    height += textHeight;
                }
                preStationInfo = stationInfo;
            }

            if (height != getHeight()) {
                ViewGroup.LayoutParams params = this.getLayoutParams();
                params.height = height;
                setLayoutParams(params);
                invalidate();
            }
            Log.d(TAG, "-----------end----------------");
        }
    }

    private void drawLine(Canvas canvas, int left, int top, int right, int bottom, int color) {
        int offext = 10;
        mPaint.setStrokeWidth(getContext().getResources().getDimension(R.dimen.line_height_size));
        mPaint.setColor(color);
        canvas.drawLine(left, top - offext, right, bottom, mPaint);
        mPaint.setColor(Color.WHITE);
        int radio = 5;
        canvas.drawCircle(left, top + (bottom - top) / 2 + radio / 2, radio, mPaint);
    }

    private void drawText(Canvas canvas, String text, float x, float y, int color, int textSize) {
        mPaint.setColor(color);
        mPaint.setTextSize(textSize);
        mPaint.setStrokeWidth(5);
        canvas.drawText(text, x, y, mPaint);
    }

    @Override
    public void onClick(View v) {
    }

}

