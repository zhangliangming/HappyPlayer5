package com.zlm.hp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.ui.R;

/**
 * 下载音乐
 * Created by zhangliangming on 2017/7/23.
 */
public class DownloadMusicFragment extends BaseFragment {

    public DownloadMusicFragment() {

    }

    @Override
    protected void loadData(boolean isRestoreInstance) {

    }

    @Override
    protected int setContentViewId() {
        return R.layout.layout_fragment_download;
    }

    @Override
    protected void initViews(Bundle savedInstanceState, View mainView) {
        TextView titleView = mainView.findViewById(R.id.title);
        titleView.setText("下载管理");

        //返回
        RelativeLayout backImg = mainView.findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                Intent closeIntent = new Intent(FragmentReceiver.ACTION_CLOSEDFRAGMENT);
                closeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mActivity.sendBroadcast(closeIntent);

            }
        });
    }

    @Override
    protected int setTitleViewId() {
        return R.layout.layout_title;
    }


    @Override
    protected boolean isAddStatusBar() {
        return true;
    }
}
