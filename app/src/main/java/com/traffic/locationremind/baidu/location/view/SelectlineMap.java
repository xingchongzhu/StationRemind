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
import com.traffic.locationremind.manager.bean.LineInfo;
import com.traffic.locationremind.manager.bean.StationInfo;
import org.apache.poi.hssf.util.HSSFColor;

import java.util.List;
import java.util.Map;

public class SelectlineMap extends TextView implements View.OnClickListener {

    private final static String TAG = "SelectlineMap";
    // 定义画笔
    private Paint mPaint;
    // 用于获取文字的宽和高
    private Rect mBounds;

    private List<StationInfo> list = null;
    private  Map<Integer,LineInfo> mLineInfoList;;
    private int PADDLEFT = 0;
    private int paddingTop = 0;
    private int radio = 20;
    private int textPadding = 100;
    private String lineTial = "";
    private int HEIGHTSCALE = 3;
    private int normalTextSize = 50;
    private int changetTextSize = 70;
    private int hintTextSize = 60;

    public void setStationList(List<StationInfo> list){
        this.list = list;
        invalidate();
    }

    public List<StationInfo> getDataList(){
        return list;
    }

   public void setLineInfoList(Map<Integer,LineInfo> mLineInfoList){
        this.mLineInfoList = mLineInfoList;
   }

    public SelectlineMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 初始化画笔、Rect
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBounds = new Rect();
        PADDLEFT = (int)context.getResources().getDimension(R.dimen.remind_line_map_padding_left);
        paddingTop = (int)context.getResources().getDimension(R.dimen.remind_line_map_padding_top);
        lineTial = getResources().getString(R.string.line_tail);
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
        int height = paddingTop;
        // 绘制一个填充色为蓝色的矩形
        //canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
        if(list != null && list.size() >0){
            Log.d(TAG,"------------start---------------");
            StationInfo preStationInfo = null;
            int size = list.size();

            for(int i = 0;i < size;i++){
                StationInfo stationInfo = list.get(i);
                Log.d(TAG,"onDraw stationInfo.lineid = "+stationInfo.lineid+" name = "+stationInfo.getCname());
                String text = stationInfo.getCname();
                mPaint.setStrokeWidth(normalTextSize);
                mPaint.getTextBounds(text, 0, text.length(), mBounds);
                int textWidth = mBounds.width();
                int textHeight = mBounds.height()*HEIGHTSCALE;
                height+=textHeight;
                if(i == 0){
                    //起点画圆
                    mPaint.setColor(getResources().getColor(R.color.green));
                    canvas.drawCircle(PADDLEFT,height,radio,mPaint);
                    String start = getContext().getResources().getString(R.string.start_station);
                    //起点 绘制字符串
                    drawText(canvas,start+text+" ("+String.format(getResources().getString(R.string.station_number),size)+")",PADDLEFT+radio*2,height-radio/2+mBounds.height()/2,getResources().getColor(R.color.green),changetTextSize);

                    textHeight = mBounds.height()*HEIGHTSCALE;
                    //起点中间断开提醒
                    height+=textHeight;
                    drawLine(canvas,PADDLEFT,height,PADDLEFT,height+textHeight,mLineInfoList.get(stationInfo.getLineid()).colorid);
                    drawText(canvas,String.format(lineTial,stationInfo.lineid),PADDLEFT+textPadding, height+textHeight/2+mBounds.height()/2,getResources().getColor(R.color.blue),normalTextSize);
                    height+=textHeight;
                }
                if(preStationInfo == null){
                    //第一个站绘制
                    drawLine(canvas,PADDLEFT,height,PADDLEFT,height+textHeight,mLineInfoList.get(stationInfo.getLineid()).colorid);
                    drawText(canvas,stationInfo.getCname(),PADDLEFT+textPadding, height+textHeight/2+mBounds.height()/2,Color.BLACK,normalTextSize);
                }else if(preStationInfo.lineid == stationInfo.lineid){
                    //不用换乘直接画
                    drawLine(canvas,PADDLEFT,height,PADDLEFT,height+textHeight,mLineInfoList.get(stationInfo.getLineid()).colorid);
                    drawText(canvas,stationInfo.getCname(),PADDLEFT+textPadding, height+textHeight/2+mBounds.height()/2,Color.BLACK,normalTextSize);
                }else {
                    //换乘中间断开
                    drawLine(canvas,PADDLEFT,height,PADDLEFT,height+textHeight,mLineInfoList.get(preStationInfo.getLineid()).colorid);
                    drawText(canvas,stationInfo.getCname(),PADDLEFT+textPadding, height+textHeight/2+mBounds.height()/2,Color.BLACK,normalTextSize);
                    height+= 2*textHeight;

                    //换乘上一条线
                    String string = getResources().getString(R.string.change_next_line);
                    drawText(canvas,stationInfo.getCname()+" "+String.format(string,stationInfo.lineid+""),PADDLEFT, height,getResources().getColor(R.color.brown),hintTextSize);
                    height+= textHeight;

                    mPaint.setStrokeWidth(normalTextSize);
                    mPaint.getTextBounds(text, 0, text.length(), mBounds);
                    //换乘线路提醒
                    drawLine(canvas,PADDLEFT,height,PADDLEFT,height+textHeight,mLineInfoList.get(stationInfo.getLineid()).colorid);
                    drawText(canvas,String.format(lineTial,stationInfo.lineid),PADDLEFT+textPadding, height+textHeight/2+mBounds.height()/2,getResources().getColor(R.color.blue),normalTextSize);
                    height+= textHeight;

                    //换乘当前线路
                    drawLine(canvas,PADDLEFT,height,PADDLEFT,height+textHeight,mLineInfoList.get(stationInfo.getLineid()).colorid);
                    drawText(canvas,stationInfo.getCname(),PADDLEFT+textPadding, height+textHeight/2+mBounds.height()/2,Color.BLACK,normalTextSize);
                }
                if(i == size -1){
                    //终点画圆
                    String end = getContext().getResources().getString(R.string.end_station);
                    height+=2*textHeight;
                    mPaint.setColor(getResources().getColor(R.color.red));
                    canvas.drawCircle(PADDLEFT,height,radio,mPaint);
                    // 绘制字符串
                    drawText(canvas,end+stationInfo.getCname(),PADDLEFT+radio*2,height-radio/2+mBounds.height()/2, getResources().getColor(R.color.red),changetTextSize);
                    height+=textHeight;
                }

                preStationInfo = stationInfo;

            }


            if(height != getHeight()){
                ViewGroup.LayoutParams params = this.getLayoutParams();
                params.height = height;
                setLayoutParams(params);
                invalidate();
            }
            Log.d(TAG,"-----------end----------------");
        }

        //Log.d(TAG,"onDraw height = "+height+getHeight());
    }

    private void drawLine(Canvas canvas,int left,int top,int right,int bottom,int color){
        int offext = 10;
        mPaint.setStrokeWidth(30);
        mPaint.setColor(color);
        canvas.drawLine(left,top-offext,right,bottom,mPaint);
        mPaint.setColor(Color.WHITE);
        int radio = 5;
        canvas.drawCircle(left,top+(bottom-top)/2+radio/2,radio,mPaint);
    }
    private void drawText(Canvas canvas,String text,float x,float y,int color,int textSize){
        mPaint.setColor(color);
        mPaint.setTextSize(textSize);
        mPaint.setStrokeWidth(5);
        canvas.drawText(text, x, y, mPaint);
    }

    @Override
    public void onClick(View v) {
    }

}

