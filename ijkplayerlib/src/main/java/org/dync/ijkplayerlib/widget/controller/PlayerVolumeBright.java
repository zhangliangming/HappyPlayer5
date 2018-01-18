package org.dync.ijkplayerlib.widget.controller;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.dync.ijkplayerlib.R;
import org.dync.ijkplayerlib.widget.controller.impl.IPlayerVolumeBrightImpl;

/**
 * Created by KathLine on 2017/12/13.
 */

public class PlayerVolumeBright extends FrameLayout {

    private Context mContext;
    private IPlayerVolumeBrightImpl mIPlayerVolumeBright;
    private View view;
    private LinearLayout mLlVideoVolume;
    private ImageView mIvVideoVolume;
    private TextView mTvVideoVolume;

    private LinearLayout mLlVideoBrightness;
    private ImageView mIvVideoBrightness;
    private TextView mTvVideoBrightness;

    private LinearLayout mLlVideoFastForward;
    private TextView mTvVideoFastForward;
    private TextView mTvVideoFastForwardTarget;
    private TextView mTvVideoFastForwardAll;

    public PlayerVolumeBright(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public PlayerVolumeBright(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public PlayerVolumeBright(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayerVolumeBright(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context){
        mContext = context;
        view = inflate(context, R.layout.video_volume_bright, this);

        mLlVideoVolume = findViewById(R.id.ll_video_volume);
        mIvVideoVolume = findViewById(R.id.iv_video_volume);
        mTvVideoVolume = findViewById(R.id.tv_video_volume);

        mLlVideoBrightness = findViewById(R.id.ll_video_brightness);
        mIvVideoBrightness = findViewById(R.id.iv_video_brightness);
        mTvVideoBrightness = findViewById(R.id.tv_video_brightness);

        mLlVideoFastForward = findViewById(R.id.ll_video_fastForward);
        mTvVideoFastForward = findViewById(R.id.tv_video_fastForward);
        mTvVideoFastForwardTarget = findViewById(R.id.tv_video_fastForward_target);
        mTvVideoFastForwardAll = findViewById(R.id.tv_video_fastForward_all);
    }

    public void setPlayerVolumeBrightImpl(IPlayerVolumeBrightImpl playerVolumeBright) {
        mIPlayerVolumeBright = playerVolumeBright;
    }

    /////////////////////////////////////////////////////get方法/////////////////////////////////////

    public LinearLayout getLlVideoVolume() {
        return mLlVideoVolume;
    }

    public ImageView getIvVideoVolume() {
        return mIvVideoVolume;
    }

    public TextView getTvVideoVolume() {
        return mTvVideoVolume;
    }

    public LinearLayout getLlVideoBrightness() {
        return mLlVideoBrightness;
    }

    public ImageView getIvVideoBrightness() {
        return mIvVideoBrightness;
    }

    public TextView getTvVideoBrightness() {
        return mTvVideoBrightness;
    }

    public LinearLayout getLlVideoFastForward() {
        return mLlVideoFastForward;
    }

    public TextView getTvVideoFastForward() {
        return mTvVideoFastForward;
    }

    public TextView getTvVideoFastForwardTarget() {
        return mTvVideoFastForwardTarget;
    }

    public TextView getTvVideoFastForwardAll() {
        return mTvVideoFastForwardAll;
    }
}
