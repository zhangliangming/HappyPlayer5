package com.zlm.hp.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.R;
import com.zlm.hp.ui.fragment.SettingFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.backImg)
    RelativeLayout backImg;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.right_flag)
    View rightFlag;
    @BindView(R.id.ll_fragment_container)
    LinearLayout llFragmentContainer;

    @Override
    protected int setContentViewId() {
        return R.layout.activity_setting;
    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }

    @Override
    public int setStatusBarParentView() {
        return 0;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);

        TextView titleView = findViewById(R.id.title);
        titleView.setText(mContext.getString(R.string.menu_setting));

        //返回
        RelativeLayout backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        SettingFragment settingFragment = new SettingFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.ll_fragment_container, settingFragment)
                .commit();
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
