package com.zlm.hp.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.R;
import com.zlm.hp.constants.PreferencesConstants;

import base.widget.SwipeBackLayout;

/**
 * @Description: 关于界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/27 21:17
 * @Throws:
 */
public class AboutActivity extends BaseActivity {
    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;


    @Override
    protected int setContentViewId() {
        return R.layout.activity_about;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        //
        mSwipeBackLayout = findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setmSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {
            @Override
            public void finishView() {
                finish();
                overridePendingTransition(0, 0);
            }
        });
        boolean shadowEnable = getIntent().getBooleanExtra(PreferencesConstants.shadowEnable_KEY, true);
        mSwipeBackLayout.setShadowEnable(shadowEnable);

        TextView titleView = findViewById(R.id.title);
        titleView.setText(mContext.getString(R.string.about));

        //返回
        RelativeLayout backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.finish();

            }
        });
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {

    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }

    @Override
    public int setStatusBarParentView() {
        return R.id.about_layout;
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.finish();
    }
}
