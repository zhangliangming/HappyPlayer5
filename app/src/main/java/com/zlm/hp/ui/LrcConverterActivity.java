package com.zlm.hp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.lyrics.formats.LyricsFileReader;
import com.zlm.hp.lyrics.formats.LyricsFileWriter;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.utils.LyricsIOUtils;
import com.zlm.hp.utils.FileUtils;
import com.zlm.hp.utils.HelperUtil;
import com.zlm.hp.utils.ResourceFileUtil;
import com.zlm.libs.widget.SwipeBackLayout;

import java.io.File;

/**
 * @Description: 歌词转换界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2018-03-16
 * @Throws:
 */
public class LrcConverterActivity extends BaseActivity {
    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;
    /**
     * 源文件路径
     */
    private TextView mOrigFilePathTv;
    private String mOrigFilePath;
    /**
     * 输出格式
     */
    private RadioGroup mOutFormatsRG;
    private String[] mOutFormats = new String[]{"ksc", "krc", "hrc", "lrc"};
    private int[] mOutFormatsRadioButtonId = new int[]{R.id.kscRadioButton, R.id.krcRadioButton, R.id.hrcRadioButton, R.id.lrcRadioButton};

    /**
     *
     */
    public HelperUtil mHelper = new HelperUtil(this);


    /**
     * 选择源文件请求码
     */
    private final int SELECTORIGFILE = 0;
    /**
     * 设置源文件路径
     */
    private final int SETORIGFILEPATH = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SETORIGFILEPATH:

                    if (mOrigFilePath != null && !mOrigFilePath.equals("")) {
                        mOrigFilePathTv.setText("源文件路径：" + mOrigFilePath);
                    } else {
                        mOrigFilePathTv.setText("源文件路径：");
                    }
                    break;
            }
        }
    };

    @Override
    protected int setContentViewId() {
        return R.layout.activity_lrc_converter;
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
        titleView.setText("歌词转换器");

        //返回
        RelativeLayout backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();

            }
        });

        //选择源文件按钮
        Button origSelectFileBtn = findViewById(R.id.origSelectFile);
        origSelectFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent selectFileIntent = new Intent(LrcConverterActivity.this, FileManagerActivity.class);
                startActivityForResult(selectFileIntent, SELECTORIGFILE);

            }
        });

        //源文件路径
        mOrigFilePathTv = findViewById(R.id.origFilePath);
        mOutFormatsRG = findViewById(R.id.outFormats);
        mOutFormatsRG.check(mOutFormatsRadioButtonId[2]);
        //转换按钮
        Button converterBtn = findViewById(R.id.converter);
        converterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOrigFilePath != null && !mOrigFilePath.equals("")) {
                    int checkedRadioButtonId = mOutFormatsRG.getCheckedRadioButtonId();
                    if (checkedRadioButtonId == -1) {
                        Toast.makeText(getApplicationContext(), "请选择歌词的输出格式！", Toast.LENGTH_SHORT).show();
                    } else {
                        //获取输出格式索引
                        int index = mOutFormatsRG.indexOfChild(mOutFormatsRG.findViewById(checkedRadioButtonId));
                        //获取输出格式
                        String outFormat = mOutFormats[index];
                        converterLrc(mOrigFilePath, outFormat);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "请选择歌词文件！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 转换歌词
     *
     * @param mOrigFilePath 源文件路径
     * @param outFormat     输出格式
     */
    private void converterLrc(String mOrigFilePath, final String outFormat) {
        final File orgFile = new File(mOrigFilePath);
        if (!orgFile.exists()) {
            Toast.makeText(getApplicationContext(), "源文件不存在，请重新选择源文件！", Toast.LENGTH_SHORT).show();
        } else {
            mHelper.showLoading("开始转换歌词，请稍等....");
            new AsyncTask<String, Integer, Boolean>() {
                @Override
                protected Boolean doInBackground(String... strings) {
                    try {
                        Thread.sleep(500);

                        //1.先读取源文件歌词
                        LyricsFileReader lyricsFileReader = LyricsIOUtils.getLyricsFileReader(orgFile);
                        String outFileName = FileUtils.removeExt(orgFile.getName());
                        String outFilePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_LYRICS, File.separator + outFileName + "." + outFormat);
                        File outFile = new File(outFilePath);
                        //2.生成转换歌词文件
                        LyricsFileWriter lyricsFileWriter = LyricsIOUtils.getLyricsFileWriter(outFile);
                        LyricsInfo lyricsInfo = lyricsFileReader.readFile(orgFile);
                        if (lyricsInfo != null) {
                            return lyricsFileWriter.writer(lyricsInfo, outFile.getPath());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.e(e.getMessage());
                    }

                    return false;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    mHelper.hideLoading();
                    if (result) {
                        Toast.makeText(getApplicationContext(), "歌词转换完成！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "歌词转换失败！", Toast.LENGTH_SHORT).show();

                    }
                }
            }.execute("");

        }
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
        return R.id.converter_layout;
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECTORIGFILE) {
            if (resultCode == Activity.RESULT_OK) {
                mOrigFilePath = data.getStringExtra("selectFilePath");
                if (mOrigFilePath != null && !mOrigFilePath.equals("")) {
                    String ext = FileUtils.getFileExt(mOrigFilePath);
                    if (!ext.equals("krc") && !ext.equals("ksc") && !ext.equals("hrc")) {
                        Toast.makeText(getApplicationContext(), "请选择支持的歌词文件！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } else {
                mOrigFilePath = null;
            }
            mHandler.sendEmptyMessage(SETORIGFILEPATH);
        }
    }


}
