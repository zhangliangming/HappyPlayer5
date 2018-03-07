package base.lyrics.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.zlm.hp.R;

import java.util.List;
import java.util.TreeMap;

import base.lyrics.LyricsReader;
import base.lyrics.model.LyricsInfo;
import base.lyrics.model.LyricsLineInfo;
import base.lyrics.utils.ColorUtils;
import base.lyrics.utils.LyricsUtils;

/**
 * 歌词视图抽象类
 * Created by zhangliangming on 2018-02-25.
 */

public abstract class AbstractLrcView extends View {
    /**
     * 初始
     */
    public static final int LRCSTATUS_INIT = 0;
    /**
     * 加载中
     */
    public static final int LRCSTATUS_LOADING = 1;
    /**
     * 绘画歌词
     */
    public static final int LRCSTATUS_LRC = 4;
    /**
     * 绘画歌词出错
     */
    public static final int LRCSTATUS_ERROR = 5;
    /**
     * 不支持格式
     */
    public static final int LRCSTATUS_NONSUPPORT = 6;

    /**
     * 没有的额外的歌词
     */
    public static final int EXTRALRCTYPE_NOLRC = 0;
    /**
     * 翻译歌词
     */
    public static final int EXTRALRCTYPE_TRANSLATELRC = 1;
    /**
     * 音译歌词
     */
    public static final int EXTRALRCTYPE_TRANSLITERATIONLRC = 2;
    /**
     * 翻译和音译歌词
     */
    public static final int EXTRALRCTYPE_BOTH = 3;

    /**
     * 初始
     */
    public static final int LRCPLAYERSTATUS_INIT = 0;
    /**
     * 播放
     */
    public static final int LRCPLAYERSTATUS_PLAY = 1;
    /**
     * 无歌词-去搜索（SearchLyricsListener不为空时）
     */
    public final int LRCSTATUS_NOLRC_GOTOSEARCH = 2;
    /**
     * 无歌词-显示默认文本
     */
    public final int LRCSTATUS_NOLRC_DEFTEXT = 3;

    /**
     * 默认歌词画笔
     */
    public Paint mPaint;
    /**
     * 默认画笔颜色
     */
    public int[] mPaintColors = new int[]{
            ColorUtils.parserColor("#00348a"),
            ColorUtils.parserColor("#0080c0"),
            ColorUtils.parserColor("#03cafc")
    };
    /**
     * 高亮歌词画笔
     */
    public Paint mPaintHL;
    //高亮颜色
    public int[] mPaintHLColors = new int[]{
            ColorUtils.parserColor("#82f7fd"),
            ColorUtils.parserColor("#ffffff"),
            ColorUtils.parserColor("#03e9fc")
    };
    /**
     * 轮廓画笔
     */
    public Paint mPaintOutline;
    /**
     * 默认提示文本
     */
    public String mDefText;
    /**
     * 正在加载提示文本
     */
    public String mLoadingText;
    /**
     * 加载歌词出错
     */
    public String mLoadErrorText;
    /**
     * 不支持歌词格式文本
     */
    public String mNonsupportText;
    /**
     * 搜索提示文本
     */
    public String mGotoSearchText;

    /**
     * 搜索歌词区域
     */
    private RectF mGotoSearchBtnRect;
    /**
     * 是否在去搜索歌词矩形区域内
     */
    private boolean isInGotoSearchBtnRect = false;

    /**
     * 绘画去搜索歌词文字矩形画笔
     */
    public Paint mGotoSearchRectPaint;

    /**
     * 去搜索歌词文字颜色
     */
    public int mGotoSearchTextColor = ColorUtils.parserColor("#0288d1");

    /**
     * 绘画去搜索歌词文字画笔
     */
    public Paint mGotoSearchTextPaint;
    /**
     * 按下搜索歌词文字颜色
     */
    public int mGotoSearchTextPressedColor = ColorUtils.parserColor("#ffffff");

    /**
     * 歌词状态
     */
    public int mLrcStatus = LRCSTATUS_INIT;
    /**
     * 搜索歌词回调
     */
    public SearchLyricsListener mSearchLyricsListener;

    /**
     * 显示翻译歌词
     */
    public static final int EXTRALRCSTATUS_SHOWTRANSLATELRC = 0;
    /**
     * 显示音译歌词
     */
    public static final int EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC = 1;
    /**
     * 只显示默认歌词
     */
    public static final int EXTRALRCSTATUS_NOSHOWEXTRALRC = 2;
    /**
     * 默认只显示默认歌词
     */
    public int mExtraLrcStatus = EXTRALRCSTATUS_NOSHOWEXTRALRC;
    /**
     * 是否是手动去设置显示的歌词类型
     */
    public boolean isHandToChangeExtraLrcStatus = false;

    /**
     * 空行高度
     */
    public int mSpaceLineHeight = 60;
    /**
     * 歌词字体大小
     */
    public int mFontSize = 30;

    /**
     * 左右间隔距离
     */
    public int mPaddingLeftOrRight = 15;
    /**
     * 歌词处理类
     */
    public LyricsReader mLyricsReader;
    /**
     * 歌词的最大宽度
     */
    public int mTextMaxWidth = 0;
    /**
     * 当前歌词的所在行数
     */
    public int mLyricsLineNum = 0;
    /**
     * 分割歌词的行索引
     */
    public int mSplitLyricsLineNum = 0;
    /**
     * 当前歌词的第几个字
     */
    public int mLyricsWordIndex = -1;
    /**
     * 分割歌词当前歌词的第几个字
     */
    public int mSplitLyricsWordIndex = -1;
    /**
     * 当前歌词第几个字 已经播放的长度
     */
    public float mLineLyricsHLWidth = 0;
    /**
     * 当前歌词第几个字 已经播放的时间
     */
    public float mLyricsWordHLTime = 0;
    /**
     * 高亮歌词的X移动位置
     */
    public float mHighLightLrcMoveX = 0;

    //////////////////////////////////翻译歌词和音译歌词///////////////////////////////////////
    /**
     * 歌词列表
     */
    public TreeMap<Integer, LyricsLineInfo> mLrcLineInfos;
    /**
     * 翻译行歌词列表
     */
    public List<LyricsLineInfo> mTranslateLrcLineInfos;
    /**
     * 音译歌词行
     */
    public List<LyricsLineInfo> mTransliterationLrcLineInfos;
    /**
     * 额外的歌词类型
     */
    public int mExtraLrcType = EXTRALRCTYPE_NOLRC;
    /**
     * 额外歌词监听事件
     */
    public ExtraLyricsListener mExtraLyricsListener;
    /**
     * 额外歌词空行高度
     */
    public int mExtraLrcSpaceLineHeight = 30;
    /**
     * 额外歌词字体大小
     */
    public int mExtraLrcFontSize = 30;
    /**
     * 额外歌词画笔
     */
    public Paint mExtraLrcPaint;
    /**
     * 额外歌词高亮画笔
     */
    public Paint mExtraLrcPaintHL;
    /**
     * 轮廓画笔
     */
    public Paint mExtraLrcPaintOutline;
    /**
     * 当前额外分割歌词的所在行数
     */
    public int mExtraSplitLyricsLineNum = 0;
    /**
     * 当前额外歌词的第几个字
     */
    public int mExtraLyricsWordIndex = -1;
    /**
     * 当前额外分割歌词的第几个字
     */
    public int mExtraSplitLyricsWordIndex = -1;
    /**
     * 额外歌词当前歌词第几个字 已经播放的长度
     */
    public float mExtraLyricsLineHLWidth = 0;

    /**
     * 额外歌词高亮歌词的X移动位置
     */
    public float mExtraLyricsHighLightMoveX = 0;

    /**
     * 绘画类型：lrc类型
     */
    public static final int TRANSLATE_DRAW_TYPE_LRC = 0;
    /**
     * 绘画类型：动感歌词类型
     */
    public static final int TRANSLATE_DRAW_TYPE_DYNAMIC = 1;
    /**
     * 翻译歌词绘画类型
     */
    public int mTranslateDrawType = TRANSLATE_DRAW_TYPE_LRC;

    /**
     * 翻译歌词的高亮宽度
     */
    public float mTranslateLyricsWordHLTime = 0;

    ///////////////////////////////歌词绘画播放器//////////////////////////////////

    private byte[] lock = new byte[0];
    /**
     * 播放器类型
     */
    private int mLrcPlayerStatus = LRCPLAYERSTATUS_INIT;

    /**
     * 播放器开始时间，用于计算歌曲播放的时长
     */
    private long mPlayerStartTime = 0;
    /**
     * 播放器开始后，所经历的播放时长
     */
    private long mPlayerSpendTime = 0;

    /**
     * 当前播放进度
     */
    private int mCurPlayingTime = 0;

    /**
     * 歌词刷新
     */
    private Handler mLrcPlayerHandler = new Handler();
    /**
     * 歌词刷新线程
     */
    private Runnable mLrcPlayerRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (lock) {
                if (mLrcPlayerStatus == LRCPLAYERSTATUS_PLAY) {
                    //
                    updateView((int) (mCurPlayingTime + mPlayerSpendTime));
                    invalidateView();
                    //
                    long endTime = System.currentTimeMillis();
                    mPlayerSpendTime = (endTime - mPlayerStartTime);
                    mLrcPlayerHandler.postDelayed(mLrcPlayerRunnable, 0);
                }
            }
        }
    };

    public AbstractLrcView(Context context) {
        super(context);
        init(context);
    }

    public AbstractLrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * @param context
     */
    protected void init(Context context) {

        //初始默认数据
        mDefText = context.getString(R.string.def_text);
        mLoadingText = context.getString(R.string.loading_text);
        mLoadErrorText = context.getString(R.string.load_error_text);
        mNonsupportText = context.getString(R.string.nonsupport_text);
        mGotoSearchText = context.getString(R.string.goto_search_text);

        //默认画笔
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mFontSize);

        //高亮画笔
        mPaintHL = new Paint();
        mPaintHL.setDither(true);
        mPaintHL.setAntiAlias(true);
        mPaintHL.setTextSize(mFontSize);

        //轮廓画笔
        mPaintOutline = new Paint();
        mPaintOutline.setDither(true);
        mPaintOutline.setAntiAlias(true);
        mPaintOutline.setColor(Color.BLACK);
        mPaintOutline.setTextSize(mFontSize);

        //额外歌词画笔
        mExtraLrcPaint = new Paint();
        mExtraLrcPaint.setDither(true);
        mExtraLrcPaint.setAntiAlias(true);
        mExtraLrcPaint.setTextSize(mExtraLrcFontSize);

        //额外高亮歌词画笔
        mExtraLrcPaintHL = new Paint();
        mExtraLrcPaintHL.setDither(true);
        mExtraLrcPaintHL.setAntiAlias(true);
        mExtraLrcPaintHL.setTextSize(mExtraLrcFontSize);

        //额外画笔轮廓
        mExtraLrcPaintOutline = new Paint();
        mExtraLrcPaintOutline.setDither(true);
        mExtraLrcPaintOutline.setAntiAlias(true);
        mExtraLrcPaintOutline.setColor(Color.BLACK);
        mExtraLrcPaintOutline.setTextSize(mExtraLrcFontSize);

        //绘画去搜索歌词画笔
        mGotoSearchTextPaint = new Paint();
        mGotoSearchTextPaint.setDither(true);
        mGotoSearchTextPaint.setAntiAlias(true);
        mGotoSearchTextPaint.setTextSize(mFontSize);

        //绘画去搜索歌词矩形画笔
        mGotoSearchRectPaint = new Paint();
        mGotoSearchRectPaint.setDither(true);
        mGotoSearchRectPaint.setAntiAlias(true);
        mGotoSearchRectPaint.setStrokeWidth(2);
        mGotoSearchRectPaint.setTextSize(mFontSize);

        viewInit(context);
        //加载完成后回调
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                viewLoadFinish();
            }
        });
    }

    /**
     * 绘画去搜索歌词按钮
     *
     * @param canvas
     * @param paintRect 画矩形画笔
     * @param paintText 画文字画笔
     * @param btnText   按钮提示文字
     */
    public void drawGoToSearchBtn(Canvas canvas, Paint paintRect, Paint paintText, String btnText) {
        if (isInGotoSearchBtnRect) {
            paintRect.setStyle(Paint.Style.FILL);
            paintText.setColor(mGotoSearchTextPressedColor);
        } else {
            paintRect.setStyle(Paint.Style.STROKE);
            paintText.setColor(mGotoSearchTextColor);
        }
        paintRect.setColor(mGotoSearchTextColor);

        //
        int textY = (getHeight() + getTextHeight(paintText)) / 2;
        int textWidth = (int) getTextWidth(paintText, btnText);
        int textX = (getWidth() - textWidth) / 2;


        //初始化搜索
        if (mGotoSearchBtnRect == null) {
            int padding = getRealTextHeight(paintText) / 2;
            int rectTop = textY - getTextHeight(paintText) - padding;
            int rectLeft = textX - padding;
            int rectRight = rectLeft + textWidth + padding * 2;
            int rectBottom = textY + padding;
            mGotoSearchBtnRect = new RectF(rectLeft, rectTop, rectRight, rectBottom);
        }

        canvas.drawRoundRect(mGotoSearchBtnRect, 15, 15, paintRect);
        canvas.drawText(btnText, textX, textY, paintText);
    }

    /**
     * 绘画文本
     *
     * @param canvas
     * @param paint  默认画笔
     * @param text   文本
     * @param x
     * @param y
     */
    public void drawText(Canvas canvas, Paint paint, String text, float x, float y) {
        //设置为上下渐变
        LinearGradient linearGradient = new LinearGradient(x, y - getTextHeight(paint), x, y, mPaintColors, null, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        canvas.drawText(text, x, y, paint);
    }

    /**
     * 绘画高亮文本
     *
     * @param canvas
     * @param paintHL 默认画笔
     * @param text    文本
     * @param x
     * @param y
     */
    public void drawHLText(Canvas canvas, Paint paintHL, String text, float x, float y) {
        //设置为上下渐变
        LinearGradient linearGradient = new LinearGradient(x, y - getTextHeight(paintHL), x, y, mPaintHLColors, null, Shader.TileMode.CLAMP);
        paintHL.setShader(linearGradient);
        canvas.drawText(text, x, y, paintHL);
    }

    /**
     * 绘画动感文本
     *
     * @param canvas
     * @param paint   默认画笔
     * @param paintHL 高亮画笔
     * @param text    文本
     * @param hlWidth 高亮宽度
     * @param x
     * @param y
     */
    public void drawDynamicText(Canvas canvas, Paint paint, Paint paintHL, String text, float hlWidth, float x, float y) {
        canvas.save();

        //设置为上下渐变
        LinearGradient linearGradient = new LinearGradient(x, y - getTextHeight(paint), x, y, mPaintColors, null, Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);
        canvas.drawText(text, x, y, paint);
        //设置动感歌词过渡效果
        canvas.clipRect(x, y - getRealTextHeight(paint), x + hlWidth,
                y + getRealTextHeight(paint));

        //设置为上下渐变
        LinearGradient linearGradientHL = new LinearGradient(x, y - getTextHeight(paint), x, y, mPaintHLColors, null, Shader.TileMode.CLAMP);
        paintHL.setShader(linearGradientHL);
        canvas.drawText(text, x, y, paintHL);
        canvas.restore();
    }

    /**
     * 描绘轮廓
     *
     * @param canvas
     * @param text
     * @param x
     * @param y
     */
    public void drawOutline(Canvas canvas, Paint paint, String text, float x, float y) {
        canvas.drawText(text, x - 1, y, paint);
        canvas.drawText(text, x + 1, y, paint);
        canvas.drawText(text, x, y + 1, paint);
        canvas.drawText(text, x, y - 1, paint);
    }

    /**
     * 获取行歌词高亮的宽度
     *
     * @param paint
     * @param lyricsLineInfo
     * @param lyricsWordIndex
     * @param lyricsWordHLTime
     * @return
     */
    public float getLineLyricsHLWidth(int lyricsType, Paint paint, LyricsLineInfo lyricsLineInfo, int lyricsWordIndex, float lyricsWordHLTime) {
        float lineLyricsHLWidth = 0;

        // 当行歌词
        String curLyrics = lyricsLineInfo.getLineLyrics();
        float curLrcTextWidth = getTextWidth(paint, curLyrics);
        if (lyricsType == LyricsInfo.LRC || lyricsWordIndex == -2) {
            // 整行歌词
            lineLyricsHLWidth = curLrcTextWidth;
        } else {
            if (lyricsWordIndex != -1) {
                String lyricsWords[] = lyricsLineInfo.getLyricsWords();
                int wordsDisInterval[] = lyricsLineInfo
                        .getWordsDisInterval();
                // 当前歌词之前的歌词
                StringBuilder lyricsBeforeWord = new StringBuilder();
                for (int i = 0; i < lyricsWordIndex; i++) {
                    lyricsBeforeWord.append(lyricsWords[i]);
                }
                // 当前歌词字
                String lrcNowWord = lyricsWords[lyricsWordIndex].trim();// 去掉空格
                // 当前歌词之前的歌词长度
                float lyricsBeforeWordWidth = paint
                        .measureText(lyricsBeforeWord.toString());

                // 当前歌词长度
                float lyricsNowWordWidth = paint.measureText(lrcNowWord);

                float len = lyricsNowWordWidth
                        / wordsDisInterval[lyricsWordIndex]
                        * lyricsWordHLTime;
                lineLyricsHLWidth = lyricsBeforeWordWidth + len;
            }
        }

        return lineLyricsHLWidth;
    }

    /**
     * 获取高亮移动的x位置（注：该方法在歌词不换行时使用）
     *
     * @param curLrcTextWidth
     * @param lineLyricsHLWidth
     * @param highLightLrcMoveX
     * @return
     */
    public float getHLMoveTextX(float curLrcTextWidth, float lineLyricsHLWidth, float highLightLrcMoveX) {
        float textX = 0;
        if (curLrcTextWidth > getWidth()) {
            if (lineLyricsHLWidth >= getWidth() / 2) {
                if ((curLrcTextWidth - lineLyricsHLWidth) >= getWidth() / 2) {
                    highLightLrcMoveX = (getWidth() / 2 - lineLyricsHLWidth);
                } else {
                    highLightLrcMoveX = getWidth() - curLrcTextWidth
                            - mPaddingLeftOrRight;
                }
            } else {
                highLightLrcMoveX = mPaddingLeftOrRight;
            }
            // 如果歌词宽度大于view的宽，则需要动态设置歌词的起始x坐标，以实现水平滚动
            textX = highLightLrcMoveX;
        } else {
            // 如果歌词宽度小于view的宽
            textX = (getWidth() - curLrcTextWidth) / 2;
        }
        return textX;
    }

    /**
     * 播放
     *
     * @param curPlayingTime
     */
    public void play(int curPlayingTime) {
        synchronized (lock) {
            this.mCurPlayingTime = curPlayingTime;
            mLrcPlayerStatus = LRCPLAYERSTATUS_PLAY;
            mPlayerStartTime = System.currentTimeMillis();
            mPlayerSpendTime = 0;
            mLrcPlayerHandler.postDelayed(mLrcPlayerRunnable, 0);
        }
    }

    /**
     * 跳转
     *
     * @param curPlayingTime
     */
    public void seekto(int curPlayingTime) {
        synchronized (lock) {
            if (mLrcPlayerStatus == LRCPLAYERSTATUS_PLAY) {
                mLrcPlayerStatus = LRCPLAYERSTATUS_INIT;
                mLrcPlayerHandler.removeCallbacks(mLrcPlayerRunnable);

                play(curPlayingTime);
            } else {
                this.mCurPlayingTime = curPlayingTime;
                mPlayerSpendTime = 0;
                updateView(curPlayingTime);
                invalidateView();
            }
        }
    }

    /**
     * 暂停
     */
    public void pause() {
        synchronized (lock) {
            if (mLrcPlayerStatus == LRCPLAYERSTATUS_PLAY) {
                mLrcPlayerStatus = LRCPLAYERSTATUS_INIT;
                mLrcPlayerHandler.removeCallbacks(mLrcPlayerRunnable);
            }
            mCurPlayingTime += mPlayerSpendTime;
            mPlayerSpendTime = 0;
        }
    }

    /**
     * 唤醒
     */
    public void resume() {
        synchronized (lock) {
            mLrcPlayerStatus = LRCPLAYERSTATUS_PLAY;
            mPlayerStartTime = System.currentTimeMillis();
            mPlayerSpendTime = 0;
            mLrcPlayerHandler.postDelayed(mLrcPlayerRunnable, 0);
        }
    }


    /**
     * 重置数据
     */
    public void resetData() {
        isHandToChangeExtraLrcStatus = false;
        mExtraLrcStatus = EXTRALRCSTATUS_NOSHOWEXTRALRC;
        mLyricsLineNum = 0;
        mSplitLyricsLineNum = 0;
        mLyricsWordIndex = -1;
        mSplitLyricsWordIndex = -1;
        mLineLyricsHLWidth = 0;
        mLyricsWordHLTime = 0;
        mHighLightLrcMoveX = 0;

        //
        mLrcLineInfos = null;
        mTranslateLrcLineInfos = null;
        mTransliterationLrcLineInfos = null;
        mExtraSplitLyricsLineNum = 0;
        mExtraLyricsWordIndex = -1;
        mExtraSplitLyricsWordIndex = -1;
        mExtraLyricsLineHLWidth = 0;
        mExtraLyricsHighLightMoveX = 0;
        mTranslateLyricsWordHLTime = 0;
        //
        mLrcPlayerStatus = LRCPLAYERSTATUS_INIT;
        mLrcPlayerHandler.removeCallbacks(mLrcPlayerRunnable);

        //player
        mCurPlayingTime = 0;
        mPlayerStartTime = 0;
        mPlayerSpendTime = 0;

        //无额外歌词回调
        if (mExtraLyricsListener != null) {
            mExtraLyricsListener.extraLrcCallback();
        }
    }

    /**
     * 获取真实的歌词高度
     *
     * @param paint
     * @return
     */
    public int getRealTextHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) (-fm.leading - fm.ascent + fm.descent);
    }

    /**
     * 获取行歌词高度。用于y轴位置计算
     *
     * @param paint
     * @return
     */
    public int getTextHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) -(fm.ascent + fm.descent);
    }

    /**
     * 获取文本宽度
     *
     * @param paint
     * @param text
     * @return
     */
    public float getTextWidth(Paint paint, String text) {
        return paint
                .measureText(text);
    }

    /**
     * 获取当前时间对应的行歌词文本
     *
     * @param curPlayingTime 当前播放进度
     * @return
     */
    public String getLineLrc(int curPlayingTime) {
        if (mLyricsReader == null || mLrcLineInfos == null || mLrcLineInfos.size() == 0)
            return null;
        return LyricsUtils.getLineLrc(mLyricsReader.getLyricsType(), mLrcLineInfos, curPlayingTime, mLyricsReader.getPlayOffset());
    }

    /**
     * 获取分割后歌词的当前时间对应的行歌词文本
     *
     * @param curPlayingTime 当前播放进度
     * @return
     */
    public String getSplitLineLrc(int curPlayingTime) {
        if (mLyricsReader == null || mLrcLineInfos == null || mLrcLineInfos.size() == 0)
            return null;
        return LyricsUtils.getSplitLineLrc(mLyricsReader.getLyricsType(), mLrcLineInfos, curPlayingTime, mLyricsReader.getPlayOffset());
    }

    /**
     * 获取当前时间对应的行歌词开始时间
     *
     * @param curPlayingTime 当前播放进度
     * @return
     */
    public int getLineLrcStartTime(int curPlayingTime) {
        if (mLyricsReader == null || mLrcLineInfos == null || mLrcLineInfos.size() == 0)
            return -1;
        return LyricsUtils.getLineLrcStartTime(mLyricsReader.getLyricsType(), mLrcLineInfos, curPlayingTime, mLyricsReader.getPlayOffset());
    }

    /**
     * 获取分割后歌词的当前时间对应的行歌词开始时间
     *
     * @param curPlayingTime 当前播放进度
     * @return
     */
    public int getSplitLineLrcStartTime(int curPlayingTime) {
        if (mLyricsReader == null || mLrcLineInfos == null || mLrcLineInfos.size() == 0)
            return -1;
        return LyricsUtils.getSplitLineLrcStartTime(mLyricsReader.getLyricsType(), mLrcLineInfos, curPlayingTime, mLyricsReader.getPlayOffset());
    }

    /**
     * 刷新View
     */
    public void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            //  当前线程是主UI线程，直接刷新。
            invalidate();
        } else {
            //  当前线程是非UI线程，post刷新。
            postInvalidate();
        }
    }


    /**
     * 更新分隔后的行号，字索引，高亮时间
     *
     * @param playProgress
     */
    public void updateSplitData(int playProgress) {
        //动感歌词
        if (mLyricsReader.getLyricsType() == LyricsInfo.DYNAMIC) {
            //获取分割后的索引
            mSplitLyricsLineNum = LyricsUtils.getSplitDynamicLyricsLineNum(mLrcLineInfos, mLyricsLineNum, playProgress, mLyricsReader.getPlayOffset());
            //获取原始的歌词字索引
            mLyricsWordIndex = LyricsUtils.getLyricsWordIndex(mLrcLineInfos, mLyricsLineNum, playProgress, mLyricsReader.getPlayOffset());
            //获取分割后的歌词字索引
            mSplitLyricsWordIndex = LyricsUtils.getSplitLyricsWordIndex(mLrcLineInfos, mLyricsLineNum, playProgress, mLyricsReader.getPlayOffset());
            mLyricsWordHLTime = LyricsUtils.getDisWordsIndexLenTime(mLrcLineInfos, mLyricsLineNum, playProgress, mLyricsReader.getPlayOffset());
        } else {
            //lrc歌词
            //获取分割后的索引
            mSplitLyricsLineNum = LyricsUtils.getSplitLrcLyricsLineNum(mLrcLineInfos, mLyricsLineNum, playProgress, mLyricsReader.getPlayOffset());
        }
        if (mLyricsReader.getLyricsType() == LyricsInfo.DYNAMIC && mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLATELRC && mTranslateDrawType == TRANSLATE_DRAW_TYPE_DYNAMIC) {
            //显示翻译歌词且歌词类型是动感歌词且以动感歌词的形式绘画翻译歌词
            if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {
                mExtraSplitLyricsLineNum = LyricsUtils.getSplitExtraLyricsLineNum(mTranslateLrcLineInfos, mLyricsLineNum, playProgress, mLyricsReader.getPlayOffset());

                mExtraLyricsWordIndex = LyricsUtils.getExtraLyricsWordIndex(mTranslateLrcLineInfos, mLyricsLineNum, playProgress, mLyricsReader.getPlayOffset());
                mExtraSplitLyricsWordIndex = LyricsUtils.getSplitExtraLyricsWordIndex(mTranslateLrcLineInfos, mLyricsLineNum, playProgress, mLyricsReader.getPlayOffset());
                mTranslateLyricsWordHLTime = LyricsUtils.getTranslateLrcDisWordsIndexLenTime(mTranslateLrcLineInfos, mLyricsLineNum, playProgress, mLyricsReader.getPlayOffset());
            }
        } else if (mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
            //显示音译歌词
            if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
                mExtraSplitLyricsLineNum = LyricsUtils.getSplitExtraLyricsLineNum(mTransliterationLrcLineInfos, mLyricsLineNum, playProgress, mLyricsReader.getPlayOffset());

                mExtraLyricsWordIndex = LyricsUtils.getExtraLyricsWordIndex(mTransliterationLrcLineInfos, mLyricsLineNum, playProgress, mLyricsReader.getPlayOffset());
                mExtraSplitLyricsWordIndex = LyricsUtils.getSplitExtraLyricsWordIndex(mTransliterationLrcLineInfos, mLyricsLineNum, playProgress, mLyricsReader.getPlayOffset());
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        synchronized (lock) {
            mPaint.setAlpha(255);
            mPaintHL.setAlpha(255);
            mExtraLrcPaint.setAlpha(255);
            mExtraLrcPaintHL.setAlpha(255);
            if (mLrcStatus == LRCSTATUS_INIT || mLrcStatus == LRCSTATUS_NOLRC_DEFTEXT) {
                //绘画默认文本
                String defText = getDefText();
                float textWidth = getTextWidth(mPaint, defText);
                int textHeight = getTextHeight(mPaint);
                float hlWidth = textWidth / 2;
                float x = (getWidth() - textWidth) / 2;
                float y = (getHeight() + textHeight) / 2;
                drawOutline(canvas, mPaintOutline, defText, x, y);
                drawDynamicText(canvas, mPaint, mPaintHL, defText, hlWidth, x, y);
            } else if (mLrcStatus == LRCSTATUS_LOADING || mLrcStatus == LRCSTATUS_ERROR || mLrcStatus == LRCSTATUS_NONSUPPORT) {
                //绘画加载中文本
                String text = getDefText();
                if (mLrcStatus == LRCSTATUS_LOADING) {
                    text = getLoadingText();
                } else if (mLrcStatus == LRCSTATUS_ERROR) {
                    text = getLoadErrorText();
                } else if (mLrcStatus == LRCSTATUS_NONSUPPORT) {
                    text = getNonsupportText();
                }
                float textWidth = getTextWidth(mPaint, text);
                int textHeight = getTextHeight(mPaint);
                float x = (getWidth() - textWidth) / 2;
                float y = (getHeight() + textHeight) / 2;
                drawOutline(canvas, mPaintOutline, text, x, y);
                drawText(canvas, mPaint, text, x, y);
            } else if (mLrcStatus == LRCSTATUS_NOLRC_GOTOSEARCH) {
                String btnText = getGotoSearchText();
                //绘画搜索歌词按钮
                drawGoToSearchBtn(canvas, mGotoSearchRectPaint, mGotoSearchTextPaint, btnText);
            } else if (mLrcStatus == LRCSTATUS_LRC) {
                onViewDrawLrc(canvas);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mLrcStatus != LRCSTATUS_NOLRC_GOTOSEARCH) return onViewTouchEvent(event);


        int actionId = event.getAction();
        switch (actionId) {
            case MotionEvent.ACTION_DOWN:
                if (mGotoSearchBtnRect != null && mGotoSearchBtnRect.contains(event.getX(), event.getY())) {
                    isInGotoSearchBtnRect = true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isInGotoSearchBtnRect) {
                    isInGotoSearchBtnRect = false;

                    if (mSearchLyricsListener != null) mSearchLyricsListener.goToSearchLrc();
                    invalidateView();
                }
                break;
        }
        if (isInGotoSearchBtnRect) {
            invalidateView();
        }
        return true;

    }

    //////////////////////////////////////////////////////////////////////////////////////////


    public String getDefText() {
        return mDefText;
    }

    public void setDefText(String mDefText) {
        this.mDefText = mDefText;
    }

    public String getLoadingText() {
        return mLoadingText;
    }

    public void setLoadingText(String mLoadingText) {
        this.mLoadingText = mLoadingText;
    }

    public String getLoadErrorText() {
        return mLoadErrorText;
    }

    public void setLoadErrorText(String mLoadErrorText) {
        this.mLoadErrorText = mLoadErrorText;
    }

    public String getNonsupportText() {
        return mNonsupportText;
    }

    public void setNonsupportText(String mNonsupportText) {
        this.mNonsupportText = mNonsupportText;
    }

    public String getGotoSearchText() {
        return mGotoSearchText;
    }

    public void setGotoSearchText(String mGotoSearchText) {
        this.mGotoSearchText = mGotoSearchText;
    }

    public int getLrcPlayerStatus() {
        return mLrcPlayerStatus;
    }

    public int getLrcStatus() {
        return mLrcStatus;
    }

    public void setLrcStatus(int mLrcStatus) {
        this.mLrcStatus = mLrcStatus;
        invalidateView();
    }

    public int getExtraLrcStatus() {
        return mExtraLrcStatus;
    }

    public void setExtraLrcStatus(int mExtraLrcStatus) {
        synchronized (lock) {
            isHandToChangeExtraLrcStatus = true;
            this.mExtraLrcStatus = mExtraLrcStatus;
            if (mExtraLrcStatus == EXTRALRCSTATUS_NOSHOWEXTRALRC) {
                //不显示额外歌词
                mHighLightLrcMoveX = 0;
                mExtraLyricsHighLightMoveX = 0;
            } else {
                mExtraLyricsHighLightMoveX = 0;
            }
            //更新行和索引等数据
            updateView((int) (mCurPlayingTime + mPlayerSpendTime));
            invalidateView();
        }
    }

    public LyricsReader getLyricsReader() {
        return mLyricsReader;
    }

    public void setTranslateDrawType(int mTranslateDrawType) {
        this.mTranslateDrawType = mTranslateDrawType;
    }

    public void setExtraLyricsListener(ExtraLyricsListener mExtraLyricsListener) {
        this.mExtraLyricsListener = mExtraLyricsListener;
    }

    public void setTextMaxWidth(int mTextMaxWidth) {
        this.mTextMaxWidth = mTextMaxWidth;
    }

    public int getExtraLrcType() {
        return mExtraLrcType;
    }

    public void setSearchLyricsListener(SearchLyricsListener mSearchLyricsListener) {
        this.mSearchLyricsListener = mSearchLyricsListener;
    }

    /**
     * 设置歌词读取器
     *
     * @param lyricsReader 歌词读取器
     */
    public void setLyricsReader(LyricsReader lyricsReader) {
        synchronized (lock) {
            this.mLyricsReader = lyricsReader;
            resetData();
            if (!hasLrcLineInfos()) {

                if (mSearchLyricsListener != null) {
                    mLrcStatus = LRCSTATUS_NOLRC_GOTOSEARCH;
                } else {
                    mLrcStatus = LRCSTATUS_NOLRC_DEFTEXT;
                }

            } else {
                //是否有歌词数据
                mLrcStatus = LRCSTATUS_LRC;

                updateView(mCurPlayingTime);
            }
            initExtraLrcTypeAndCallBack();
            invalidateView();
        }
    }

    /**
     * 初始化歌词数据
     */
    public void initLrcData() {
        synchronized (lock) {
            mLyricsReader = null;
            mLrcStatus = LRCSTATUS_INIT;
            resetData();
            initExtraLrcTypeAndCallBack();
            invalidateView();
        }
    }

    /**
     * 是否有歌词数据
     *
     * @return
     */
    public boolean hasLrcLineInfos() {
        if (mLyricsReader != null && mLyricsReader.getLrcLineInfos() != null && mLyricsReader.getLrcLineInfos().size() > 0) {
            //获取分割歌词集合
            if (mLyricsReader.getLyricsType() == LyricsInfo.LRC) {
                //lrc歌词
                mLrcLineInfos = LyricsUtils.getSplitLrcLyrics(mLyricsReader.getLrcLineInfos(), mTextMaxWidth, mPaint);
                //翻译歌词
                mTranslateLrcLineInfos = LyricsUtils.getSplitLrcExtraLyrics(mLyricsReader.getTranslateLrcLineInfos(), mTextMaxWidth, mExtraLrcPaint);
                //该lrc歌词不支持音译歌词
            } else {
                //动感歌词
                //默认歌词
                mLrcLineInfos = LyricsUtils.getSplitDynamicLyrics(mLyricsReader.getLrcLineInfos(), mTextMaxWidth, mPaint);
                //翻译歌词
                mTranslateLrcLineInfos = LyricsUtils.getSplitDynamicExtraLyrics(mLyricsReader.getTranslateLrcLineInfos(), mTextMaxWidth, mExtraLrcPaint);
                //音译歌词
                mTransliterationLrcLineInfos = LyricsUtils.getSplitDynamicExtraLyrics(mLyricsReader.getTransliterationLrcLineInfos(), mTextMaxWidth, mExtraLrcPaint);
            }

            return true;
        }

        return false;
    }

    /**
     * 初始化额外歌词类型
     */
    private void initExtraLrcTypeAndCallBack() {
        int extraLrcStatus = EXTRALRCSTATUS_NOSHOWEXTRALRC;
        //判断音译和翻译歌词
        if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0 && mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
            //有翻译歌词和音译歌词
            mExtraLrcType = EXTRALRCTYPE_BOTH;
            extraLrcStatus = EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC;
        } else if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {
            //有翻译歌词
            mExtraLrcType = EXTRALRCTYPE_TRANSLATELRC;
            extraLrcStatus = EXTRALRCSTATUS_SHOWTRANSLATELRC;
        } else if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
            //音译歌词
            mExtraLrcType = EXTRALRCTYPE_TRANSLITERATIONLRC;
            extraLrcStatus = EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC;
        } else {
            //无翻译歌词和音译歌词
            mExtraLrcType = EXTRALRCTYPE_NOLRC;
            isHandToChangeExtraLrcStatus = false;
        }
        if (!isHandToChangeExtraLrcStatus) {
            mExtraLrcStatus = extraLrcStatus;
            isHandToChangeExtraLrcStatus = false;
        }
        if (mExtraLyricsListener != null) {
            mExtraLyricsListener.extraLrcCallback();
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 设置字体文件
     *
     * @param typeFace
     */
    public void setTypeFace(Typeface typeFace, boolean isInvalidateView) {
        if (typeFace != null) {
            mPaint.setTypeface(typeFace);
            mPaintHL.setTypeface(typeFace);
            mPaintOutline.setTypeface(typeFace);
            mExtraLrcPaint.setTypeface(typeFace);
            mExtraLrcPaintHL.setTypeface(typeFace);
            mExtraLrcPaintOutline.setTypeface(typeFace);
        }
        if (isInvalidateView) {
            invalidateView();
        }
    }

    public void setSpaceLineHeight(int mSpaceLineHeight, boolean isInvalidateView) {
        this.mSpaceLineHeight = mSpaceLineHeight;
        if (isInvalidateView) {
            invalidateView();
        }
    }

    public void setExtraLrcSpaceLineHeight(int mExtraLrcSpaceLineHeight, boolean isInvalidateView) {
        this.mExtraLrcSpaceLineHeight = mExtraLrcSpaceLineHeight;
        if (isInvalidateView) {
            invalidateView();
        }
    }

    public int getFontSize() {
        return mFontSize;
    }

    public int getExtraLrcFontSize() {
        return mExtraLrcFontSize;
    }


    /**
     * 设置歌词字体大小
     *
     * @param fontSize
     * @param isReloadLrcData  是否重新加载歌词数据
     * @param isInvalidateView 是否刷新视图
     */
    public void setFontSize(int fontSize, boolean isReloadLrcData, boolean isInvalidateView) {
        synchronized (lock) {

            this.mFontSize = fontSize;

            //
            mPaint.setTextSize(mFontSize);
            mPaintHL.setTextSize(mFontSize);
            mPaintOutline.setTextSize(mFontSize);

            //搜索歌词回调不为空
            if (mSearchLyricsListener != null) {
                mGotoSearchRectPaint.setTextSize(mFontSize);
                mGotoSearchTextPaint.setTextSize(mFontSize);

                //初始化搜索
                if (mGotoSearchBtnRect != null) {

                    int textY = (getHeight() + getTextHeight(mGotoSearchTextPaint)) / 2;
                    int textWidth = (int) getTextWidth(mGotoSearchTextPaint, getGotoSearchText());
                    int textX = (getWidth() - textWidth) / 2;

                    int padding = getRealTextHeight(mGotoSearchTextPaint) / 2;
                    int rectTop = textY - getTextHeight(mGotoSearchTextPaint) - padding;
                    int rectLeft = textX - padding;
                    int rectRight = rectLeft + textWidth + padding * 2;
                    int rectBottom = textY + padding;
                    mGotoSearchBtnRect = new RectF(rectLeft, rectTop, rectRight, rectBottom);
                }
            }

            if (isReloadLrcData) {
                //加载歌词数据
                if (hasLrcLineInfos()) {
                    updateView((int) (mCurPlayingTime + mPlayerSpendTime));
                }
            }
            if (isInvalidateView) {
                invalidateView();
            }
        }
    }

    /**
     * 设置额外歌词字体大小
     *
     * @param extraLrcFontSize
     * @param isReloadLrcData  是否重新加载歌词数据
     * @param isInvalidateView 是否刷新视图
     */
    public void setExtraLrcFontSize(int extraLrcFontSize, boolean isReloadLrcData, boolean isInvalidateView) {
        synchronized (lock) {
            this.mExtraLrcFontSize = extraLrcFontSize;

            //
            mExtraLrcPaint.setTextSize(mExtraLrcFontSize);
            mExtraLrcPaintHL.setTextSize(mExtraLrcFontSize);
            mExtraLrcPaintOutline.setTextSize(mExtraLrcFontSize);


            if (isReloadLrcData) {
                if (hasLrcLineInfos()) {
                    updateView((int) (mCurPlayingTime + mPlayerSpendTime));
                }
            }
            if (isInvalidateView) {
                invalidateView();
            }
        }
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     * @param extraLrcFontSize
     * @param isReloadLrcData  是否重新加载歌词数据
     * @param isInvalidateView 是否刷新视图
     */
    public void setFontSize(int fontSize, int extraLrcFontSize, boolean isReloadLrcData, boolean isInvalidateView) {
        synchronized (lock) {
            setFontSize(fontSize, false, false);
            setExtraLrcFontSize(extraLrcFontSize, false, false);
            if (isReloadLrcData) {
                if (hasLrcLineInfos()) {
                    updateView((int) (mCurPlayingTime + mPlayerSpendTime));
                }
            }
            if (isInvalidateView) {
                invalidateView();
            }
        }
    }

    /**
     * 设置画笔颜色
     *
     * @param colors 颜色集合 至少两种颜色
     */
    public void setPaintColor(int[] colors, boolean isInvalidateView) {
        if (colors.length < 2) {
            int[] newolors = new int[]{colors[0], colors[0]};
            colors = newolors;
        }
        this.mPaintColors = colors;
        if (isInvalidateView) {
            invalidateView();
        }
    }

    /**
     * 设置高亮画笔颜色
     *
     * @param colors 颜色集合 至少两种颜色
     */
    public void setPaintHLColor(int[] colors, boolean isInvalidateView) {
        if (colors.length < 2) {
            int[] newolors = new int[]{colors[0], colors[0]};
            colors = newolors;
        }
        this.mPaintHLColors = colors;
        if (isInvalidateView) {
            invalidateView();
        }
    }

    public void setGotoSearchTextColor(int mGotoSearchTextColor, boolean isInvalidateView) {
        this.mGotoSearchTextColor = mGotoSearchTextColor;
        if (isInvalidateView) {
            invalidateView();
        }
    }

    public void setGotoSearchTextPressedColor(int mGotoSearchTextPressedColor, boolean isInvalidateView) {
        this.mGotoSearchTextPressedColor = mGotoSearchTextPressedColor;
        if (isInvalidateView) {
            invalidateView();
        }
    }

    /**
     * 初始化
     *
     * @param context
     */
    protected abstract void viewInit(Context context);

    /**
     * view视图加载完成
     */
    protected abstract void viewLoadFinish();

    /**
     * view的draw歌词调用方法
     *
     * @param canvas
     * @return
     */
    protected abstract void onViewDrawLrc(Canvas canvas);

    /**
     * view的onTouchEvent调用方法
     *
     * @param event
     * @return
     */
    protected abstract boolean onViewTouchEvent(MotionEvent event);

    /**
     * 更新视图
     *
     * @param playProgress
     */
    protected abstract void updateView(int playProgress);

    /**
     * 搜索歌词接口
     */
    public interface SearchLyricsListener {
        /**
         * 搜索歌词回调
         */
        void goToSearchLrc();
    }

    /**
     * 额外歌词事件
     */
    public interface ExtraLyricsListener {
        /**
         * 额外歌词回调
         */
        void extraLrcCallback();
    }
}
