/*
package com.traffic.locationremind.share.utils;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.tencent.mm.opensdk.modelmsg.*;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.traffic.location.remind.R;
import com.traffic.locationremind.baidu.location.activity.MainActivity;

import java.io.File;

public class WeiXinUtil {
    private static final int THUMB_SIZE = 150;
    private static final int MMAlertSelect1 = 0;
    private static final int MMAlertSelect2 = 1;
    private static final int MMAlertSelect3 = 2;

    public static void shareText(MainActivity activity, final int mTargetScene, final IWXAPI api) {
        final EditText editor = new EditText(activity);
        editor.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        editor.setText(R.string.send_text_default);
        MMAlert.showAlert(activity, "send text", editor, activity.getString(R.string.app_share), activity.getString(R.string.app_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = editor.getText().toString();
                if (text == null || text.length() == 0) {
                    return;
                }

                WXTextObject textObj = new WXTextObject();
                textObj.text = text;

                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = textObj;
                // msg.title = "Will be ignored";
                msg.description = text;

                // ����һ��Req
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = buildTransaction("text"); // transaction�ֶ�����Ψһ��ʶһ������
                req.message = msg;
                req.scene = mTargetScene;

                api.sendReq(req);
                //finish();
            }
        }, null);
    }

    public static void shareImageLocal(final MainActivity activity, final int mTargetScene, final IWXAPI api, final String path) {
        MMAlert.showAlert(activity, activity.getString(R.string.send_img),
                activity.getResources().getStringArray(R.array.send_img_item),
                null, new MMAlert.OnAlertSelectId() {

                    @Override
                    public void onClick(int whichButton) {
                        switch (whichButton) {
                            case MMAlertSelect1: {
                                Bitmap bmp = BitmapFactory.decodeResource(activity.getResources(), R.drawable.send_img);
                                WXImageObject imgObj = new WXImageObject(bmp);

                                WXMediaMessage msg = new WXMediaMessage();
                                msg.mediaObject = imgObj;

                                Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
                                bmp.recycle();
                                msg.thumbData = Util.bmpToByteArray(thumbBmp, true);  // ��������ͼ

                                SendMessageToWX.Req req = new SendMessageToWX.Req();
                                req.transaction = buildTransaction("img");
                                req.message = msg;
                                req.scene = mTargetScene;
                                api.sendReq(req);

                                //finish();
                                break;
                            }
                            case MMAlertSelect2: {
                                File file = new File(path);
                                if (!file.exists()) {
                                    String tip = activity.getString(R.string.send_img_file_not_exist);
                                    Toast.makeText(activity, tip + " path = " + path, Toast.LENGTH_LONG).show();
                                    break;
                                }

                                WXImageObject imgObj = new WXImageObject();
                                imgObj.setImagePath(path);

                                WXMediaMessage msg = new WXMediaMessage();
                                msg.mediaObject = imgObj;

                                Bitmap bmp = BitmapFactory.decodeFile(path);
                                Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
                                bmp.recycle();
                                msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

                                SendMessageToWX.Req req = new SendMessageToWX.Req();
                                req.transaction = buildTransaction("img");
                                req.message = msg;
                                req.scene = mTargetScene;
                                api.sendReq(req);

                                //finish();
                                break;
                            }
                            default:
                                break;
                        }
                    }

                });
    }

    public static void shareImageNet(final MainActivity activity, final int mTargetScene, final IWXAPI api, final String path) {
    }

    public static void shareMusic(final MainActivity activity, final int mTargetScene, final IWXAPI api, final String path) {
        MMAlert.showAlert(activity, activity.getString(R.string.send_music),
                activity.getResources().getStringArray(R.array.send_music_item),
                null, new MMAlert.OnAlertSelectId() {

                    @Override
                    public void onClick(int whichButton) {
                        switch (whichButton) {
                            case MMAlertSelect1: {
                                WXMusicObject music = new WXMusicObject();
                                //music.musicUrl = "http://www.baidu.com";
                                music.musicUrl = "http://staff2.ustc.edu.cn/~wdw/softdown/index.asp/0042515_05.ANDY.mp3";
                                //music.musicUrl="http://120.196.211.49/XlFNM14sois/AKVPrOJ9CBnIN556OrWEuGhZvlDF02p5zIXwrZqLUTti4o6MOJ4g7C6FPXmtlh6vPtgbKQ==/31353278.mp3";

                                WXMediaMessage msg = new WXMediaMessage();
                                msg.mediaObject = music;
                                msg.title = "Music Title Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long";
                                msg.description = "Music Album Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long";

                                Bitmap bmp = BitmapFactory.decodeResource(activity.getResources(), R.drawable.send_music_thumb);
                                Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
                                bmp.recycle();
                                msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

                                SendMessageToWX.Req req = new SendMessageToWX.Req();
                                req.transaction = buildTransaction("music");
                                req.message = msg;
                                req.scene = mTargetScene;
                                api.sendReq(req);

                                //finish();
                                break;
                            }
                            case MMAlertSelect2: {
                                WXMusicObject music = new WXMusicObject();
                                music.musicLowBandUrl = "http://www.qq.com";

                                WXMediaMessage msg = new WXMediaMessage();
                                msg.mediaObject = music;
                                msg.title = "Music Title";
                                msg.description = "Music Album";

                                Bitmap bmp = BitmapFactory.decodeResource(activity.getResources(), R.drawable.send_music_thumb);
                                Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
                                bmp.recycle();
                                msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

                                SendMessageToWX.Req req = new SendMessageToWX.Req();
                                req.transaction = buildTransaction("music");
                                req.message = msg;
                                req.scene = mTargetScene;
                                api.sendReq(req);

                                //finish();
                                break;
                            }
                            default:
                                break;
                        }
                    }
                });
    }

    public static void shareVideo(final MainActivity activity, final int mTargetScene, final IWXAPI api, final String path) {
        MMAlert.showAlert(activity, activity.getString(R.string.send_video),
                activity.getResources().getStringArray(R.array.send_video_item),
                null, new MMAlert.OnAlertSelectId(){

                    @Override
                    public void onClick(int whichButton) {
                        switch(whichButton){
                            case MMAlertSelect1: {
                                WXVideoObject video = new WXVideoObject();
                                video.videoUrl = "http://www.qq.com";

                                WXMediaMessage msg = new WXMediaMessage(video);
                                msg.title = "Video Title Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long";
                                msg.description = "Video Description Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long Very Long";
                                Bitmap bmp = BitmapFactory.decodeResource(activity.getResources(), R.drawable.send_music_thumb);
                                Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
                                bmp.recycle();
                                msg.thumbData = Util.bmpToByteArray(thumbBmp, true);

                                SendMessageToWX.Req req = new SendMessageToWX.Req();
                                req.transaction = buildTransaction("video");
                                req.message = msg;
                                req.scene = mTargetScene;
                                api.sendReq(req);

                                //finish();
                                break;
                            }
                            case MMAlertSelect2: {
                                WXVideoObject video = new WXVideoObject();
                                video.videoLowBandUrl = "http://www.qq.com";

                                WXMediaMessage msg = new WXMediaMessage(video);
                                msg.title = "Video Title";
                                msg.description = "Video Description";

                                SendMessageToWX.Req req = new SendMessageToWX.Req();
                                req.transaction = buildTransaction("video");
                                req.message = msg;
                                req.scene = mTargetScene;
                                api.sendReq(req);

                                //finish();
                                break;
                            }
                            default:
                                break;
                        }
                    }
                });
    }

    public static void shareTextAndImage(final MainActivity activity, final int mTargetScene, final IWXAPI api, final String path) {

    }
    public static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}
*/
