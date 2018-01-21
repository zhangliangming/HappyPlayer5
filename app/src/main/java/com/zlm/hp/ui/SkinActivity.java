package com.zlm.hp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.R;
import com.zlm.hp.constants.PreferencesConstants;

import base.widget.SwipeBackLayout;

public class SkinActivity extends BaseActivity {

    private SwipeBackLayout mSwipeBackLayout;


    @Override
    protected int setContentViewId() {
        return R.layout.activity_skin;
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
        titleView.setText(mContext.getString(R.string.menu_skin_peeler));

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
        return R.id.skin_layout;
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.finish();
    }
}
