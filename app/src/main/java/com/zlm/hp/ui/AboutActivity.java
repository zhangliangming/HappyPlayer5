package com.zlm.hp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.libs.widget.SwipeBackLayout;

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
    protected void contentViewFinish(View contentView) {
        //
        mSwipeBackLayout = contentView.findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setContentView(R.layout.activity_about_layout);
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        mSwipeBackLayout.setSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {

            @Override
            public void finishActivity() {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        TextView titleView = findViewById(R.id.title);
        titleView.setText("关于");

        //返回
        RelativeLayout backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();

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
        mSwipeBackLayout.closeView();
    }
}
