package com.zlm.hp.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.constants.PreferencesConstants;
import com.zlm.hp.libs.utils.ColorUtil;
import com.zlm.hp.libs.utils.LoggerUtil;
import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.utils.LyricsUtils;
import com.zlm.hp.lyrics.widget.AbstractLrcView;
import com.zlm.hp.lyrics.widget.FloatLyricsView;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.NotificationReceiver;
import com.zlm.hp.ui.MainActivity;
import com.zlm.hp.ui.R;
import com.zlm.hp.widget.des.FloatLinearLayout;

/**
 * @Description: 悬浮窗口服务
 * @author: zhangliangming
 * @date: 2018-05-12 10:39
 **/
public class FloatService extends Service {
    /**
     *
     */
    private LoggerUtil logger;

    private HPApplication mHPApplication;

    /**
     * 窗口管理
     */
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayout;
    private int mWindowY = 0;

    //////////////////////////////////////////////////
    /**
     * 桌面布局
     */
    private FloatLinearLayout mFloatLinearLayout;
    /**
     * 标题栏
     */
    private RelativeLayout mTitleRelativeLayout;
    /**
     * 歌曲名称
     */
    private TextView mSongNameTv;
    /**
     * 双行歌词视图
     */
    private FloatLyricsView mFloatLyricsView;
    /**
     * 操作布局
     */
    private LinearLayout mOperateLinearLayout;
    /**
     * 播放按钮
     */
    private ImageView mPlayBtn;
    /**
     * 暂停按钮
     */
    private ImageView mPauseBtn;

    /**
     * 设置布局
     */
    private LinearLayout mSettingLinearLayout;

    ////////////////////////////////////////////////////////
    /**
     * 音频广播
     */
    private AudioBroadcastReceiver mAudioBroadcastReceiver;

    /**
     * 广播监听
     */
    private AudioBroadcastReceiver.AudioReceiverListener mAudioReceiverListener = new AudioBroadcastReceiver.AudioReceiverListener() {
        @Override
        public void onReceive(Context context, Intent intent) {
            doAudioReceive(context, intent);
        }
    };

    /**
     * 处理界面隐藏
     */
    private int mAlpha = 200;
    private Handler mHandler = new Handler();
    private int mDelayMs = 3 * 1000;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            if (mTitleRelativeLayout.getVisibility() != View.GONE) {
                mTitleRelativeLayout.setVisibility(View.GONE);
            }
            if (mOperateLinearLayout.getVisibility() != View.GONE) {
                mOperateLinearLayout.setVisibility(View.GONE);
            }
            if (mSettingLinearLayout.getVisibility() != View.GONE) {
                mSettingLinearLayout.setVisibility(View.GONE);
            }

            mFloatLinearLayout.setBackgroundColor(ColorUtil.parserColor(Color.BLACK, 0));
            mFloatLinearLayout.setTag(0);
        }
    };
    /**
     * 显示界面
     */
    private Runnable mShowRunnable = new Runnable() {
        @Override
        public void run() {

            mFloatLinearLayout.setBackgroundColor(ColorUtil.parserColor(Color.BLACK, mAlpha));
            mFloatLinearLayout.setTag(mAlpha);

            if (mTitleRelativeLayout.getVisibility() != View.VISIBLE) {
                mTitleRelativeLayout.setVisibility(View.VISIBLE);
            }
            if (mOperateLinearLayout.getVisibility() != View.VISIBLE) {
                mOperateLinearLayout.setVisibility(View.VISIBLE);
            }

            mHandler.postDelayed(mRunnable, mDelayMs);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initData();
        registerReceiver();
        return super.onStartCommand(intent, START_STICKY, startId);
    }

    /**
     *
     */
    private void registerReceiver() {
        //注册接收音频播放广播
        mAudioBroadcastReceiver = new AudioBroadcastReceiver(getApplicationContext(), mHPApplication);
        mAudioBroadcastReceiver.setAudioReceiverListener(mAudioReceiverListener);
        mAudioBroadcastReceiver.registerReceiver(getApplicationContext());

    }

    /**
     * 初始化数据
     */
    private void initData() {
        mHPApplication = HPApplication.getInstance();
        logger = LoggerUtil.getZhangLogger(getApplicationContext());

        //创建窗口
        createWindowManager();
        //创建桌面布局
        createDesktopLayout();

        //加载数据
        AudioInfo curAudioInfo = mHPApplication.getCurAudioInfo();
        if (curAudioInfo != null) {
            Intent initIntent = new Intent(AudioBroadcastReceiver.ACTION_INITMUSIC);
            doAudioReceive(getApplicationContext(), initIntent);
        } else {
            Intent nullIntent = new Intent(AudioBroadcastReceiver.ACTION_NULLMUSIC);
            doAudioReceive(getApplicationContext(), nullIntent);
        }

        if (mFloatLinearLayout.getParent() == null) {
            mWindowManager.addView(mFloatLinearLayout, mLayout);
        }

        //
        mHandler.postDelayed(mRunnable, 0);

    }

    /**
     * 创建窗口
     */
    @SuppressLint("WrongConstant")
    private void createWindowManager() {
        // 取得系统窗体
        mWindowManager = (WindowManager) getApplicationContext()
                .getSystemService("window");

        // 窗体的布局样式
        mLayout = new WindowManager.LayoutParams();
        // 设置窗体显示类型——TYPE_SYSTEM_ALERT(系统提示)
        mLayout.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        // 设置显示的模式
        mLayout.format = PixelFormat.RGBA_8888;
        // 设置对齐的方法
        mLayout.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        // 设置窗体宽度和高度
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        mLayout.width = dm.widthPixels - 20;
        mLayout.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayout.y = mHPApplication.getDesktopLrcY();

        // 设置窗体焦点及触摸：
        boolean desktopLyricsIsMove = HPApplication.getInstance().isDesktopLyricsIsMove();
        if (desktopLyricsIsMove) {
            mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        } else {
            mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }

        mWindowY = mLayout.y;
    }


    /**
     * 创建窗口界面
     */
    private void createDesktopLayout() {
        ViewGroup mainView = (ViewGroup) LayoutInflater.from(getApplicationContext()).inflate(
                R.layout.window_float_layout, null);
        mFloatLinearLayout = mainView.findViewById(R.id.floatLinearLayout);
        mFloatLinearLayout.setBackgroundColor(ColorUtil.parserColor(Color.BLACK, mAlpha));
        mFloatLinearLayout.setTag(mAlpha);
        mFloatLinearLayout.setFloatEventCallBack(new FloatLinearLayout.FloatEventCallBack() {
            @Override
            public void moveStart() {
                mHandler.removeCallbacks(mRunnable);
            }

            @Override
            public void move(int dy) {

                mLayout.y = mWindowY - dy;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        mWindowManager.updateViewLayout(mFloatLinearLayout, mLayout);
                    }
                });


            }

            @Override
            public void moveEnd() {
                mWindowY = mLayout.y;
                mHandler.postDelayed(mRunnable, mDelayMs);
                mHPApplication.setDesktopLrcY(mWindowY);
            }

            @Override
            public void click() {

                mHandler.removeCallbacks(mRunnable);
                if (Integer.valueOf(mFloatLinearLayout.getTag() + "") == 0) {
                    mHandler.postDelayed(mShowRunnable, 0);
                } else {
                    mHandler.removeCallbacks(mShowRunnable);
                    mHandler.postDelayed(mRunnable, 100);
                }
            }
        });

        //标题面板
        mTitleRelativeLayout = mainView.findViewById(R.id.title);
        mSongNameTv = mainView.findViewById(R.id.songName);

        //图标
        ImageView iconBtn = mainView.findViewById(R.id.iconbtn);
        iconBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setClass(getBaseContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                startActivity(intent);
            }
        });

        //关闭歌词
        ImageView closeBtn = mainView.findViewById(R.id.closebtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent closeIntent = new Intent(NotificationReceiver.NOTIFIATION_DESLRC_HIDE);
                closeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(closeIntent);
            }
        });

        //双行歌词视图
        mFloatLyricsView = mainView.findViewById(R.id.floatLyricsView);
        mFloatLyricsView.setOrientation(FloatLyricsView.ORIENTATION_CENTER);
        //设置字体大小
        final float floatVH = getResources().getDimension(R.dimen.player_height);
        final int minFontSize = (int) (floatVH / 4);
        final int maxFontSize = (int) (floatVH / 3);
        int fontSize = Math.max(mHPApplication.getDesktopLrcFontSize(), minFontSize);
        //设置字体文件
        Typeface typeFace = Typeface.createFromAsset(getAssets(),
                "fonts/pingguolihei.ttf");
        mFloatLyricsView.setTypeFace(typeFace);
        final Paint paint = new Paint();
        paint.setTypeface(typeFace);
        setLrcFontSize(paint, floatVH, fontSize, false);

        //
        int desktopLrcColorIndex = mHPApplication.getDesktopLrcColorIndex();
        setLrcColor(desktopLrcColorIndex, false);

        //操作面板
        mOperateLinearLayout = mainView.findViewById(R.id.operate);
        //锁按钮
        RelativeLayout lockBtn = mainView.findViewById(R.id.lockbtn);
        lockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(mRunnable);

                Intent preIntent = new Intent(NotificationReceiver.NOTIFIATION_DESLRC_LOCK);
                preIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(preIntent);

                Toast.makeText(getApplicationContext(), "歌词已锁，可通过点击通知栏解锁按钮进行解锁!", Toast.LENGTH_LONG).show();
                mHandler.postDelayed(mRunnable, 0);
            }
        });

        //上一首
        RelativeLayout preBtn = mainView.findViewById(R.id.prebtn);
        preBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(mRunnable);


                Intent preIntent = new Intent(AudioBroadcastReceiver.ACTION_PREMUSIC);
                preIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(preIntent);

                mHandler.postDelayed(mRunnable, mDelayMs);
            }
        });

        //播放
        mPlayBtn = mainView.findViewById(R.id.play_btn);
        mPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(mRunnable);

                int playStatus = mHPApplication.getPlayStatus();
                if (playStatus == AudioPlayerManager.PAUSE) {

                    AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                    if (audioInfo != null) {

                        AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
                        Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_RESUMEMUSIC);
                        resumeIntent.putExtra(AudioMessage.KEY, audioMessage);
                        resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        sendBroadcast(resumeIntent);

                    }

                } else {
                    if (mHPApplication.getCurAudioMessage() != null) {
                        AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
                        AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                        if (audioInfo != null) {
                            audioMessage.setAudioInfo(audioInfo);
                            Intent playIntent = new Intent(AudioBroadcastReceiver.ACTION_PLAYMUSIC);
                            playIntent.putExtra(AudioMessage.KEY, audioMessage);
                            playIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            sendBroadcast(playIntent);
                        }
                    }
                }

                mHandler.postDelayed(mRunnable, mDelayMs);
            }
        });

        //暂停
        mPauseBtn = mainView.findViewById(R.id.pause_btn);
        mPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(mRunnable);

                int playStatus = mHPApplication.getPlayStatus();
                if (playStatus == AudioPlayerManager.PLAYING) {

                    Intent resumeIntent = new Intent(AudioBroadcastReceiver.ACTION_PAUSEMUSIC);
                    resumeIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(resumeIntent);

                }

                mHandler.postDelayed(mRunnable, mDelayMs);
            }
        });


        //下一首
        RelativeLayout nextBtn = mainView.findViewById(R.id.nextbtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(mRunnable);


                Intent nextIntent = new Intent(AudioBroadcastReceiver.ACTION_NEXTMUSIC);
                nextIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                sendBroadcast(nextIntent);

                mHandler.postDelayed(mRunnable, mDelayMs);
            }
        });

        //设置按钮
        RelativeLayout settingBtn = mainView.findViewById(R.id.settingbtn);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(mRunnable);

                if (mSettingLinearLayout.getVisibility() == View.GONE) {
                    mSettingLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    mSettingLinearLayout.setVisibility(View.GONE);
                }

                mHandler.postDelayed(mRunnable, mDelayMs);
            }
        });

        //设置布局
        mSettingLinearLayout = mainView.findViewById(R.id.setting);
        mSettingLinearLayout.setVisibility(View.GONE);

        //初始化歌词颜色面板
        initLrcColorPanel(mainView);

        //字体减小
        RelativeLayout lrcSizeDecrease = mainView.findViewById(R.id.lyric_decrease);
        lrcSizeDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.removeCallbacks(mRunnable);

                int fontSize = mHPApplication.getDesktopLrcFontSize();
                fontSize -= 2;
                fontSize = Math.max(fontSize, minFontSize);
                setLrcFontSize(paint, floatVH, fontSize, true);


                mHandler.postDelayed(mRunnable, mDelayMs);
            }
        });

        //字体增加
        RelativeLayout lrcSizeIncrease = mainView.findViewById(R.id.lyric_increase);
        lrcSizeIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.removeCallbacks(mRunnable);

                int fontSize = mHPApplication.getDesktopLrcFontSize();
                fontSize += 2;
                fontSize = Math.min(fontSize, maxFontSize);
                setLrcFontSize(paint, floatVH, fontSize, true);


                mHandler.postDelayed(mRunnable, mDelayMs);
            }
        });
    }

    /**
     * @param paint
     * @param fontSize
     */
    private void setLrcFontSize(Paint paint, float floatVH, int fontSize, boolean isInvalidateView) {
        float spaceLineHeight = (floatVH - LyricsUtils.getTextHeight(paint) * 2) / 3;
        float extraLrcSpaceLineHeight = spaceLineHeight;
        mFloatLyricsView.setSpaceLineHeight(spaceLineHeight);
        mFloatLyricsView.setExtraLrcSpaceLineHeight(extraLrcSpaceLineHeight);
        mFloatLyricsView.setSize(fontSize, fontSize, isInvalidateView);
        mHPApplication.setDesktopLrcFontSize(fontSize);
    }

    /**
     * @param mainView
     */
    private void initLrcColorPanel(ViewGroup mainView) {
        //歌词颜色面板
        ImageView[] colorPanel = new ImageView[5];
        final ImageView[] colorStatus = new ImageView[colorPanel.length];

        int i = 0;
        //
        colorPanel[i] = mainView.findViewById(R.id.color_panel1);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.removeCallbacks(mRunnable);

                int index = mHPApplication.getDesktopLrcColorIndex();
                if (index != 0) {
                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[0].setVisibility(View.VISIBLE);
                    setLrcColor(0, true);
                }

                mHandler.postDelayed(mRunnable, mDelayMs);
            }
        });
        colorStatus[i] = mainView.findViewById(R.id.color_status1);
        colorStatus[i].setVisibility(View.GONE);
        //
        i++;
        colorPanel[i] = mainView.findViewById(R.id.color_panel2);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.removeCallbacks(mRunnable);

                int index = mHPApplication.getDesktopLrcColorIndex();
                if (index != 1) {

                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[1].setVisibility(View.VISIBLE);

                    setLrcColor(1, true);

                }

                mHandler.postDelayed(mRunnable, mDelayMs);

            }
        });
        colorStatus[i] = mainView.findViewById(R.id.color_status2);
        colorStatus[i].setVisibility(View.GONE);
        //
        i++;
        colorPanel[i] = mainView.findViewById(R.id.color_panel3);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.removeCallbacks(mRunnable);

                int index = mHPApplication.getDesktopLrcColorIndex();
                if (index != 2) {

                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[2].setVisibility(View.VISIBLE);

                    setLrcColor(2, true);
                }

                mHandler.postDelayed(mRunnable, mDelayMs);
            }
        });
        colorStatus[i] = mainView.findViewById(R.id.color_status3);
        colorStatus[i].setVisibility(View.GONE);
        //
        i++;
        colorPanel[i] = mainView.findViewById(R.id.color_panel4);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.removeCallbacks(mRunnable);

                int index = mHPApplication.getDesktopLrcColorIndex();
                if (index != 3) {

                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[3].setVisibility(View.VISIBLE);

                    setLrcColor(3, true);
                }

                mHandler.postDelayed(mRunnable, mDelayMs);
            }
        });
        colorStatus[i] = mainView.findViewById(R.id.color_status4);
        colorStatus[i].setVisibility(View.GONE);
        //
        i++;
        colorPanel[i] = mainView.findViewById(R.id.color_panel5);
        colorPanel[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.removeCallbacks(mRunnable);

                int index = mHPApplication.getDesktopLrcColorIndex();
                if (index != 4) {

                    colorStatus[index].setVisibility(View.GONE);
                    colorStatus[4].setVisibility(View.VISIBLE);

                    setLrcColor(4, true);
                }

                mHandler.postDelayed(mRunnable, mDelayMs);
            }
        });
        colorStatus[i] = mainView.findViewById(R.id.color_status5);
        colorStatus[i].setVisibility(View.GONE);

        //
        colorStatus[mHPApplication.getDesktopLrcColorIndex()].setVisibility(View.VISIBLE);
    }

    /**
     * 设置歌词颜色
     *
     * @param index
     */
    private void setLrcColor(int index, boolean invalidate) {
        if (invalidate) {
            mHPApplication.setDesktopLrcColorIndex(index);
        }
        //未读颜色
        int paintColors[] = PreferencesConstants.desktopLrcNotReadColors[index];
        //已读颜色
        int paintHLColors[] = PreferencesConstants.desktopLrcReadedColors[index];
        mFloatLyricsView.setPaintColor(paintColors);
        mFloatLyricsView.setPaintHLColor(paintHLColors, invalidate);
    }

    @Override
    public void onDestroy() {


        if (mFloatLinearLayout.getParent() != null) {
            mWindowManager.removeView(mFloatLinearLayout);
        }
        //注销广播
        mAudioBroadcastReceiver.unregisterReceiver(getApplicationContext());

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 处理音频广播事件
     *
     * @param context
     * @param intent
     */
    private void doAudioReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(AudioBroadcastReceiver.ACTION_NULLMUSIC)) {
            //空数据
            mSongNameTv.setText(R.string.def_text);
            mPauseBtn.setVisibility(View.INVISIBLE);
            mPlayBtn.setVisibility(View.VISIBLE);
            //
            mFloatLyricsView.initLrcData();

        } else if (action.equals(AudioBroadcastReceiver.ACTION_INITMUSIC)) {

            //初始化
            AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
            mSongNameTv.setText(audioInfo.getSingerName() + "-" + audioInfo.getSongName());


            //加载歌词
            String keyWords = "";
            if (audioInfo.getSingerName().equals("未知")) {
                keyWords = audioInfo.getSongName();
            } else {
                keyWords = audioInfo.getSingerName() + " - " + audioInfo.getSongName();
            }
            LyricsManager.getLyricsManager(mHPApplication, getApplicationContext()).loadLyricsUtil(keyWords, keyWords, audioInfo.getDuration() + "", audioInfo.getHash());


            mFloatLyricsView.initLrcData();
            //加载中
            mFloatLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);

            //
            if (mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYING) {
                mPauseBtn.setVisibility(View.VISIBLE);
                mPlayBtn.setVisibility(View.INVISIBLE);
            } else {
                mPauseBtn.setVisibility(View.INVISIBLE);
                mPlayBtn.setVisibility(View.VISIBLE);
            }
        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYMUSIC)) {
            //播放

            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();

            mPauseBtn.setVisibility(View.VISIBLE);
            mPlayBtn.setVisibility(View.INVISIBLE);


            if (audioMessage != null) {

                AudioInfo audioInfo = mHPApplication.getCurAudioInfo();
                if (audioInfo != null) {
                    //更新歌词


                    if (mFloatLyricsView.getLyricsReader() != null && mFloatLyricsView.getLyricsReader().getHash().equals(audioInfo.getHash()) && mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mFloatLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
                        mFloatLyricsView.play((int) audioMessage.getPlayProgress());
                    }
                }

            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PAUSEMUSIC)) {

            if (mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                mFloatLyricsView.pause();
            }

            //暂停完成
            mPauseBtn.setVisibility(View.INVISIBLE);
            mPlayBtn.setVisibility(View.VISIBLE);


        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_RESUMEMUSIC)) {
            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
            if (audioMessage != null) {
                if (mFloatLyricsView != null && mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mFloatLyricsView.play((int) audioMessage.getPlayProgress());
                }
            }

            //唤醒完成
            mPauseBtn.setVisibility(View.VISIBLE);
            mPlayBtn.setVisibility(View.INVISIBLE);


        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_SEEKTOMUSIC)) {
            //唤醒完成
            mPauseBtn.setVisibility(View.VISIBLE);
            mPlayBtn.setVisibility(View.INVISIBLE);

            AudioMessage audioMessage = mHPApplication.getCurAudioMessage();
            if (audioMessage != null) {
                if (mFloatLyricsView != null && mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                    mFloatLyricsView.play((int) audioMessage.getPlayProgress());
                }
            }
        } else if (action.equals(AudioBroadcastReceiver.ACTION_SERVICE_PLAYINGMUSIC)) {

        } else if (action.equals(AudioBroadcastReceiver.ACTION_LRCLOADED)) {
            if (mHPApplication.getCurAudioMessage() != null && mHPApplication.getCurAudioInfo() != null) {
                //歌词加载完成
                AudioMessage curAudioMessage = mHPApplication.getCurAudioMessage();
                AudioMessage audioMessage = (AudioMessage) intent.getSerializableExtra(AudioMessage.KEY);
                String hash = audioMessage.getHash();
                if (hash.equals(mHPApplication.getCurAudioInfo().getHash())) {
                    //
                    LyricsReader lyricsReader = LyricsManager.getLyricsManager(mHPApplication, getApplicationContext()).getLyricsUtil(hash);
                    if (lyricsReader != null) {
                        if (lyricsReader.getHash() != null && lyricsReader.getHash().equals(hash) && mFloatLyricsView.getLyricsReader() != null) {
                            //已加载歌词，不用重新加载
                        } else {
                            lyricsReader.setHash(hash);
                            mFloatLyricsView.setLyricsReader(lyricsReader);
                            if (mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYING && mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mFloatLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY)
                                mFloatLyricsView.play((int) curAudioMessage.getPlayProgress());
                        }
                    }
                }
            }
        } else if (action.equals(AudioBroadcastReceiver.ACTION_LRCSEEKTO)) {
            if (mHPApplication.getCurAudioMessage() != null) {
                if (mHPApplication.getCurAudioInfo() != null) {
                    if (mFloatLyricsView.getLyricsReader() != null && mFloatLyricsView.getLyricsReader().getHash().equals(mHPApplication.getCurAudioInfo().getHash()) && mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC) {
                        mFloatLyricsView.seekto((int) mHPApplication.getCurAudioMessage().getPlayProgress());
                    }
                }
            }

        } else if (action.equals(AudioBroadcastReceiver.ACTION_DESLRC_LOCKORUNLOCK)) {
            //歌词解锁或者加锁
            if (mHPApplication.isDesktopLyricsIsMove()) {
                mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            } else {
                mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            }
            if (mFloatLinearLayout.getParent() != null) {
                mWindowManager.updateViewLayout(mFloatLinearLayout, mLayout);
            }
        }
    }
}
