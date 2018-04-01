package com.zlm.hp.ui;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zlm.hp.lyrics.model.MakeExtraLrcLineInfo;
import com.zlm.hp.lyrics.utils.FileUtils;
import com.zlm.libs.widget.SwipeBackLayout;

import java.io.File;

/**
 * @Description: 歌词制作选择界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2018-03-25
 * @Throws:
 */
public class LrcMakeSettingActivity extends BaseActivity {
    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;
    /**
     * 歌曲路径
     */
    private String mAudioFilePath;

    /**
     * 歌词路径
     */
    private String mLrcFilePath;

    /**
     * 制作歌词按钮
     */
    private Button mMakeLrcBtn;

    /**
     * 制作翻译歌词按钮
     */
    private Button mMakeTranslateLrcBtn;


    /**
     * 制作音译歌词按钮
     */
    private Button mMakeTransliterationLrcBtn;

    /**
     * 初始化数据
     */
    private final int INITDATA = 0;
    /**
     *
     */
    private boolean mReloadLrcData = false;

    private String mHash;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INITDATA:

                    if (mAudioFilePath != null && !mAudioFilePath.equals("")) {

                        mMakeLrcBtn.setEnabled(true);
                    }

                    if (mLrcFilePath != null && !mLrcFilePath.equals("")) {

                        mMakeTranslateLrcBtn.setEnabled(true);
                        mMakeTransliterationLrcBtn.setEnabled(true);

                    }
                    break;

            }
        }
    };


    @Override
    protected int setContentViewId() {
        return R.layout.activity_lrc_make;
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
        titleView.setText("歌词制作");

        //返回
        RelativeLayout backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();

            }
        });

        //制作歌词按钮
        mMakeLrcBtn = findViewById(R.id.makeLrcBtn);
        mMakeLrcBtn.setEnabled(false);
        mMakeLrcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //打开歌词制作界面
                Intent lrcMakeIntent = new Intent(LrcMakeSettingActivity.this,
                        MakeLrcActivity.class);


                lrcMakeIntent.putExtra("audioFilePath", mAudioFilePath);

                if (mLrcFilePath != null && !mLrcFilePath.equals(""))
                    lrcMakeIntent.putExtra("lrcFilePath", mLrcFilePath);

                lrcMakeIntent.putExtra("reloadLrcData", mReloadLrcData);
                lrcMakeIntent.putExtra("hash", mHash);

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

        //制作翻译歌词按钮
        mMakeTranslateLrcBtn = findViewById(R.id.makeTranslateLrcBtn);
        mMakeTranslateLrcBtn.setEnabled(false);
        mMakeTranslateLrcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mLrcFilePath == null || mLrcFilePath.equals("")) {
                    Toast.makeText(getApplicationContext(), "歌词文件不能为空!！", Toast.LENGTH_SHORT).show();
                    return;
                } else {

                    File lrcFile = new File(mLrcFilePath);
                    String ext = FileUtils.getFileExt(lrcFile.getName());
                    if (!ext.equals("krc") && !ext.equals("hrc")) {
                        Toast.makeText(getApplicationContext(), ext + "歌词不支持制作翻译歌词!！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                //打开翻译歌词制作界面
                Intent lrcMakeIntent = new Intent(LrcMakeSettingActivity.this,
                        MakeTranslateLrcActivity.class);


                lrcMakeIntent.putExtra("audioFilePath", mAudioFilePath);

                lrcMakeIntent.putExtra("extraLrcType", 1);
                lrcMakeIntent.putExtra("lrcFilePath", mLrcFilePath);
                lrcMakeIntent.putExtra("reloadLrcData", mReloadLrcData);
                lrcMakeIntent.putExtra("hash", mHash);

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


        //制作音译歌词按钮
        mMakeTransliterationLrcBtn = findViewById(R.id.makeTransliterationLrcBtn);
        mMakeTransliterationLrcBtn.setEnabled(false);
        mMakeTransliterationLrcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mLrcFilePath == null || mLrcFilePath.equals("")) {
                    Toast.makeText(getApplicationContext(), "歌词文件不能为空!！", Toast.LENGTH_SHORT).show();
                    return;
                } else {

                    File lrcFile = new File(mLrcFilePath);
                    String ext = FileUtils.getFileExt(lrcFile.getName());
                    if (!ext.equals("krc") && !ext.equals("hrc")) {
                        Toast.makeText(getApplicationContext(), ext + "歌词不支持制作音译歌词!！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }


                //打开音译歌词制作界面
                Intent lrcMakeIntent = new Intent(LrcMakeSettingActivity.this,
                        MakeTransliterationLrcActivity.class);


                lrcMakeIntent.putExtra("audioFilePath", mAudioFilePath);

                if (mLrcFilePath != null && !mLrcFilePath.equals(""))
                    lrcMakeIntent.putExtra("lrcFilePath", mLrcFilePath);

                lrcMakeIntent.putExtra("reloadLrcData", mReloadLrcData);
                lrcMakeIntent.putExtra("extraLrcType", 0);
                lrcMakeIntent.putExtra("hash", mHash);

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

    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        mAudioFilePath = getIntent().getStringExtra("audioFilePath");
        mLrcFilePath = getIntent().getStringExtra("lrcFilePath");
        mReloadLrcData = getIntent().getBooleanExtra("reloadLrcData", false);
        mHash = getIntent().getStringExtra("hash");

        mHandler.sendEmptyMessage(INITDATA);
    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }

    @Override
    public int setStatusBarParentView() {
        return R.id.lrcmake_layout;
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }
}
