package com.zlm.hp.ui;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zlm.hp.lyrics.LyricsReader;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.LyricsLineInfo;
import com.zlm.hp.lyrics.utils.ColorUtils;
import com.zlm.hp.lyrics.utils.LyricsUtils;
import com.zlm.hp.lyrics.utils.TimeUtils;
import com.zlm.hp.lyrics.widget.LrcImgPreView;
import com.zlm.hp.utils.FileUtils;
import com.zlm.hp.utils.HelperUtil;
import com.zlm.hp.utils.ImageUtil;
import com.zlm.hp.utils.ScreenUtil;
import com.zlm.libs.widget.SwipeBackLayout;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @Description: 歌词文件转换视频界面
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2018-03-17
 * @Throws:
 */
public class LrcImg2VideoActivity extends BaseActivity {
    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     *
     */
    public HelperUtil mHelper = new HelperUtil(this);

    /**
     * 颜色主题
     */
    private RadioGroup mThemeRadioGroup;
    private int[] mThemeRadioButtonId = new int[]{R.id.themeBlue, R.id.themeWhite, R.id.themeBlack, R.id.themePurple};
    /**
     * 主题颜色
     */
    private List<ThemeColor> mThemeColors = new ArrayList<ThemeColor>();

    /**
     * 输出歌词类型
     */
    private RadioGroup mOutputLrcTypeRadioGroup;
    /**
     * 默认歌词
     */
    private RadioButton mOutputDefLrcRadioButton;

    /**
     * 翻译歌词
     */
    private RadioButton mOutputTranslateLrcRadioButton;


    /**
     * 音译歌词
     */
    private RadioButton mOutputTransliterationLrcRadioButton;

    /**
     * 源文件路径
     */
    private TextView mLrcFilePathTv;
    private String mLrcFilePath;
    /**
     * 歌词窗口大小
     */
    private EditText mLrcDialogWidthEt;
    private EditText mLrcDialogHeightEt;
    /**
     * 歌词字体大小
     */
    private EditText mLrcFontSizeEt;
    /**
     * 歌词行高度
     */
    private EditText mLrcLineHeightEt;

    /**
     * 歌词边框间隔大小
     */
    private EditText mLrcPaddingEt;
    /**
     * 预览视图
     */
    private LrcImgPreView mLrcImgPreView;

    /**
     * 选择源文件请求码
     */
    private final int SELECTORIGFILE = 0;

    /**
     * 设置源文件路径
     */
    private final int SETORIGFILEPATH = 0;
    /**
     * 更新预览视图
     */
    private final int UPDATEPREVIEW = 1;
    /**
     * 歌词生成图片完成
     */
    private final int CREATELRCIMAGEFINISH = 2;

    private static LyricsReader mLyricsReader;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SETORIGFILEPATH:

                    if (mLrcFilePath != null && !mLrcFilePath.equals("")) {
                        mLrcFilePathTv.setText("歌词文件路径：" + mLrcFilePath);

                        initReadData();
                    } else {
                        mLrcFilePathTv.setText("歌词文件路径：");

                        resetReadData();
                    }
                    break;
                case UPDATEPREVIEW:

                    mLrcImgPreView.postInvalidate();
                    break;

                case CREATELRCIMAGEFINISH:

                    mHelper.hideLoading();
                    Toast.makeText(getApplicationContext(), "生成完成！", Toast.LENGTH_SHORT).show();

                    break;
            }
        }
    };

    private void resetReadData() {
        mLyricsReader = null;
        int outputLrcTypeRadioButtonId = mOutputLrcTypeRadioGroup.getCheckedRadioButtonId();
        if (outputLrcTypeRadioButtonId != -1) {
            int outputLrcTypeIndex = mOutputLrcTypeRadioGroup.indexOfChild(mOutputLrcTypeRadioGroup.findViewById(outputLrcTypeRadioButtonId));
            if (outputLrcTypeIndex == 0) {
                mOutputDefLrcRadioButton.setChecked(false);
            } else if (outputLrcTypeIndex == 1) {
                mOutputTranslateLrcRadioButton.setChecked(false);
            } else {
                mOutputTransliterationLrcRadioButton.setChecked(false);
            }
        }
        //
        mOutputDefLrcRadioButton.setEnabled(false);
        //
        mOutputTranslateLrcRadioButton.setEnabled(false);
        //
        mOutputTransliterationLrcRadioButton.setEnabled(false);
    }

    /**
     *
     */
    private void initReadData() {
        //
        mLyricsReader = new LyricsReader();

        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {
                File lrcFile = new File(mLrcFilePath);
                mLyricsReader.loadLrc(lrcFile);
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if (mLyricsReader.getLrcLineInfos() != null && mLyricsReader.getLrcLineInfos().size() != 0) {

                    mOutputDefLrcRadioButton.setEnabled(true);
                    mOutputDefLrcRadioButton.setChecked(true);

                    if (mLyricsReader.getTranslateLrcLineInfos() != null && mLyricsReader.getTranslateLrcLineInfos().size() != 0) {
                        mOutputTranslateLrcRadioButton.setEnabled(true);
                    }

                    if (mLyricsReader.getTransliterationLrcLineInfos() != null && mLyricsReader.getTransliterationLrcLineInfos().size() != 0) {
                        mOutputTransliterationLrcRadioButton.setEnabled(true);
                    }
                }
            }
        }.execute("");

    }


    @Override
    protected int setContentViewId() {
        return R.layout.activity_lrc_img2_video;
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
        titleView.setText("歌词图片生成器");

        //返回
        RelativeLayout backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();

            }
        });

        //选择歌词文件按钮
        Button selectLrcFileBtn = findViewById(R.id.selectLrcFile);
        selectLrcFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent selectFileIntent = new Intent(LrcImg2VideoActivity.this, FileManagerActivity.class);
                startActivityForResult(selectFileIntent, SELECTORIGFILE);

            }
        });
        //
        mLrcFilePathTv = findViewById(R.id.lrcFilePath);
        //颜色主题
        mThemeRadioGroup = findViewById(R.id.themeRG);
        mThemeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                updatePreView();
            }
        });
        mThemeRadioGroup.check(mThemeRadioButtonId[0]);

        //添加主题颜色
        //1.1添加蓝色主题
        ThemeColor themeBlueColor = new ThemeColor();
        themeBlueColor.setPaintColors(new int[]{
                ColorUtils.parserColor("#00348a"),
                ColorUtils.parserColor("#0080c0"),
                ColorUtils.parserColor("#03cafc")
        });
        themeBlueColor.setPaintHLColors(new int[]{
                ColorUtils.parserColor("#82f7fd"),
                ColorUtils.parserColor("#ffffff"),
                ColorUtils.parserColor("#03e9fc")
        });
        mThemeColors.add(themeBlueColor);

        //1.2添加白主题
        ThemeColor themeWhiteColor = new ThemeColor();
        themeWhiteColor.setPaintColors(new int[]{
                ColorUtils.parserColor("#ffffff"),
                ColorUtils.parserColor("#ffffff"),
                ColorUtils.parserColor("#ffffff")
        });
        themeWhiteColor.setPaintHLColors(new int[]{
                ColorUtils.parserColor("#ffff00"),
                ColorUtils.parserColor("#ffff00"),
                ColorUtils.parserColor("#ffff00")
        });
        mThemeColors.add(themeWhiteColor);

        //1.3添加黑主题
        ThemeColor themeBlackColor = new ThemeColor();
        themeBlackColor.setPaintColors(new int[]{
                ColorUtils.parserColor("#e1e1e1"),
                ColorUtils.parserColor("#6a6a6a"),
                ColorUtils.parserColor("#000000")
        });
        themeBlackColor.setPaintHLColors(new int[]{
                ColorUtils.parserColor("#ffffff"),
                ColorUtils.parserColor("#00ffff"),
                ColorUtils.parserColor("#80ffff")
        });
        mThemeColors.add(themeBlackColor);


        //1.4添加紫主题
        ThemeColor themePurpleColor = new ThemeColor();
        themePurpleColor.setPaintColors(new int[]{
                ColorUtils.parserColor("#400080"),
                ColorUtils.parserColor("#ff80ff"),
                ColorUtils.parserColor("#400080")
        });
        themePurpleColor.setPaintHLColors(new int[]{
                ColorUtils.parserColor("#ff3792"),
                ColorUtils.parserColor("#fff386"),
                ColorUtils.parserColor("#ff3792")
        });
        mThemeColors.add(themePurpleColor);

        //生成按钮
        final Button createBtn = findViewById(R.id.createBtn);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createLrcImage();
            }
        });

        //歌词窗口宽度
        mLrcDialogWidthEt = findViewById(R.id.lrcdialogwidth);
        Button lrcDialogWidthBtn = findViewById(R.id.lrcdialogwidthbtn);
        lrcDialogWidthBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String widthString = mLrcDialogWidthEt.getText().toString();
                if (widthString == null || widthString.equals("")) {
                    Toast.makeText(getApplicationContext(), "请输入歌词窗口宽度！", Toast.LENGTH_SHORT).show();
                } else {
                    updatePreView();
                }
            }
        });
        //歌词窗口高度
        mLrcDialogHeightEt = findViewById(R.id.lrcdialogheight);
        Button lrcDialogHeightBtn = findViewById(R.id.lrcdialogheightbtn);
        lrcDialogHeightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String heightString = mLrcDialogHeightEt.getText().toString();
                if (heightString == null || heightString.equals("")) {
                    Toast.makeText(getApplicationContext(), "请输入歌词窗口高度！", Toast.LENGTH_SHORT).show();
                } else {
                    updatePreView();
                }
            }
        });

        //歌词字体大小
        mLrcFontSizeEt = findViewById(R.id.lrcfontsize);
        Button lrcFontSizeBtn = findViewById(R.id.lrcfontsizebtn);
        lrcFontSizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fontsizeString = mLrcFontSizeEt.getText().toString();
                if (fontsizeString == null || fontsizeString.equals("")) {
                    Toast.makeText(getApplicationContext(), "请输入歌词字体大小！", Toast.LENGTH_SHORT).show();
                } else {
                    updatePreView();
                }
            }
        });

        //歌词行高度
        mLrcLineHeightEt = findViewById(R.id.lrclineheight);
        Button lrcLineHeightBtn = findViewById(R.id.lrclineheightbtn);
        lrcLineHeightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lineHeightString = mLrcLineHeightEt.getText().toString();
                if (lineHeightString == null || lineHeightString.equals("")) {
                    Toast.makeText(getApplicationContext(), "请输入歌词行间隔大小！", Toast.LENGTH_SHORT).show();
                } else {
                    updatePreView();
                }
            }
        });

        //歌词边框间隔大小
        mLrcPaddingEt = findViewById(R.id.lrcpadding);
        Button lrcPaddingBtn = findViewById(R.id.lrcpaddingbtn);
        lrcPaddingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String paddingString = mLrcPaddingEt.getText().toString();
                if (paddingString == null || paddingString.equals("")) {
                    Toast.makeText(getApplicationContext(), "请输入左右边框间隔大小！", Toast.LENGTH_SHORT).show();
                } else {
                    updatePreView();
                }
            }
        });
        //预览视图
        mLrcImgPreView = findViewById(R.id.lrcImgPreView);
        //设置字体文件
        Typeface typeFace = Typeface.createFromAsset(getAssets(),
                "fonts/weiruanyahei14M.ttf");
        mLrcImgPreView.setTypeFace(typeFace);
        mLrcImgPreView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mLrcImgPreView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                updatePreView();
            }
        });

        //设置输出歌词类型
        mOutputLrcTypeRadioGroup = findViewById(R.id.outputlrctype);

        mOutputDefLrcRadioButton = findViewById(R.id.outputlrctypedef);
        mOutputDefLrcRadioButton.setEnabled(false);

        mOutputTranslateLrcRadioButton = findViewById(R.id.outputlrctypeextra1);
        mOutputTranslateLrcRadioButton.setEnabled(false);

        mOutputTransliterationLrcRadioButton = findViewById(R.id.outputlrctypeextra2);
        mOutputTransliterationLrcRadioButton.setEnabled(false);

        mOutputLrcTypeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                updatePreView();
            }
        });
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
    }

    /**
     * 更新预览视图
     */
    private void updatePreView() {
        if (mLrcImgPreView == null) return;
        int preViewHeight = mLrcImgPreView.getHeight();
        if (check()) {
            //视图高度
            String viewHeightString = mLrcDialogHeightEt.getText().toString();
            int viewHeight = Integer.parseInt(viewHeightString);
            float scale = viewHeight * 1.0f / preViewHeight;
            //字体大小
            String fontsizeString = mLrcFontSizeEt.getText().toString();
            float lrcFontSize = Float.parseFloat(fontsizeString);

            //歌词空行高度
            String lineHeightString = mLrcLineHeightEt.getText().toString();
            float lineHeight = Float.parseFloat(lineHeightString);

            //歌词左右间隔
            String paddingString = mLrcPaddingEt.getText().toString();
            float padding = Float.parseFloat(paddingString);

            //按照比例缩放字体
            float fontSize = ScreenUtil.px2sp(getApplicationContext(), lrcFontSize * scale);
            mLrcImgPreView.setFontSize(fontSize);
            mLrcImgPreView.setSpaceLineHeight(lineHeight);
            mLrcImgPreView.setPaddingLeftOrRight(padding);

            //设置字体颜色
            int checkedRadioButtonId = mThemeRadioGroup.getCheckedRadioButtonId();
            //获取输出格式索引
            int index = mThemeRadioGroup.indexOfChild(mThemeRadioGroup.findViewById(checkedRadioButtonId));
            ThemeColor themeColor = mThemeColors.get(index);
            mLrcImgPreView.setPaintColor(themeColor.getPaintColors());
            mLrcImgPreView.setPaintHLColor(themeColor.getPaintHLColors());

            //获取输出歌词格式类型
            int outputLrcTypeRadioButtonId = mOutputLrcTypeRadioGroup.getCheckedRadioButtonId();
            if (outputLrcTypeRadioButtonId != -1) {
                //获取输出类型
                int outputLrcTypeIndex = mOutputLrcTypeRadioGroup.indexOfChild(mOutputLrcTypeRadioGroup.findViewById(outputLrcTypeRadioButtonId));
                if (outputLrcTypeIndex == 0) {
                    mLrcImgPreView.setShowLrcType(LrcImgPreView.SHOWDEFLRC);
                } else {
                    mLrcImgPreView.setShowLrcType(LrcImgPreView.SHOWEXTRALRC);
                }
            }

            mHandler.sendEmptyMessage(UPDATEPREVIEW);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    static int mViewWidth;
    static int mViewHeight;
    static float mLineHeight;
    static float mPaddingLeftOrRight;
    static float mFontSize;
    static Paint mPaint;
    static Paint mPaintHL;
    static Paint mPaintOutline;
    static int[] mPaintColor;
    static int[] mPaintHLColor;
    static int mOutputLrcType;
    static int mPlayRate;
    static long mMaxProgress;
    static String mLrcFileParentPath;
    static String mLrcFileName;


    /**
     * 生成歌词图片
     */
    private void createLrcImage() {
        if (mLrcFilePath == null || mLrcFilePath.equals("")) {
            Toast.makeText(getApplicationContext(), "请选择歌词文件！", Toast.LENGTH_SHORT).show();
            return;
        }

        File lrcFile = new File(mLrcFilePath);
        if (!lrcFile.exists()) {
            Toast.makeText(getApplicationContext(), "歌词文件不存在，请重新选择歌词文件！", Toast.LENGTH_SHORT).show();
            return;
        }

        int outputLrcTypeRadioButtonId = mOutputLrcTypeRadioGroup.getCheckedRadioButtonId();
        if (outputLrcTypeRadioButtonId == -1) {
            Toast.makeText(getApplicationContext(), "请选择输出歌词类型！", Toast.LENGTH_SHORT).show();
            return;
        }

        if (check()) {

            //视图宽度
            String viewWidthString = mLrcDialogWidthEt.getText().toString();
            mViewWidth = Integer.parseInt(viewWidthString);

            //视图高度
            String viewHeightString = mLrcDialogHeightEt.getText().toString();
            mViewHeight = Integer.parseInt(viewHeightString);

            //字体大小
            String fontsizeString = mLrcFontSizeEt.getText().toString();
            float lrcFontSize = Float.parseFloat(fontsizeString);


            //歌词空行高度
            String lineHeightString = mLrcLineHeightEt.getText().toString();
            mLineHeight = Float.parseFloat(lineHeightString);

            //歌词左右间隔
            String paddingString = mLrcPaddingEt.getText().toString();
            mPaddingLeftOrRight = Float.parseFloat(paddingString);

            mFontSize = ScreenUtil.px2sp(getApplicationContext(), lrcFontSize);

            //设置字体文件
            Typeface typeFace = Typeface.createFromAsset(getAssets(),
                    "fonts/weiruanyahei14M.ttf");
            //默认画笔
            mPaint = new Paint();
            mPaint.setDither(true);
            mPaint.setAntiAlias(true);
            mPaint.setTextSize(mFontSize);
            mPaint.setTypeface(typeFace);

            //高亮画笔
            mPaintHL = new Paint();
            mPaintHL.setDither(true);
            mPaintHL.setAntiAlias(true);
            mPaintHL.setTextSize(mFontSize);
            mPaintHL.setTypeface(typeFace);

            //轮廓画笔
            mPaintOutline = new Paint();
            mPaintOutline.setDither(true);
            mPaintOutline.setAntiAlias(true);
            mPaintOutline.setColor(Color.BLACK);
            mPaintOutline.setTextSize(mFontSize);
            mPaintOutline.setTypeface(typeFace);

            //获取输出格式索引
            int index = mThemeRadioGroup.indexOfChild(mThemeRadioGroup.findViewById(mThemeRadioGroup.getCheckedRadioButtonId()));
            ThemeColor themeColor = mThemeColors.get(index);
            mPaintColor = themeColor.getPaintColors();
            mPaintHLColor = themeColor.getPaintHLColors();

            //
            TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = mLyricsReader.getLrcLineInfos();
            if (lyricsLineInfos == null) {
                Toast.makeText(getApplicationContext(), "歌词内容为空，生成失败！", Toast.LENGTH_SHORT).show();
                return;
            }
            //
            mMaxProgress = mLyricsReader.getLyricsInfo().getTotal();
            if (mMaxProgress == 0) {
                if (mLyricsReader.getLyricsType() == LyricsInfo.LRC) {
                    Toast.makeText(getApplicationContext(), "歌词内容中不含有歌曲总时长信息（[total]标签），不支持生成！", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    mMaxProgress = lyricsLineInfos.get(lyricsLineInfos.size() - 1).getEndTime();
                }
            }

            mOutputLrcType = -1;
            //获取输出歌词格式类型
            int outputLrcTypeIndex = mOutputLrcTypeRadioGroup.indexOfChild(mOutputLrcTypeRadioGroup.findViewById(outputLrcTypeRadioButtonId));
            if (outputLrcTypeIndex == 0) {
                mOutputLrcType = -1;
            } else if (outputLrcTypeIndex == 1) {
                mOutputLrcType = 1;
            } else {
                mOutputLrcType = 0;
            }
            mLrcFileParentPath = lrcFile.getParent();
            mLrcFileName = lrcFile.getName();

            mPlayRate = 10;//每1ms播放进度
            logger.e("mMaxProgress=" + mMaxProgress);
            mHelper.showLoading("正在生成，请稍等...");
            long startTime = System.currentTimeMillis();
            logger.e("startTime=" + startTime);

            Intent intent = new Intent(getApplicationContext(), CreateLrcImageIntentService.class);
            intent.putExtra("startProgressIndex", 0);
            intent.putExtra("endProgressIndex", mMaxProgress);
            intent.putExtra("startTime", startTime);
            startService(intent);

            CreateLrcImageIntentService.setUpdateUI(new CreateLrcImageIntentService.UpdateUI() {
                @Override
                public void refreshLoadingText(String text) {
                    mHelper.refreshLoadingText(text);
                }

                @Override
                public void finish() {
                    mHandler.sendEmptyMessage(CREATELRCIMAGEFINISH);
                }

                @Override
                public void loge(String e) {
                    logger.e(e);
                }
            });
        }
    }

    /**
     * 创建歌词图片
     */
    public static class CreateLrcImageIntentService extends IntentService {

        private static UpdateUI mUpdateUI;

        public CreateLrcImageIntentService() {
            super("CreateLrcImageIntentService");
        }

        @Override
        protected void onHandleIntent(@Nullable Intent intent) {
            long startTime = intent.getLongExtra("startTime", 0);
            long startProgressIndex = intent.getLongExtra("startProgressIndex", 0);
            long endProgressIndex = intent.getLongExtra("endProgressIndex", 0);
            long curPlayingTime = startProgressIndex;

            TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = mLyricsReader.getLrcLineInfos();
            while (curPlayingTime <= endProgressIndex) {

                Bitmap lrcImageBitmap = null;
                if (mOutputLrcType == -1) {
                    lrcImageBitmap = LyricsUtils.getDynamicLyricsImage(mLyricsReader.getLyricsType(), mViewWidth, mViewHeight, mViewWidth, mLineHeight, mPaddingLeftOrRight, mPaint, mPaintHL, mPaintOutline, mPaintColor, mPaintHLColor, lyricsLineInfos, curPlayingTime, mLyricsReader.getPlayOffset());
                } else if (mOutputLrcType == 1) {
                    //翻译
                    lrcImageBitmap = LyricsUtils.getDynamiAndExtraLyricsImage(mLyricsReader.getLyricsType(), mViewWidth, mViewHeight, mLineHeight, mPaddingLeftOrRight, mPaint, mPaintHL, mPaintOutline, mPaintColor, mPaintHLColor, lyricsLineInfos, mLyricsReader.getTranslateLrcLineInfos(), 1, curPlayingTime, mLyricsReader.getPlayOffset());
                } else if (mOutputLrcType == 0) {
                    //音译
                    lrcImageBitmap = LyricsUtils.getDynamiAndExtraLyricsImage(mLyricsReader.getLyricsType(), mViewWidth, mViewHeight, mLineHeight, mPaddingLeftOrRight, mPaint, mPaintHL, mPaintOutline, mPaintColor, mPaintHLColor, lyricsLineInfos, mLyricsReader.getTransliterationLrcLineInfos(), 0, curPlayingTime, mLyricsReader.getPlayOffset());
                }

                if (lrcImageBitmap == null) {
                    if (mUpdateUI != null) {
                        mUpdateUI.loge("歌词部分内容生成图片失败!当前播放时长为：" + TimeUtils.parseMMSSFFString((int) curPlayingTime));
                    }

                    break;
                }
                //文件名称
                String fileName = String.format("%0" + (mMaxProgress + "").length() + "d", curPlayingTime);
                if (mUpdateUI != null) {
                    mUpdateUI.loge("fileName=" + fileName + ".png");
                }

                //输出文件
                String outFilePath = mLrcFileParentPath + File.separator + FileUtils.removeExt(mLrcFileName) + File.separator + (curPlayingTime / (10 * 1000)) + File.separator + fileName + ".png";
                boolean result = ImageUtil.savePngImage(lrcImageBitmap, outFilePath);
                if (!result) {

                    if (mUpdateUI != null) {
                        mUpdateUI.loge("歌词部分内容保存图片失败!当前播放时长为：" + TimeUtils.parseMMSSFFString((int) curPlayingTime));
                    }

                    break;
                }

                if (mUpdateUI != null) {
                    mUpdateUI.refreshLoadingText("正在生成图片，进度：" + String.format("%0" + (mMaxProgress + "").length() + "d", curPlayingTime) + "/" + mMaxProgress);
                }
                //
                curPlayingTime += mPlayRate;
            }

            long endTime = System.currentTimeMillis();
            if (mUpdateUI != null) {
                mUpdateUI.loge("endTime=" + endTime);
            }

            long spendTime = endTime - startTime;
            if (mUpdateUI != null) {
                mUpdateUI.loge("spendTime=" + spendTime + " 生成耗时：" + TimeUtils.parseMMSSFFString((int) spendTime));
            }

            if (mUpdateUI != null) {
                mUpdateUI.finish();
            }

        }

        public static void setUpdateUI(UpdateUI mUpdateUI) {
            CreateLrcImageIntentService.mUpdateUI = mUpdateUI;
        }

        public interface UpdateUI {
            void refreshLoadingText(String text);

            void finish();

            void loge(String e);
        }
    }


    /**
     * 检查
     *
     * @return
     */
    private boolean check() {

        String widthString = mLrcDialogWidthEt.getText().toString();
        if (widthString == null || widthString.equals("")) {
            Toast.makeText(getApplicationContext(), "请输入歌词窗口宽度！", Toast.LENGTH_SHORT).show();
            return false;
        }


        String heightString = mLrcDialogHeightEt.getText().toString();
        if (heightString == null || heightString.equals("")) {
            Toast.makeText(getApplicationContext(), "请输入歌词窗口高度！", Toast.LENGTH_SHORT).show();
            return false;
        }

        String fontsizeString = mLrcFontSizeEt.getText().toString();
        if (fontsizeString == null || fontsizeString.equals("")) {
            Toast.makeText(getApplicationContext(), "请输入歌词字体大小！", Toast.LENGTH_SHORT).show();
            return false;
        }


        String lineHeightString = mLrcLineHeightEt.getText().toString();
        if (lineHeightString == null || lineHeightString.equals("")) {
            Toast.makeText(getApplicationContext(), "请输入歌词行间隔大小！", Toast.LENGTH_SHORT).show();
            return false;
        }


        String paddingString = mLrcPaddingEt.getText().toString();
        if (paddingString == null || paddingString.equals("")) {
            Toast.makeText(getApplicationContext(), "请输入左右边框间隔大小！", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }

    @Override
    public int setStatusBarParentView() {
        return R.id.lrcimgvideo_layout;
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECTORIGFILE) {
            if (resultCode == Activity.RESULT_OK) {

                mLrcFilePath = data.getStringExtra("selectFilePath");
                if (mLrcFilePath != null && !mLrcFilePath.equals("")) {
                    String ext = FileUtils.getFileExt(mLrcFilePath);
                    if (!ext.equals("krc") && !ext.equals("ksc") && !ext.equals("hrc") && !ext.equals("lrc")) {
                        Toast.makeText(getApplicationContext(), "请选择支持的歌词文件！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            } else {
                mLrcFilePath = null;
            }
            mHandler.sendEmptyMessage(SETORIGFILEPATH);
        }
    }

    private class ThemeColor {

        /**
         * 默认画笔颜色
         */
        public int[] mPaintColors;

        /**
         * 高亮颜色
         */
        public int[] mPaintHLColors;

        public int[] getPaintColors() {
            return mPaintColors;
        }

        public void setPaintColors(int[] mPaintColors) {
            this.mPaintColors = mPaintColors;
        }

        public int[] getPaintHLColors() {
            return mPaintHLColors;
        }

        public void setPaintHLColors(int[] mPaintHLColors) {
            this.mPaintHLColors = mPaintHLColors;
        }
    }
}
