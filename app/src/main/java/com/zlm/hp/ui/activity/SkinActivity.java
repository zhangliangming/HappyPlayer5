package com.zlm.hp.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.soundcloud.android.crop.Crop;
import com.zlm.hp.R;
import com.zlm.hp.constants.PreferencesConstants;

import base.widget.SwipeBackLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SkinActivity extends BaseActivity {
    @BindView(R.id.ll_green)
    LinearLayout llGreen;
    @BindView(R.id.ll_black)
    LinearLayout llBlack;
    @BindView(R.id.ll_custom_skin)
    LinearLayout llCustomSkin;
    @BindView(R.id.swipeback_layout)
    SwipeBackLayout swipebackLayout;


    @Override
    protected int setContentViewId() {
        return R.layout.activity_skin;
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
    protected void initViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        //
        swipebackLayout.setSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {
            @Override
            public void finishView() {
                finish();
                overridePendingTransition(0, 0);
            }
        });
        swipebackLayout.setAllowDrag(false);
        boolean shadowEnable = getIntent().getBooleanExtra(PreferencesConstants.shadowEnable_KEY, true);
        swipebackLayout.setShadowEnable(shadowEnable);

        TextView titleView = findViewById(R.id.title);
        titleView.setText(mContext.getString(R.string.menu_skin_peeler));

        //返回
        RelativeLayout backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swipebackLayout.finish();

            }
        });
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {

    }

    @Override
    public void onBackPressed() {
        swipebackLayout.finish();
    }

    @OnClick({R.id.ll_green, R.id.ll_black, R.id.ll_custom_skin})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_green:
                break;
            case R.id.ll_black:
                break;
            case R.id.ll_custom_skin:
                Crop.pickImage(mActivity, MainActivity.PHOTO_REQUEST_GALLERY);
                break;
        }
    }
}
