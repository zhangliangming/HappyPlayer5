package com.zlm.hp.ui;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zlm.hp.adapter.TabFragmentAdapter;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.dialog.AlartTwoButtonDialog;
import com.zlm.hp.fragment.MakeExtraLrcFragment;
import com.zlm.hp.fragment.PreviewLrcFragment;
import com.zlm.hp.lyrics.formats.LyricsFileWriter;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.MakeExtraLrcLineInfo;
import com.zlm.hp.lyrics.utils.ColorUtils;
import com.zlm.hp.lyrics.utils.FileUtils;
import com.zlm.hp.lyrics.utils.LyricsIOUtils;
import com.zlm.hp.lyrics.utils.StringUtils;
import com.zlm.hp.lyrics.widget.MakeLrcPreView;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.utils.ResourceFileUtil;
import com.zlm.hp.widget.CustomViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Description: 制作音译歌词
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2018-04-01
 * @Throws:
 */
public class MakeTransliterationLrcActivity extends BaseActivity {


    /**
     *
     */
    private CustomViewPager mViewPager;
    /**
     * 翻译歌词视图
     */
    private MakeExtraLrcFragment mMakeExtraLrcFragment;

    /**
     * 歌词预览
     */
    private PreviewLrcFragment mPreviewLrcFragment;

    /**
     * 事件回调
     */
    private MakeLrcActivity.MakeLrcFragmentEvent mMakeLrcFragmentEvent;

    /**
     * 歌曲路径
     */
    private String mAudioFilePath;

    private int mPageIndex;

    private final int CLOSE = 0;
    private final int PREPAGE = 1;
    private final int NEXTPAGE = 2;
    private int type = CLOSE;

    /**
     * 两个按钮弹出窗口
     */
    private AlartTwoButtonDialog mAlartTwoButtonDialog;

    /**
     * 制作额外歌词事件
     */
    private MakeTranslateLrcActivity.ExtraItemEvent mExtraItemEvent;
    /////////////////////////////////额外歌词///////////////////////////////////
    private LinearLayout mExtraLrcLL;
    private TextView mExtraLrcIndexTv;
    private MakeLrcPreView mMakeLrcPreView;
    private TextView mProgressTv;
    private EditText mExtraLrcEt;
    private MakeExtraLrcLineInfo mMakeExtraLrcLineInfo;


    /**
     *
     */
    private boolean mReloadLrcData = false;
    private String mHash;
    /**
     * 额外歌词类型,0是音译，1是翻译
     */
    private int mExtraLrcType;

    @Override
    protected int setContentViewId() {
        return R.layout.activity_make_transliteration_lrc;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        mViewPager = findViewById(R.id.viewpage);
        mMakeLrcFragmentEvent = new MakeLrcActivity.MakeLrcFragmentEvent() {
            @Override
            public void close() {
                type = CLOSE;
                mAlartTwoButtonDialog.showDialog("退出后本页面数据将丢弃!", "确定", "取消");
            }

            @Override
            public void nextPage(int index) {

            }

            @Override
            public void prePage(int index) {
                type = PREPAGE;
                mPageIndex = 0;
                mAlartTwoButtonDialog.showDialog("退出后本页面数据将丢弃!", "确定", "取消");

            }

            @Override
            public void openPreView(LyricsInfo lyricsInfo) {
                type = NEXTPAGE;
                mPageIndex = 1;
                if (mPageIndex == 1 && type == NEXTPAGE) {
                    mViewPager.setCurrentItem(mPageIndex, true);
                    mPreviewLrcFragment.initData(mAudioFilePath, lyricsInfo);
                }
            }

            @Override
            public void saveLrcData(LyricsInfo lyricsInfo) {
                try {
                    //保存歌词
                    File audioFile = new File(mAudioFilePath);
                    String lrcFileName = FileUtils.removeExt(audioFile.getName()) + ".hrc";

                    //
                    if (mExtraLrcType == 0) {
                        //音译，则需要保存该歌词之前的翻译歌词
                        if (mMakeExtraLrcFragment.getLyricsInfo() != null)
                            lyricsInfo.setTranslateLrcLineInfos(mMakeExtraLrcFragment.getLyricsInfo().getTranslateLrcLineInfos());

                    } else {
                        //翻译，则需要保存该歌词之前的音译歌词
                        if (mMakeExtraLrcFragment.getLyricsInfo() != null)
                            lyricsInfo.setTransliterationLrcLineInfos(mMakeExtraLrcFragment.getLyricsInfo().getTransliterationLrcLineInfos());
                    }
                    //
                    lyricsInfo.setLyricsFileExt(FileUtils.getFileExt(lrcFileName));
                    lyricsInfo.setLyricsTags(new HashMap<String, Object>());

                    String saveLrcFilePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_LYRICS, lrcFileName);
                    LyricsFileWriter lyricsFileWriter = LyricsIOUtils.getLyricsFileWriter(lrcFileName);
                    boolean result = lyricsFileWriter.writer(lyricsInfo, saveLrcFilePath);
                    if (result) {
                        Toast.makeText(getApplicationContext(), "保存成功！", Toast.LENGTH_SHORT).show();

                        if (mReloadLrcData && mHash != null && !mHash.equals("")) {

                            LyricsManager.getLyricsManager(mHPApplication,getApplicationContext()).setUseLrcUtil(mHash,saveLrcFilePath);

                        }

                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "保存失败！", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    Toast.makeText(getApplicationContext(), "保存失败！", Toast.LENGTH_SHORT).show();

                }
            }
        };

        ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        //翻译歌词界面
        mMakeExtraLrcFragment = new MakeExtraLrcFragment();
        mMakeExtraLrcFragment.setMakeLrcFragmentEvent(mMakeLrcFragmentEvent);
        fragments.add(mMakeExtraLrcFragment);

        //预览歌词界面
        mPreviewLrcFragment = new PreviewLrcFragment();
        mPreviewLrcFragment.setMakeLrcFragmentEvent(mMakeLrcFragmentEvent);
        fragments.add(mPreviewLrcFragment);

        //
        TabFragmentAdapter adapter = new TabFragmentAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });


        //两个按钮弹出窗口
        mAlartTwoButtonDialog = new AlartTwoButtonDialog(this, new AlartTwoButtonDialog.TwoButtonDialogListener() {
            @Override
            public void oneButtonClick() {
                if (type == CLOSE) {
                    finish();
                } else if (mPageIndex == 0 && type == PREPAGE) {
                    mViewPager.setCurrentItem(mPageIndex, true);
                    mPreviewLrcFragment.resetData();
                }

            }

            @Override
            public void twoButtonClick() {

            }
        });

        /**
         *
         */
        mExtraItemEvent = new MakeTranslateLrcActivity.ExtraItemEvent() {
            @Override
            public void itemClick(int index) {
                extraItemClick(index);
            }
        };
        mMakeExtraLrcFragment.setExtraItemEvent(mExtraItemEvent);

        //翻译
        mExtraLrcLL = findViewById(R.id.extraLL);
        LinearLayout backgroundLL = findViewById(R.id.backgroundLL);
        backgroundLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveAndUpdate();

                if (mExtraLrcLL.getVisibility() != View.GONE) {
                    mExtraLrcLL.setVisibility(View.GONE);
                }
            }
        });
        mExtraLrcLL.setBackgroundColor(ColorUtils.parserColor(Color.BLACK, 50));
        mExtraLrcLL.setVisibility(View.GONE);
        mExtraLrcIndexTv = findViewById(R.id.extraLrcIndex);
        mMakeLrcPreView = findViewById(R.id.makeLrcPreView);
        mProgressTv = findViewById(R.id.progress);
        mExtraLrcEt = findViewById(R.id.extraLrcEt);

        //过滤空格，输入空格后会用∮符号替换空格
        InputFilter spaceFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                String inputExtraLrcString = mExtraLrcEt.getText().toString();
                String[] extraLyricsWords = inputExtraLrcString.split("∮");
                String[] lyricsWords = mMakeExtraLrcLineInfo.getLyricsLineInfo().getLyricsWords();
                if (extraLyricsWords.length == lyricsWords.length) {
                    if (source.equals(" ")) {
                        return "";
                    }
                } else {
                    if (source.equals(" ")) {
                        return "∮";
                    }
                }
                return null;
            }
        };
        mExtraLrcEt.setFilters(new InputFilter[]{spaceFilter});
        mExtraLrcEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String[] lyricsWords = mMakeExtraLrcLineInfo.getLyricsLineInfo().getLyricsWords();
                String inputExtraLrcString = mExtraLrcEt.getText().toString();

                if (StringUtils.isBlank(inputExtraLrcString)) {
                    mMakeExtraLrcLineInfo.setLrcIndex(-1);
                    mProgressTv.setText(0 + "/" + lyricsWords.length);
                    mMakeLrcPreView.postInvalidate();
                    return;
                }
                String[] extraLyricsWords = inputExtraLrcString.split("∮");
                mMakeExtraLrcLineInfo.setLrcIndex(extraLyricsWords.length - 1);
                if (extraLyricsWords.length == lyricsWords.length) {
                    mProgressTv.setText("已完成");
                } else {
                    mProgressTv.setText(extraLyricsWords.length + "/" + lyricsWords.length);
                }
                mMakeLrcPreView.postInvalidate();
            }
        });

        Button preLineBtn = findViewById(R.id.preLineBtn);
        preLineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndUpdate();
                extraItemClick(mMakeExtraLrcFragment.getPreIndex());
            }
        });

        Button nextLineBtn = findViewById(R.id.nextLineBtn);
        nextLineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndUpdate();
                extraItemClick(mMakeExtraLrcFragment.getNextIndex());
            }
        });
    }

    /**
     * 更新
     */
    private void saveAndUpdate() {
        String extraLineLyrics = mExtraLrcEt.getText().toString();
        if (extraLineLyrics == null) {
            extraLineLyrics = "";
        } else {
            extraLineLyrics = extraLineLyrics.trim();
        }
        if (StringUtils.isBlank(extraLineLyrics)) {
            extraLineLyrics = "";
        }
        mMakeExtraLrcLineInfo.setExtraLineLyrics(extraLineLyrics);
        mMakeExtraLrcFragment.saveAndUpdate();
    }

    /**
     * 额外歌词列表item点击
     *
     * @param index
     */
    private void extraItemClick(final int index) {

        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {

                mMakeExtraLrcLineInfo = mMakeExtraLrcFragment.getMakeExtraLrcLineInfo(index);
                return null;
            }

            @Override
            protected void onPostExecute(String result) {

                showInputExtraLrcDialog(index);

                super.onPostExecute(result);
            }
        }.execute("");

    }

    /**
     * 显示输入额外歌词窗口
     *
     * @param index
     */
    private void showInputExtraLrcDialog(int index) {
        mExtraLrcIndexTv.setText(String.format("%0" + (mMakeExtraLrcFragment.getLrcDataSize() + "").length() + "d", (index + 1)));
        mMakeLrcPreView.setMakeLrcInfo(mMakeExtraLrcLineInfo);
        mMakeLrcPreView.postInvalidate();

        String extraLineLyrics = mMakeExtraLrcLineInfo.getExtraLineLyrics();
        if (StringUtils.isNotBlank(extraLineLyrics)) {
            mExtraLrcEt.setText(extraLineLyrics);
        } else {
            mExtraLrcEt.setText("");
        }

        mExtraLrcLL.setVisibility(View.VISIBLE);

    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        mAudioFilePath = getIntent().getStringExtra("audioFilePath");
        String lrcFilePath = getIntent().getStringExtra("lrcFilePath");
        mExtraLrcType = getIntent().getIntExtra("extraLrcType", 1);

        mReloadLrcData = getIntent().getBooleanExtra("reloadLrcData", false);
        mHash = getIntent().getStringExtra("hash");

        //设置音频和歌词路径
        mMakeExtraLrcFragment.setExtraLrcType(mExtraLrcType);
        mMakeExtraLrcFragment.setAudioFilePath(mAudioFilePath);
        mMakeExtraLrcFragment.setLrcFilePath(lrcFilePath);
    }

    @Override
    protected boolean isAddStatusBar() {
        return false;
    }

    @Override
    public int setStatusBarParentView() {
        return 0;
    }

    @Override
    public void onBackPressed() {

    }

}
