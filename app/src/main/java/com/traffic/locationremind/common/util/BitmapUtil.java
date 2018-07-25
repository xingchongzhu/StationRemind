/**
 * Copyright (C) 2015~2050 by foolstudio. All rights reserved.
 * 
 * ��Դ�ļ��д��벻����������������ҵ��;�����߱�������Ȩ��
 * 
 * ���ߣ�������
 * 
 * �������䣺foolstudio@qq.com
 * 
*/

package com.traffic.locationremind.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import com.traffic.location.remind.R;

import java.io.*;
import java.util.ArrayList;

public class BitmapUtil {
	/**
	 * 往图片上写入文字、图片等内容
	 */
	public static Bitmap drawNewBitmap(Context context, float radius, int width, int hight, int colorId, boolean canTransfer) {
		Bitmap bitmap = Bitmap.createBitmap(width, hight, Bitmap.Config.ARGB_8888);
		bitmap.eraseColor(context.getResources().getColor(R.color.white));
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setColor(colorId);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(radius / 2);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawCircle(width / 2, hight / 2, radius, paint);
		if (canTransfer) {
			paint.setStrokeWidth(2);
			paint.setColor(Color.BLACK);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(width / 2, hight / 2, radius / 2, paint);
		}
		return bitmap;
	}

	/**
	 * 往图片上写入文字、图片等内容
	 */
	public static Bitmap drawNewColorBitmap(int width, int hight, int colorId) {
		Bitmap bitmap = Bitmap.createBitmap(width, hight, Bitmap.Config.ARGB_8888);
		bitmap.eraseColor(colorId);
		return bitmap;
	}

}
