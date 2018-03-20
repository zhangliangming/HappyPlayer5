package com.zlm.hp.ui.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tencent.bugly.beta.Beta;
import com.zlm.hp.R;
import com.zlm.hp.utils.ApkUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @Description: 关于界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/27 21:17
 * @Throws:
 */
public class AboutActivity extends BaseActivity {
    @BindView(R.id.backImg)
    RelativeLayout backImg;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.iv_icon)
    ImageView ivIcon;
    @BindView(R.id.tv_cur_version)
    TextView tvCurVersion;
    @BindView(R.id.tv_update_description)
    TextView tvUpdateDescription;
    @BindView(R.id.rl_check_update)
    RelativeLayout rlCheckUpdate;
    @BindView(R.id.about_layout)
    LinearLayout aboutLayout;

    @Override
    protected int setContentViewId() {
        return R.layout.activity_about;
    }

    @Override
    protected boolean isAddStatusBar() {
        setStatusColor(Color.TRANSPARENT);
        return true;
    }

    @Override
    public int setStatusBarParentView() {
        return 0;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        title.setText(mContext.getString(R.string.about));
        tvCurVersion.setText(String.format(getString(R.string.cur_version), ApkUtil.getVersionName(mContext)));
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @OnClick({R.id.backImg, R.id.rl_check_update})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.backImg:
                finish();
                break;
            case R.id.rl_check_update:
                Beta.checkUpgrade();
                break;
        }
    }
}
