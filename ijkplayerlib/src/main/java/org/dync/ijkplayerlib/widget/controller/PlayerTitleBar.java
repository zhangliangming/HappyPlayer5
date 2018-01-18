package org.dync.ijkplayerlib.widget.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.dync.ijkplayerlib.R;
import org.dync.ijkplayerlib.widget.controller.impl.IPlayerTitleBarImpl;

/**
 * Created by KathLine on 2017/12/13.
 */

public class PlayerTitleBar extends LinearLayout {

    private Context mContext;
    private IPlayerTitleBarImpl mIPlayerTitleBar;
    private TextView mTvTitle;

    public PlayerTitleBar(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public PlayerTitleBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PlayerTitleBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayerTitleBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context){
        mContext = context;
        inflate(context, R.layout.video_title_bar, this);

        View rlBack = findViewById(R.id.rl_back);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
    }

    public void setTitleBarImpl(IPlayerTitleBarImpl playerTitleBar) {
        mIPlayerTitleBar = playerTitleBar;
    }

    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            mTvTitle.setText(title);
        }
    }
}
