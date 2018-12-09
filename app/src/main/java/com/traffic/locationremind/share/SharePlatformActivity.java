package com.traffic.locationremind.share;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.traffic.location.remind.R;
import com.traffic.locationremind.share.views.Item;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.common.ResContainer;
import com.umeng.socialize.shareboard.SnsPlatform;

/**
 * Created by wangfei on 2018/1/23.
 */

public class SharePlatformActivity extends BaseActivity {

    public ArrayList<SnsPlatform> platforms = new ArrayList<SnsPlatform>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("分享示例");
        setBackVisibily();
        initViews();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_ushareplatform;
    }
    private void initViews(){
        LinearLayout container = (LinearLayout)findViewById(R.id.platform_container);
        initPlatforms();
        for (final SnsPlatform platform:platforms){
            Item item = new Item(this);
            item.setIcon(ResContainer.getResourceId(this,"drawable",platform.mIcon));
            item.setName(platform.mShowWord);
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getDimensionPixelOffset(R.dimen.item_height));
            item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SharePlatformActivity.this,ShareDetailActivity.class);
                    intent.putExtra("platform",platform.mPlatform);
                    intent.putExtra("name",platform.mShowWord);
                    SharePlatformActivity.this.startActivity(intent);
                }
            });
            container.addView(item,lp);
        }


    }
    private void initPlatforms(){
        platforms.clear();
        platforms.add(SHARE_MEDIA.WEIXIN.toSnsPlatform());
        platforms.add(SHARE_MEDIA.WEIXIN_CIRCLE.toSnsPlatform());
        platforms.add(SHARE_MEDIA.WEIXIN_FAVORITE.toSnsPlatform());

    }
}
