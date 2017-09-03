package com.zlm.hp.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.libs.utils.LoggerUtil;
import com.zlm.hp.model.SongSingerInfo;
import com.zlm.hp.utils.ImageUtil;

import java.util.List;

/**
 * 歌手写真
 * Created by zhangliangming on 2017/9/2.
 */

public class SingerImageView extends AppCompatImageView {
    /**
     * 写真路径集合
     */
    private List<SongSingerInfo> mSongSingerInfos;

    /**
     * 歌手写真图片
     */
    private Drawable[] drawables;
    private int mCurIndex = 0;
    private int mDuration = 500;

    private boolean isHasData = false;
    private LoggerUtil logger;

    //处理transition的改变
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if (isHasData && mSongSingerInfos != null && mSongSingerInfos.size() > 0 && drawables != null && drawables.length > 0) {
                if (drawables[mCurIndex % drawables.length] != null) {
                    TransitionDrawable transitionDrawable = null;
                    if (drawables[(mCurIndex + 1) % drawables.length] != null) {
                        transitionDrawable = new TransitionDrawable(new Drawable[]{
                                drawables[mCurIndex % drawables.length],//实现从0 1 2 3 4 5 0 1 2.。。这样的不停转变
                                drawables[(mCurIndex + 1) % drawables.length]});
                        mCurIndex++;
                    } else {
                        transitionDrawable = new TransitionDrawable(new Drawable[]{
                                drawables[mCurIndex % drawables.length],//实现从0 1 2 3 4 5 0 1 2.。。这样的不停转变
                                drawables[mCurIndex % drawables.length]});
                        mCurIndex++;
                    }
                    if (transitionDrawable != null) {
                        setBackground(transitionDrawable);
                        transitionDrawable.startTransition(mDuration);
                    }
                }
            }

        }
    };


    public SingerImageView(Context context) {
        super(context);
        init(context);
    }

    public SingerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SingerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        logger = LoggerUtil.getZhangLogger(context);
        new Thread(new ChageRunnable()).start();
    }

    public synchronized void setSongSingerInfos(HPApplication hPApplication, Context context, List<SongSingerInfo> songSingerInfos) {
        setBackground(new BitmapDrawable());
        drawables = null;
        isHasData = false;
        this.mSongSingerInfos = songSingerInfos;
        //
        if (mSongSingerInfos != null && mSongSingerInfos.size() > 0) {
            mCurIndex = 0;
            drawables = new Drawable[mSongSingerInfos.size() + 1];
            int index = 0;
            for (int i = 0; i < mSongSingerInfos.size(); i++) {
                SongSingerInfo songSingerInfo = mSongSingerInfos.get(i);
                Bitmap bitmap = ImageUtil.getSingerImgBitmap(hPApplication, context, songSingerInfo.getHash(), songSingerInfo.getSingerName(), songSingerInfo.getImgUrl(), false);
                if (bitmap != null) {
                    if (index == 0) {
                        drawables[index++] = new BitmapDrawable(bitmap);
                    }
                    drawables[index++] = new BitmapDrawable(bitmap);
                }
            }
            if (drawables != null && drawables.length > 0) {
                isHasData = true;
            }
        }

    }

    //线程，去发送消息，让transition一直改变
    private class ChageRunnable implements Runnable {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(5 * 1000);
                    if (isHasData && mSongSingerInfos != null && mSongSingerInfos.size() > 0 && drawables != null && drawables.length > 0)
                        handler.sendEmptyMessage(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
