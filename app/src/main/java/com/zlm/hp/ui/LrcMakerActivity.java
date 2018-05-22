package com.zlm.hp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zlm.hp.utils.FileUtils;
import com.zlm.libs.widget.SwipeBackLayout;

/**
 * @Description: 歌词制作器界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2018-03-24
 * @Throws:
 */
public class LrcMakerActivity extends BaseActivity {

    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     * 选择歌曲文件请求码
     */
    private final int SELECTAUDIOFILE = 0;
    /**
     * 选择歌曲文件请求码
     */
    private final int SELECTLRCFILE = 1;

    /**
     * 设置歌曲文件路径
     */
    private final int SETAUDIOFILEPATH = 0;

    /**
     * 设置歌词文件路径
     */
    private final int SETLRCFILEPATH = 1;

    /**
     * 歌曲路径tv
     */
    private TextView mAudioFilePathTv;

    /**
     * 歌词路径tv
     */
    private TextView mLrcFilePathTv;

    /**
     * 歌曲路径
     */
    private String mAudioFilePath;

    /**
     * 歌词路径
     */
    private String mLrcFilePath;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SETAUDIOFILEPATH:

                    if (mAudioFilePath != null && !mAudioFilePath.equals("")) {
                        mAudioFilePathTv.setText("歌曲文件路径：" + mAudioFilePath);
                    } else {
                        mAudioFilePathTv.setText("歌曲文件路径：");
                    }
                    break;

                case SETLRCFILEPATH:

                    if (mLrcFilePath != null && !mLrcFilePath.equals("")) {
                        mLrcFilePathTv.setText("歌词文件路径：" + mLrcFilePath);
                    } else {
                        mLrcFilePathTv.setText("歌词文件路径：");
                    }
                    break;
            }
        }
    };


    @Override
    protected int setContentViewId() {
        return R.layout.activity_lrc_maker;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mSwipeBackLayout = findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {

            @Override
            public void finishActivity() {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        TextView titleView = findViewById(R.id.title);
        titleView.setText("歌词制作器");

        //返回
        RelativeLayout backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();

            }
        });

        //选择歌曲按钮
        Button selectAudioFile = findViewById(R.id.selectAudioFile);
        selectAudioFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent selectFileIntent = new Intent(LrcMakerActivity.this, FileManagerActivity.class);
                startActivityForResult(selectFileIntent, SELECTAUDIOFILE);


            }
        });
        mAudioFilePathTv = findViewById(R.id.audioFilePath);

        //制作歌词
        Button mMakeLrcBtn = findViewById(R.id.makeLrcBtn);
        mMakeLrcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //传递音频和歌词数据
                if (mAudioFilePath == null || mAudioFilePath.equals("")) {

                    Toast.makeText(getApplicationContext(), "请选择支持的音频文件！", Toast.LENGTH_SHORT).show();

                    return;
                }


                //打开歌词制作界面
                Intent lrcMakeIntent = new Intent(LrcMakerActivity.this,
                        LrcMakeSettingActivity.class);


                lrcMakeIntent.putExtra("audioFilePath", mAudioFilePath);

                if (mLrcFilePath != null && !mLrcFilePath.equals(""))
                    lrcMakeIntent.putExtra("lrcFilePath", mLrcFilePath);

                lrcMakeIntent.putExtra("reloadLrcData", false);
                startActivity(lrcMakeIntent);
                //去掉动画
                overridePendingTransition(0, 0);

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        finish();
                    }
                }.start();


            }
        });

        //选择歌词文件
        Button selectLrcFile = findViewById(R.id.selectLrcFile);
        selectLrcFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent selectFileIntent = new Intent(LrcMakerActivity.this, FileManagerActivity.class);
                startActivityForResult(selectFileIntent, SELECTLRCFILE);


            }
        });
        mLrcFilePathTv = findViewById(R.id.lrcFilePath);

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
        return R.id.maker_layout;
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECTAUDIOFILE) {
            if (resultCode == Activity.RESULT_OK) {

                mAudioFilePath = data.getStringExtra("selectFilePath");
                if (mAudioFilePath != null && !mAudioFilePath.equals("")) {
                    String ext = FileUtils.getFileExt(mAudioFilePath);
                    if (!ext.equals("mp3") && !ext.equals("ape") && !ext.equals("flac") && !ext.equals("wav")) {
                        Toast.makeText(getApplicationContext(), "请选择支持的音频文件！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } else {
                mAudioFilePath = null;
            }
            mHandler.sendEmptyMessage(SETAUDIOFILEPATH);
        } else {
            if (resultCode == Activity.RESULT_OK) {

                mLrcFilePath = data.getStringExtra("selectFilePath");
                if (mLrcFilePath != null && !mLrcFilePath.equals("")) {
                    String ext = FileUtils.getFileExt(mLrcFilePath);
                    if (!ext.equals("krc") && !ext.equals("hrc") && !ext.equals("ksc") && !ext.equals("lrc")) {
                        Toast.makeText(getApplicationContext(), "请选择支持的歌词文件！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } else {
                mLrcFilePath = null;
            }
            mHandler.sendEmptyMessage(SETLRCFILEPATH);
        }
    }

}
