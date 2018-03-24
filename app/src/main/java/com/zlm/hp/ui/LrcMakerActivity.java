package com.zlm.hp.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
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

        //选择歌曲按钮
        Button selectAudioFile = findViewById(R.id.selectAudioFile);
        selectAudioFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = null;
                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("file/*");
                }
                startActivityForResult(intent, SELECTAUDIOFILE);

            }
        });
        mAudioFilePathTv = findViewById(R.id.audioFilePath);
        Button mMakeLrcBtn = findViewById(R.id.makeLrcBtn);
        mMakeLrcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //选择歌词文件
        Button selectLrcFile = findViewById(R.id.selectLrcFile);
        selectLrcFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = null;
                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                } else {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.setType("file/*");
                }
                startActivityForResult(intent, SELECTLRCFILE);

            }
        });
        mLrcFilePathTv = findViewById(R.id.lrcFilePath);
        Button mMakeExtraLrcBtn = findViewById(R.id.makeExtraLrcBtn);
        mMakeExtraLrcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
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

                Uri uri = data.getData();
                if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                    mAudioFilePath = uri.getPath();

                    return;
                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                    mAudioFilePath = FileUtils.getPath(getApplicationContext(), uri);

                } else {//4.4以下下系统调用方法
                    mAudioFilePath = FileUtils.getRealPathFromURI(getApplicationContext(), uri);

                }
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

                Uri uri = data.getData();
                if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                    mLrcFilePath = uri.getPath();

                    return;
                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                    mLrcFilePath = FileUtils.getPath(getApplicationContext(), uri);

                } else {//4.4以下下系统调用方法
                    mLrcFilePath = FileUtils.getRealPathFromURI(getApplicationContext(), uri);

                }
                if (mLrcFilePath != null && !mLrcFilePath.equals("")) {
                    String ext = FileUtils.getFileExt(mLrcFilePath);
                    if (!ext.equals("krc") && !ext.equals("hrc")) {
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
