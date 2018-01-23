package base.widget.lrc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.zlm.hp.R;
import com.zlm.hp.mp3.lyrics.model.LyricsLineInfo;
import com.zlm.hp.mp3.lyrics.model.TranslateLrcLineInfo;
import com.zlm.hp.mp3.lyrics.utils.LyricsUtil;
import com.zlm.hp.utils.MediaUtil;

import java.util.List;
import java.util.TreeMap;

import base.utils.ColorUtil;
import base.utils.LoggerUtil;

/**
 * 多行歌词第二版:歌词行号和view所在位置关联,Scroller只做动画处理，不去移动view
 * 该版本将支持翻译歌词和音译歌词
 * Created by zhangliangming on 2017/8/19.
 */

public class ManyLineLyricsViewV2 extends View {
    public static final int SHOWTRANSLATELRC = 0;
    public static final int SHOWTRANSLITERATIONLRC = 1;
    public static final int NOSHOWEXTRALRC = 2;
    private Context mContext;
    /**
     * 默认提示文本
     */
    private String mDefText;
    /**
     * 默认歌词画笔
     */
    private Paint mPaint;
    /**
     * 高亮歌词画笔
     */
    private Paint mPaintHL;
    /**
     * 画时间线指示器
     ***/
    private Paint mPaintIndicator;
    /**
     * 画虚线
     */
    private Paint mPaintPathEffect;
    /**
     * 绘画播放按钮
     */
    private Paint mPaintPlay;
    private Rect mPlayRect;
    private int mRectSize = 60;
    /**
     *
     */
    private LoggerUtil logger;
    /**
     * 空行高度
     */
    private int mSpaceLineHeight = 40;
    /**
     * 歌词字体大小
     */
    private int mFontSize = 32;
    /**
     * 歌词颜色
     */
    private int mLrcColor = ColorUtil.parserColor("#fada83");
    private int mDefLrcColor = ColorUtil.parserColor("#ffffff");
    /**
     * 歌词的最大宽度
     */
    private int mTextMaxWidth = 0;
    /**
     * 歌词解析
     */
    private LyricsUtil mLyricsUtil;
    /**
     * 歌词列表
     */
    private TreeMap<Integer, LyricsLineInfo> mLyricsLineTreeMap;
    /**
     * 翻译行歌词列表
     */
    private List<TranslateLrcLineInfo> mTranslateLrcLineInfos;
    /**
     * 音译歌词行
     */
    private List<LyricsLineInfo> mTransliterationLrcLineInfos;
    /**
     * 当前歌词的所在行数
     */
    private int mLyricsLineNum = 0;


    ///////////////////////////////////////
    /**
     * 当前歌词的第几个字
     */
    private int mLyricsWordIndex = -1;
    /**
     * 当前歌词第几个字 已经播放的时间
     */
    private float mLyricsWordHLTime = 0;
    /**
     * 判断view是点击还是移动的距离
     */
    private int mTouchSlop;
    /**
     *
     */
    private Scroller mScroller;
    /**
     * Y轴移动的时间
     */
    private int mDuration = 400;
    /**
     * 歌词在Y轴上的偏移量
     */
    private float mOffsetY = 0;
    /**
     * 视图y中间
     */
    private float mCentreY = 0;

    /////////////////////////////////////////////////////
    /**
     * 颜色渐变梯度
     */
    private int mMaxAlpha = 255;
    private int mMinAlpha = 50;
    //渐变的高度
    private int mShadeHeight = 0;
    /**
     * 记录手势
     */
    private VelocityTracker mVelocityTracker;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    //用于判断拦截
    private int mInterceptX = 0;
    private int mInterceptY = 0;
    /**
     * 触摸最后一次的坐标
     */
    private int mLastY;
    /**
     * 是否直接拦截
     */
    private boolean mTouchIntercept = false;
    /**
     * 是否允许触摸
     */
    private boolean mTouchAble = true;
    /**
     * 正在拖动
     */
    private boolean isTouchMove = false;
    /**
     * 是否按下
     */
    private boolean isTouchDown = false;
    /**
     * 是否滑动越界
     */
    private boolean isOverScroll = false;
    /**
     * 是否是快速滑动
     */
    private boolean isFlingScroll = false;
    /**
     * 还原歌词视图
     */
    private int RESETLRCVIEW = 1;
    /**
     *
     */
    private int mResetDuration = 3000;
    /**
     * 歌词快进事件
     */
    private OnLrcClickListener mOnLrcClickListener;
    /**
     * 额外歌词监听事件
     */
    private ExtraLyricsListener mExtraLyricsListener;

    //////////////////////////////////////翻译和音译歌词变量//////////////////////////////////////////////
    /**
     * 判断歌词集合是否在重构
     */
    private boolean isReconstruct = false;
    /**
     * 是否是多行歌词
     */
    private boolean isManyLineLrc = true;
    /**
     * 额外歌词状态
     */
    private int mExtraLrcStatus = NOSHOWEXTRALRC;
    /**
     * 分割歌词的行索引
     */
    private int mSplitLyricsLineNum = 0;
    /**
     * 额外歌词空行高度
     */
    private int mExtraLrcSpaceLineHeight = 20;
    /**
     * 额外歌词画笔
     */
    private Paint mExtraLrcPaint;
    /**
     * Handler处理滑动指示器隐藏和歌词滚动到当前播放的位置
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    if (mScroller.computeScrollOffset()) {
                        //发送还原
                        handler.sendEmptyMessageDelayed(RESETLRCVIEW, mResetDuration);
                    } else {
                        //
                        isTouchMove = false;
                        int deltaY = (int) getLineScollHeight(mLyricsLineNum) - mScroller.getFinalY();
                        mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
                        invalidateView();
                    }

                    break;
            }
        }
    };
    /**
     * 额外歌词高亮画笔
     */
    private Paint mExtraLrcPaintHL;

    /**
     * 当前音译歌词的所在行数
     */
    private int mExtraLyricsLineNum = 0;

    /**
     * 当前音译歌词的第几个字
     */
    private int mExtraLyricsWordIndex = -1;

    public ManyLineLyricsViewV2(Context context) {
        super(context);
        init(context);
    }

    public ManyLineLyricsViewV2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ManyLineLyricsViewV2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ManyLineLyricsViewV2(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {

        //
        logger = LoggerUtil.getZhangLogger(context);
        mDefText = context.getString(R.string.def_text);
        //
        mScroller = new Scroller(context, new LinearInterpolator());
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();


        //默认画笔
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);

        //歌词高亮画笔
        mPaintHL = new Paint();
        mPaintHL.setDither(true);
        mPaintHL.setAntiAlias(true);

        //额外歌词画笔
        mExtraLrcPaint = new Paint();
        mExtraLrcPaint.setDither(true);
        mExtraLrcPaint.setAntiAlias(true);

        //额外高亮歌词画笔
        mExtraLrcPaintHL = new Paint();
        mExtraLrcPaintHL.setDither(true);
        mExtraLrcPaintHL.setAntiAlias(true);

        //画指示器
        mPaintIndicator = new Paint();
        mPaintIndicator.setDither(true);
        mPaintIndicator.setAntiAlias(true);

        //画虚线
        mPaintPathEffect = new Paint();
        mPaintPathEffect.setDither(true);
        mPaintPathEffect.setAntiAlias(true);
        mPaintPathEffect.setStyle(Paint.Style.STROKE);
        mPaintPathEffect.setStrokeWidth(4);
        PathEffect effects = new DashPathEffect(
                new float[]{10, 10}, 0);
        mPaintPathEffect.setPathEffect(effects);

        //绘画播放按钮
        mPaintPlay = new Paint();
        mPaintPlay.setDither(true);
        mPaintPlay.setAntiAlias(true);
        mPaintPlay.setStyle(Paint.Style.STROKE);
        mPaintPlay.setStrokeWidth(4);


        //初始化画笔颜色
        initColor();
        //
        initFontSize();
    }

    /**
     * 初始化画笔颜色
     */
    private void initColor() {
        mPaint.setColor(mDefLrcColor);
        mPaintHL.setColor(mLrcColor);
        mPaintIndicator.setColor(mLrcColor);
        mPaintPathEffect.setColor(mLrcColor);
        mPaintPlay.setColor(mLrcColor);

        mExtraLrcPaint.setColor(mDefLrcColor);
        mExtraLrcPaintHL.setColor(mLrcColor);
    }

    /**
     * 初始化字体大小
     */
    private void initFontSize() {

        mPaint.setTextSize(mFontSize);
        mPaintHL.setTextSize(mFontSize);
        mPaintIndicator.setTextSize(mFontSize);
        mPaintPathEffect.setTextSize(mFontSize);
        mPaintPlay.setTextSize(mFontSize);

        //
        mExtraLrcPaint.setTextSize(mFontSize / 5 * 4);
        mExtraLrcPaintHL.setTextSize(mFontSize / 5 * 4);
    }

    /**
     * 初始化渐变高度
     */
    private void initShadeHeight() {
        mShadeHeight = getHeight() / 4;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLyricsLineTreeMap == null || mLyricsLineTreeMap.size() == 0) {
            if (isManyLineLrc) {
                //多行歌词默认文本
                drawManyLineDefText(canvas);
            } else {
                drawTwoLineDefText(canvas);
            }

        } else {
            if (isManyLineLrc) {
                //绘画多行歌词
                drawManyLineLrcText(canvas);
            } else {
                drawTwoLineLrcText(canvas);
            }
        }
        //多行歌词才绘画时间线
        if (isTouchMove && mOnLrcClickListener != null && isManyLineLrc)
            drawIndicator(canvas);
    }

    /**
     * 绘画时间线提示器
     *
     * @param canvas
     */
    private void drawIndicator(Canvas canvas) {

        //画当前时间
        int scrollLrcLineNum = getScrollLrcLineNum(mOffsetY);
        int startTime = mLyricsLineTreeMap.get(scrollLrcLineNum).getStartTime();
        String timeString = MediaUtil.parseTimeToString(startTime);
        int textHeight = getTextHeight(mPaintIndicator);
        float textX = 0;
        float textY = (getHeight() - textHeight) / 2;
        canvas.drawText(timeString, textX, textY, mPaintIndicator);

        //画播放按钮
        mPlayRect = new Rect();
        int padding = 10;
        int rectLeft = getWidth() - padding - mRectSize;
        int rectTop = getHeight() / 2 - mRectSize / 2 - padding / 2;
        int rectRight = rectLeft + mRectSize + padding;
        int rectBottom = rectTop + mRectSize + padding;
        mPlayRect.set(rectLeft, rectTop, rectRight, rectBottom);

        //画圆
        int cx = rectLeft + mRectSize / 2;
        int cy = rectTop + mRectSize / 2;
        canvas.drawCircle(cx, cy, mRectSize / 2, mPaintPlay);

        //画三角形
        int triangleSize = mRectSize / 3 * 2;
        Path trianglePath = new Path();
        int startX = rectLeft + padding + padding / 2 + (mRectSize - triangleSize) / 2;
        int startY = rectTop + (mRectSize - triangleSize) / 2;
        trianglePath.moveTo(startX, startY);// 此点为多边形的起点
        trianglePath.lineTo(startX + triangleSize / 2, startY + triangleSize / 2);
        trianglePath.lineTo(startX, startY + triangleSize);
        trianglePath.close();// 使这些点构成封闭的多边形
        canvas.drawPath(trianglePath, mPaintIndicator);

        //画虚线
        float y = getHeight() * 0.5f;
        float x = 0;
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(getWidth() - 2 * padding - mRectSize, y);
        canvas.drawPath(path, mPaintPathEffect);

    }

    /**
     * 画默认字体
     *
     * @param canvas
     */
    private void drawManyLineDefText(Canvas canvas) {

        //
        float textWidth = getTextWidth(mPaint, mDefText);
        int textHeight = getTextHeight(mPaint);

        canvas.save();

        float textX = (getWidth() - textWidth) * 0.5f;
        float textY = (getHeight() + textHeight) * 0.5f;

        mPaint.setAlpha(mMaxAlpha);
        mPaintHL.setAlpha(mMaxAlpha);

        canvas.drawText(mDefText, textX, textY, mPaint);

        // 设置过渡的颜色和进度
        canvas.clipRect(textX, textY - getClipTextHeight(mPaint), textX + textWidth * 0.5f,
                textY + getRealTextHeight(mPaint));

        canvas.drawText(mDefText, textX, textY, mPaintHL);
        canvas.restore();

    }

    /**
     * 画双行歌词
     *
     * @param canvas
     */
    private void drawTwoLineDefText(Canvas canvas) {
        //
        float textWidth = getTextWidth(mPaint, mDefText);
        int textHeight = getTextHeight(mPaint);

        canvas.save();

        float textX = (getWidth() - textWidth) * 0.5f;
        float textY = getHeight() - getLineHeight(mPaint) - mSpaceLineHeight - textHeight * 0.5f;

        mPaint.setAlpha(mMaxAlpha);
        mPaintHL.setAlpha(mMaxAlpha);

        canvas.drawText(mDefText, textX, textY, mPaint);

        // 设置过渡的颜色和进度
        canvas.clipRect(textX, textY - getClipTextHeight(mPaint), textX + textWidth * 0.5f,
                textY + getRealTextHeight(mPaint));

        canvas.drawText(mDefText, textX, textY, mPaintHL);
        canvas.restore();
    }

    /**
     * 画双行歌词
     *
     * @param canvas
     */
    private void drawTwoLineLrcText(Canvas canvas) {

        mPaint.setAlpha(mMaxAlpha);
        mPaintHL.setAlpha(mMaxAlpha);

        if (mLyricsLineNum == -1) {
            int textHeight = getTextHeight(mPaint);
            mCentreY = getHeight() - getLineHeight(mPaint) - mSpaceLineHeight - textHeight * 0.5f;
            String fristLyrics = mLyricsLineTreeMap.get(0).getLineLyrics();
            float fristLyricsWidth = getTextWidth(mPaint, fristLyrics);
            float fristLyricsX = (getWidth() - fristLyricsWidth) * 0.5f;
            canvas.drawText(fristLyrics, fristLyricsX, mCentreY,
                    mPaint);

            if (mLyricsLineNum + 2 < mLyricsLineTreeMap.size()) {
                String nextLyrics = mLyricsLineTreeMap.get(mLyricsLineNum + 2)
                        .getLineLyrics();

                float nextLyricsWidth = mPaint.measureText(nextLyrics);
                float nextLyricsX = (getWidth() - nextLyricsWidth) * 0.5f;

                canvas.drawText(nextLyrics, nextLyricsX, mCentreY + getLineHeight(mPaint), mPaint);
            }
        } else {
            //
            if (mLyricsLineNum % 2 == 0) {
                int textHeight = getTextHeight(mPaint);
                mCentreY = getHeight() - getLineHeight(mPaint) - mSpaceLineHeight - textHeight * 0.5f;

                // 画下一句的歌词
                if (mLyricsLineNum + 1 < mLyricsLineTreeMap.size()) {
                    String nextLyrics = mLyricsLineTreeMap.get(
                            mLyricsLineNum + 1).getLineLyrics();
                    float nextLyricsWidth = mPaint
                            .measureText(nextLyrics);
                    float nextLyricsX = (getWidth() - nextLyricsWidth) * 0.5f;

                    canvas.drawText(nextLyrics, nextLyricsX, mCentreY + getLineHeight(mPaint), mPaint);
                }
            } else {

                int textHeight = getTextHeight(mPaint);
                float fristLyricsY = getHeight() - getLineHeight(mPaint) - mSpaceLineHeight - textHeight * 0.5f;
                mCentreY = fristLyricsY + getLineHeight(mPaint);

                // 画下一句的歌词
                if (mLyricsLineNum + 1 < mLyricsLineTreeMap.size()) {
                    String nextLyrics = mLyricsLineTreeMap.get(
                            mLyricsLineNum + 1).getLineLyrics();
                    float nextLyricsWidth = mPaint
                            .measureText(nextLyrics);
                    float nextLyricsX = (getWidth() - nextLyricsWidth) * 0.5f;
                    canvas.drawText(nextLyrics, nextLyricsX, fristLyricsY, mPaint);
                } else {
                    //因为当前是最后一句了，这里画上一句
                    String fristLyrics = mLyricsLineTreeMap.get(
                            mLyricsLineNum - 1).getLineLyrics();
                    float fristLyricsWidth = getTextWidth(mPaint, fristLyrics);
                    float fristLyricsX = (getWidth() - fristLyricsWidth) * 0.5f;

                    canvas.drawText(fristLyrics, fristLyricsX, fristLyricsY, mPaintHL);
                }

            }

            //画当前双行动感歌词行
            drawTwoDGLineLrc(canvas);

        }
    }

    /**
     * 画当前双行动感歌词行
     *
     * @param canvas
     */
    private void drawTwoDGLineLrc(Canvas canvas) {

        float lineLyricsHLWidth = 0;

        LyricsLineInfo lyricsLineInfo = mLyricsLineTreeMap
                .get(mLyricsLineNum);
        // 整行歌词
        String curLyrics = lyricsLineInfo.getLineLyrics();

        //超出视图
        if (mCentreY < getLineHeight(mPaintHL) || mCentreY + mSpaceLineHeight > getHeight()) {
            return;
        }

        int curLyricsHeight = getTextHeight(mPaintHL);
        float curLyricsWidth = getTextWidth(mPaintHL, curLyrics);

        // 歌词
        if (mLyricsWordIndex == -1) {
            //设置等于当行歌词的大小，防止跳转下一行歌词后，该行歌词不为高亮状态
            lineLyricsHLWidth = curLyricsWidth;
        } else {
            String lyricsWords[] = lyricsLineInfo.getLyricsWords();
            int wordsDisInterval[] = lyricsLineInfo
                    .getWordsDisInterval();
            // 当前歌词之前的歌词
            String lyricsBeforeWord = "";
            for (int i = 0; i < mLyricsWordIndex; i++) {
                lyricsBeforeWord += lyricsWords[i];
            }
            // 当前歌词
            String lyricsNowWord = lyricsWords[mLyricsWordIndex].trim();// 去掉空格

            // 当前歌词之前的歌词长度
            float lyricsBeforeWordWidth = getTextWidth(mPaintHL, lyricsBeforeWord);

            // 当前歌词长度
            float lyricsNowWordWidth = getTextWidth(mPaintHL, lyricsNowWord);

            float len = lyricsNowWordWidth
                    / wordsDisInterval[mLyricsWordIndex]
                    * mLyricsWordHLTime;
            lineLyricsHLWidth = lyricsBeforeWordWidth + len;
        }
        float curTextX = (getWidth() - curLyricsWidth) * 0.5f;


        //计算颜色透明度
        int alpha = mMaxAlpha;

        mPaint.setAlpha(alpha);
        mPaintHL.setAlpha(alpha);

        // save和restore是为了剪切操作不影响画布的其它元素
        canvas.save();

        // 画当前歌词
        canvas.drawText(curLyrics, curTextX, mCentreY, mPaint);

        // 设置过渡的颜色和进度
        canvas.clipRect(curTextX, mCentreY - getClipTextHeight(mPaint), curTextX + lineLyricsHLWidth,
                mCentreY + getRealTextHeight(mPaint));

        // 画当前歌词
        canvas.drawText(curLyrics, curTextX, mCentreY, mPaintHL);
        canvas.restore();
    }

    /**
     * 画歌词
     *
     * @param canvas
     */
    private void drawManyLineLrcText(Canvas canvas) {
        mCentreY = (getHeight() + getTextHeight(mPaintHL)) * 0.5f + getLineScollHeight(mLyricsLineNum) - mOffsetY;

        //获取要透明度要渐变的高度大小
        if (mShadeHeight == 0) {
            initShadeHeight();
        }

        //画中间歌词
        float[] topAndBottomY = drawDGLineLrc(canvas);
        //画下面歌词
        drawDownLineLrc(canvas, topAndBottomY[1]);

        //画上面歌词
        drawUpLineLrc(canvas, topAndBottomY[0]);
    }

    /**
     * 画下面歌词
     *
     * @param canvas
     * @param oldBottomY
     */
    private void drawDownLineLrc(Canvas canvas, float oldBottomY) {
        float centreLastBottomY = oldBottomY;
        float oldCentreLastBottomY = oldBottomY;
        // 画当前歌词之后的歌词
        for (int i = mLyricsLineNum + 1; i < mLyricsLineTreeMap.size(); i++) {
            LyricsLineInfo lyricsLineInfo = mLyricsLineTreeMap
                    .get(i);
            //获取分割后的歌词列表
            List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();

            for (int j = 0; j < lyricsLineInfos.size(); j++) {

                String text = lyricsLineInfos.get(j).getLineLyrics();
                centreLastBottomY = centreLastBottomY + getLineHeight(mPaint);

                //超出上视图
                if (centreLastBottomY < getLineHeight(mPaint)) {
                    continue;
                }
                //超出下视图
                if (centreLastBottomY + mSpaceLineHeight > getHeight()) {
                    break;
                }

                //计算颜色透明度
                int alpha = mMaxAlpha;
                if (centreLastBottomY < mShadeHeight) {

                    alpha = mMaxAlpha - (int) ((mShadeHeight - centreLastBottomY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);


                } else if (centreLastBottomY > getHeight() - mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((centreLastBottomY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);

                }
                alpha = Math.max(alpha, 0);
                mPaint.setAlpha(alpha);

                //
                float textWidth = getTextWidth(mPaint, text);
                float textX = (getWidth() - textWidth) * 0.5f;
                canvas.drawText(text, textX, centreLastBottomY, mPaint);
            }

            //考虑部分歌词越界，导致高度不正确，这里重新获取基本歌词结束后的y轴位置
            centreLastBottomY = oldCentreLastBottomY + getLineHeight(mPaint) * lyricsLineInfos.size();

            //画完成当行歌词后，判断是否需要绘画翻译歌词和音译歌词
            if (mExtraLrcStatus == SHOWTRANSLATELRC) {
                //画翻译歌词

                centreLastBottomY = drawDownLineTranslateLrc(canvas, i, centreLastBottomY);

            } else if (mExtraLrcStatus == SHOWTRANSLITERATIONLRC) {
                //画音译歌词
                centreLastBottomY = drawDownLineTransliterationLrc(canvas, i, centreLastBottomY);
            }

            oldCentreLastBottomY = centreLastBottomY;

        }
    }


    /**
     * 画上面歌词
     *
     * @param canvas
     * @param centreLastTopY 中线最后一次的y轴
     */
    private void drawUpLineLrc(Canvas canvas, float centreLastTopY) {
        // 画当前歌词之前的歌词
        for (int i = mLyricsLineNum - 1; i >= 0; i--) {
            LyricsLineInfo lyricsLineInfo = mLyricsLineTreeMap
                    .get(i);

            //判断是否需要绘画翻译歌词和音译歌词
            if (mExtraLrcStatus == SHOWTRANSLATELRC) {
                //画翻译歌词
                centreLastTopY = drawUpLineTranslateLrc(canvas, i, centreLastTopY);
            } else if (mExtraLrcStatus == SHOWTRANSLITERATIONLRC) {
                //画音译歌词
                centreLastTopY = drawUpLineTransliterationLrc(canvas, i, centreLastTopY);
            }


            //获取分割后的歌词列表
            List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();

            for (int j = lyricsLineInfos.size() - 1; j >= 0; j--) {

                String text = lyricsLineInfos.get(j).getLineLyrics();
                centreLastTopY = centreLastTopY - getLineHeight(mPaint);

                //超出上视图
                if (centreLastTopY < getLineHeight(mPaint)) {
                    continue;
                }
                //超出下视图
                if (centreLastTopY + mSpaceLineHeight > getHeight()) {
                    continue;
                }

                //计算颜色透明度
                int alpha = mMaxAlpha;
                if (centreLastTopY < mShadeHeight) {

                    alpha = mMaxAlpha - (int) ((mShadeHeight - centreLastTopY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);

                } else if (centreLastTopY > getHeight() - mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((centreLastTopY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                }
                alpha = Math.max(alpha, 0);
                mPaint.setAlpha(alpha);

                //
                float textWidth = getTextWidth(mPaint, text);
                float textX = (getWidth() - textWidth) * 0.5f;
                canvas.drawText(text, textX, centreLastTopY, mPaint);
            }

        }
    }


    /**
     * 绘画动感歌词
     *
     * @param canvas
     */
    private float[] drawDGLineLrc(Canvas canvas) {
        float[] topAndBottomY = new float[2];
        //
        float topY = mCentreY;//当行歌词第一行歌词的高度
        float bottomY = mCentreY;//当行歌词最后一行歌词的高度

        LyricsLineInfo lyricsLineInfo = mLyricsLineTreeMap
                .get(mLyricsLineNum);
        //获取分割后的歌词列表
        List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();

        int curLyricsLineNum = mSplitLyricsLineNum;
        if (mLyricsWordIndex == -1) {
            //设置为最后索引，防止跳转到下一句时，前面歌词不是高亮的问题
            curLyricsLineNum = lyricsLineInfos.size() - 1;
        }

        //往下绘画歌词
        for (int i = 0; i < lyricsLineInfos.size(); i++) {
            String text = lyricsLineInfos.get(i).getLineLyrics();

            bottomY = mCentreY + i * getLineHeight(mPaint);

            //超出上视图
            if (bottomY < getLineHeight(mPaint)) {
                continue;
            }
            //超出下视图
            if (bottomY + mSpaceLineHeight > getHeight()) {
                break;
            }

            //计算颜色透明度
            int alpha = mMaxAlpha;

            //颜色透明度过渡

            if (bottomY < mShadeHeight) {
                alpha = mMaxAlpha - (int) ((mShadeHeight - bottomY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            } else if (bottomY > getHeight() - mShadeHeight) {
                alpha = mMaxAlpha - (int) ((bottomY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            }

            alpha = Math.max(alpha, 0);
            mPaint.setAlpha(alpha);
            mPaintHL.setAlpha(alpha);

            float textWidth = getTextWidth(mPaint, text);
            float textX = (getWidth() - textWidth) * 0.5f;
            //
            if (i < curLyricsLineNum) {
                canvas.drawText(text, textX, bottomY, mPaint);
                canvas.drawText(text, textX, bottomY, mPaintHL);
            } else if (i == curLyricsLineNum) {
                //绘画动感歌词
                drawDGCurLineLrc(canvas, lyricsLineInfos.get(i), bottomY, mLyricsWordIndex);
            } else if (i > curLyricsLineNum) {
                canvas.drawText(text, textX, bottomY, mPaint);
            }


        }

        //考虑部分歌词越界，导致高度不正确，这里重新获取基本歌词结束后的y轴位置
        bottomY = mCentreY + getLineHeight(mPaint) * (lyricsLineInfos.size() - 1);
        //画完成当前行歌词后，判断是否需要绘画翻译歌词和音译歌词
        if (mExtraLrcStatus == SHOWTRANSLATELRC) {

            //画翻译歌词
            bottomY = drawDGLineTranslateLrc(canvas, mLyricsLineNum, bottomY);


        } else if (mExtraLrcStatus == SHOWTRANSLITERATIONLRC) {
            //画音译歌词
            bottomY = drawDGLineTransliterationLrc(canvas, mLyricsLineNum, bottomY);
        }

        //
        topAndBottomY[0] = topY;
        topAndBottomY[1] = bottomY;

        return topAndBottomY;
    }


    /**
     * 往上绘画当前行歌词对应的翻译歌词
     *
     * @param canvas
     * @param lyricsLineNum
     * @param oldTopY
     * @return
     */
    private float drawUpLineTranslateLrc(Canvas canvas, int lyricsLineNum, float oldTopY) {
        float newTopY = oldTopY;

        if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {

            //获取分割后的翻译歌词行
            List<TranslateLrcLineInfo> translateLrcLineInfos = mTranslateLrcLineInfos.get(lyricsLineNum).getSplitTranslateLrcLineInfos();
            for (int i = translateLrcLineInfos.size() - 1; i >= 0; i--) {

                if (i == translateLrcLineInfos.size() - 1) {
                    newTopY = newTopY - mSpaceLineHeight - getTextHeight(mExtraLrcPaint);
                } else {
                    newTopY = newTopY - getExtraLrcLineHeight(mExtraLrcPaint);
                }

                //超出上视图
                if (newTopY < getExtraLrcLineHeight(mExtraLrcPaint)) {
                    continue;
                }
                //超出下视图
                if (newTopY + mExtraLrcSpaceLineHeight > getHeight()) {
                    continue;
                }

                String text = translateLrcLineInfos.get(i).getLineLyrics();
                //计算颜色透明度
                int alpha = mMaxAlpha;

                //颜色透明度过渡

                if (newTopY < mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((mShadeHeight - newTopY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                } else if (newTopY > getHeight() - mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((newTopY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                }

                alpha = Math.max(alpha, 0);
                mExtraLrcPaint.setAlpha(alpha);

                float textWidth = getTextWidth(mExtraLrcPaint, text);
                float textX = (getWidth() - textWidth) * 0.5f;

                canvas.drawText(text, textX, newTopY, mExtraLrcPaint);


            }
        }

        newTopY = newTopY + mExtraLrcSpaceLineHeight;

        return newTopY;
    }

    /***
     * 往上绘画当前行歌词对应的音译歌词
     * @param canvas
     * @param lyricsLineNum
     * @param oldTopY
     * @return
     */
    private float drawUpLineTransliterationLrc(Canvas canvas, int lyricsLineNum, float oldTopY) {
        float newTopY = oldTopY;

        if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {

            //获取分割后的音译歌词行
            List<LyricsLineInfo> transliterationLrcLineInfos = mTransliterationLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
            for (int i = transliterationLrcLineInfos.size() - 1; i >= 0; i--) {

                if (i == transliterationLrcLineInfos.size() - 1) {
                    newTopY = newTopY - mSpaceLineHeight - getTextHeight(mExtraLrcPaint);
                } else {
                    newTopY = newTopY - getExtraLrcLineHeight(mExtraLrcPaint);
                }

                //超出上视图
                if (newTopY < getExtraLrcLineHeight(mExtraLrcPaint)) {
                    continue;
                }
                //超出下视图
                if (newTopY + mExtraLrcSpaceLineHeight > getHeight()) {
                    continue;
                }

                String text = transliterationLrcLineInfos.get(i).getLineLyrics();
                //计算颜色透明度
                int alpha = mMaxAlpha;

                //颜色透明度过渡

                if (newTopY < mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((mShadeHeight - newTopY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                } else if (newTopY > getHeight() - mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((newTopY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                }

                alpha = Math.max(alpha, 0);
                mExtraLrcPaint.setAlpha(alpha);

                float textWidth = getTextWidth(mExtraLrcPaint, text);
                float textX = (getWidth() - textWidth) * 0.5f;

                canvas.drawText(text, textX, newTopY, mExtraLrcPaint);


            }
        }

        newTopY = newTopY + mExtraLrcSpaceLineHeight;

        return newTopY;
    }

    /**
     * 往下绘画当前行歌词对应的翻译歌词
     *
     * @param canvas
     * @param lyricsLineNum
     * @param oldBottomY
     * @return
     */
    private float drawDownLineTranslateLrc(Canvas canvas, int lyricsLineNum, float oldBottomY) {
        float newBottomY = oldBottomY;


        if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {

            //获取分割后的翻译歌词行
            List<TranslateLrcLineInfo> translateLrcLineInfos = mTranslateLrcLineInfos.get(lyricsLineNum).getSplitTranslateLrcLineInfos();
            for (int i = 0; i < translateLrcLineInfos.size(); i++) {

                newBottomY = newBottomY + getExtraLrcLineHeight(mExtraLrcPaint);

                //超出上视图
                if (newBottomY < getLineHeight(mExtraLrcPaint)) {
                    continue;
                }
                //超出下视图
                if (newBottomY + mExtraLrcSpaceLineHeight > getHeight()) {
                    break;
                }


                String text = translateLrcLineInfos.get(i).getLineLyrics();
                //计算颜色透明度
                int alpha = mMaxAlpha;

                //颜色透明度过渡

                if (newBottomY < mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((mShadeHeight - newBottomY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                } else if (newBottomY > getHeight() - mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((newBottomY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                }

                alpha = Math.max(alpha, 0);
                mExtraLrcPaint.setAlpha(alpha);

                float textWidth = getTextWidth(mExtraLrcPaint, text);
                float textX = (getWidth() - textWidth) * 0.5f;

                canvas.drawText(text, textX, newBottomY, mExtraLrcPaint);
            }

            //考虑部分歌词越界，导致高度不正确，这里重新获取结束后的y轴位置
            newBottomY = oldBottomY + getExtraLrcLineHeight(mExtraLrcPaint) * translateLrcLineInfos.size();

        }

        return newBottomY;
    }

    /**
     * 画音译歌词
     *
     * @param canvas
     * @param lyricsLineNum
     * @param oldBottomY
     * @return
     */
    private float drawDownLineTransliterationLrc(Canvas canvas, int lyricsLineNum, float oldBottomY) {

        float newBottomY = oldBottomY;


        if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {

            //获取分割后的音译歌词行
            List<LyricsLineInfo> transliterationLrcLineInfos = mTransliterationLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
            for (int i = 0; i < transliterationLrcLineInfos.size(); i++) {

                newBottomY = newBottomY + getExtraLrcLineHeight(mExtraLrcPaint);

                //超出上视图
                if (newBottomY < getLineHeight(mExtraLrcPaint)) {
                    continue;
                }
                //超出下视图
                if (newBottomY + mExtraLrcSpaceLineHeight > getHeight()) {
                    break;
                }


                String text = transliterationLrcLineInfos.get(i).getLineLyrics();
                //计算颜色透明度
                int alpha = mMaxAlpha;

                //颜色透明度过渡

                if (newBottomY < mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((mShadeHeight - newBottomY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                } else if (newBottomY > getHeight() - mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((newBottomY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                }

                alpha = Math.max(alpha, 0);
                mExtraLrcPaint.setAlpha(alpha);

                float textWidth = getTextWidth(mExtraLrcPaint, text);
                float textX = (getWidth() - textWidth) * 0.5f;

                canvas.drawText(text, textX, newBottomY, mExtraLrcPaint);
            }

            //考虑部分歌词越界，导致高度不正确，这里重新获取结束后的y轴位置
            newBottomY = oldBottomY + getExtraLrcLineHeight(mExtraLrcPaint) * transliterationLrcLineInfos.size();

        }

        return newBottomY;
    }


    /**
     * 往下绘画当前行歌词对应的翻译歌词
     *
     * @param canvas
     * @param oldBottomY
     * @return
     */
    private float drawDGLineTranslateLrc(Canvas canvas, int lyricsLineNum, float oldBottomY) {
        float newBottomY = oldBottomY;

        if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {

            //获取分割后的翻译歌词行
            List<TranslateLrcLineInfo> translateLrcLineInfos = mTranslateLrcLineInfos.get(lyricsLineNum).getSplitTranslateLrcLineInfos();
            for (int i = 0; i < translateLrcLineInfos.size(); i++) {

                newBottomY = newBottomY + getExtraLrcLineHeight(mExtraLrcPaint);

                //超出上视图
                if (newBottomY < getLineHeight(mExtraLrcPaint)) {
                    continue;
                }
                //超出下视图
                if (newBottomY + mExtraLrcSpaceLineHeight > getHeight()) {
                    break;
                }

                String text = translateLrcLineInfos.get(i).getLineLyrics();
                //计算颜色透明度
                int alpha = mMaxAlpha;

                //颜色透明度过渡

                if (newBottomY < mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((mShadeHeight - newBottomY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                } else if (newBottomY > getHeight() - mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((newBottomY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                }

                alpha = Math.max(alpha, 0);
                mExtraLrcPaint.setAlpha(alpha);

                float textWidth = getTextWidth(mExtraLrcPaint, text);
                float textX = (getWidth() - textWidth) * 0.5f;

                canvas.drawText(text, textX, newBottomY, mExtraLrcPaint);


            }
            //考虑部分歌词越界，导致高度不正确，这里重新获取结束后的y轴位置
            newBottomY = oldBottomY + getExtraLrcLineHeight(mExtraLrcPaint) * translateLrcLineInfos.size();
        }
        return newBottomY;
    }

    /***
     * 画动感音译歌词
     * @param canvas
     * @param lyricsLineNum
     * @param oldBottomY
     * @return
     */
    private float drawDGLineTransliterationLrc(Canvas canvas, int lyricsLineNum, float oldBottomY) {
        float newBottomY = oldBottomY;

        if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {

            //获取分割后的音译歌词行
            List<LyricsLineInfo> transliterationLrcLineInfos = mTransliterationLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();

            int curLyricsLineNum = mExtraLyricsLineNum;
            if (mExtraLyricsWordIndex == -1) {
                //设置为最后索引，防止跳转到下一句时，前面歌词不是高亮的问题
                curLyricsLineNum = transliterationLrcLineInfos.size() - 1;
            }

            for (int i = 0; i < transliterationLrcLineInfos.size(); i++) {

                newBottomY = newBottomY + getExtraLrcLineHeight(mExtraLrcPaint);

                //超出上视图
                if (newBottomY < getLineHeight(mExtraLrcPaint)) {
                    continue;
                }
                //超出下视图
                if (newBottomY + mExtraLrcSpaceLineHeight > getHeight()) {
                    break;
                }

                String text = transliterationLrcLineInfos.get(i).getLineLyrics();
                //计算颜色透明度
                int alpha = mMaxAlpha;

                //颜色透明度过渡

                if (newBottomY < mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((mShadeHeight - newBottomY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                } else if (newBottomY > getHeight() - mShadeHeight) {
                    alpha = mMaxAlpha - (int) ((newBottomY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                }

                alpha = Math.max(alpha, 0);
                mExtraLrcPaint.setAlpha(alpha);
                mExtraLrcPaintHL.setAlpha(alpha);

                float textWidth = getTextWidth(mExtraLrcPaint, text);
                float textX = (getWidth() - textWidth) * 0.5f;

                //
                if (i < curLyricsLineNum) {
                    canvas.drawText(text, textX, newBottomY, mExtraLrcPaint);
                    canvas.drawText(text, textX, newBottomY, mExtraLrcPaintHL);
                } else if (i == curLyricsLineNum) {
                    //绘画音译的动感歌词
                    drawDGCurTransliterationLineLrc(canvas, transliterationLrcLineInfos.get(i), newBottomY, mExtraLyricsWordIndex);

                } else if (i > curLyricsLineNum) {
                    canvas.drawText(text, textX, newBottomY, mExtraLrcPaint);
                }
            }
            //考虑部分歌词越界，导致高度不正确，这里重新获取结束后的y轴位置
            newBottomY = oldBottomY + getExtraLrcLineHeight(mExtraLrcPaint) * transliterationLrcLineInfos.size();
        }
        return newBottomY;
    }

    /***
     * 绘画音译的动感歌词行
     * @param canvas
     * @param transliterationLrcLineInfo
     * @param centreLastBottomY
     * @param curLyricsWordIndex
     */
    private void drawDGCurTransliterationLineLrc(Canvas canvas, LyricsLineInfo transliterationLrcLineInfo, float centreLastBottomY, int curLyricsWordIndex) {

        float extraLyricsHLWidth = 0;
        // 整行歌词
        String curLyrics = transliterationLrcLineInfo.getLineLyrics();
        int curLyricsHeight = getTextHeight(mExtraLrcPaintHL);
        float curLyricsWidth = getTextWidth(mExtraLrcPaintHL, curLyrics);

        // 歌词
        if (curLyricsWordIndex == -1) {
            //设置等于当行歌词的大小，防止跳转下一行歌词后，该行歌词不为高亮状态
            extraLyricsHLWidth = curLyricsWidth;
        } else {
            String lyricsWords[] = transliterationLrcLineInfo.getLyricsWords();
            int wordsDisInterval[] = transliterationLrcLineInfo
                    .getWordsDisInterval();
            // 当前歌词之前的歌词
            String lyricsBeforeWord = "";
            for (int i = 0; i < curLyricsWordIndex; i++) {
                lyricsBeforeWord += lyricsWords[i];
            }
            // 当前歌词
            String lyricsNowWord = lyricsWords[curLyricsWordIndex].trim();// 去掉空格

            // 当前歌词之前的歌词长度
            float lyricsBeforeWordWidth = getTextWidth(mExtraLrcPaintHL, lyricsBeforeWord);

            // 当前歌词长度
            float lyricsNowWordWidth = getTextWidth(mExtraLrcPaintHL, lyricsNowWord);

            float len = lyricsNowWordWidth
                    / wordsDisInterval[curLyricsWordIndex]
                    * mLyricsWordHLTime;

            extraLyricsHLWidth = lyricsBeforeWordWidth + len;
        }
        float curTextX = (getWidth() - curLyricsWidth) * 0.5f;


        // save和restore是为了剪切操作不影响画布的其它元素
        canvas.save();

        // 画当前歌词
        canvas.drawText(curLyrics, curTextX, centreLastBottomY, mExtraLrcPaint);

        // 设置过渡的颜色和进度
        canvas.clipRect(curTextX, centreLastBottomY - getClipTextHeight(mExtraLrcPaint), curTextX + extraLyricsHLWidth,
                centreLastBottomY + getRealTextHeight(mExtraLrcPaint));

        // 画当前歌词
        canvas.drawText(curLyrics, curTextX, centreLastBottomY, mExtraLrcPaintHL);
        canvas.restore();

    }

    /**
     * 画动感歌词当前行歌词
     *
     * @param canvas
     * @param centreLastBottomY
     */
    private void drawDGCurLineLrc(Canvas canvas, LyricsLineInfo lyricsLineInfo, float centreLastBottomY, int curLyricsWordIndex) {

        float lineLyricsHLWidth = 0;
        // 整行歌词
        String curLyrics = lyricsLineInfo.getLineLyrics();
        int curLyricsHeight = getTextHeight(mPaintHL);
        float curLyricsWidth = getTextWidth(mPaintHL, curLyrics);

        // 歌词
        if (curLyricsWordIndex == -1) {
            //设置等于当行歌词的大小，防止跳转下一行歌词后，该行歌词不为高亮状态
            lineLyricsHLWidth = curLyricsWidth;
        } else {
            String lyricsWords[] = lyricsLineInfo.getLyricsWords();
            int wordsDisInterval[] = lyricsLineInfo
                    .getWordsDisInterval();
            // 当前歌词之前的歌词
            String lyricsBeforeWord = "";
            for (int i = 0; i < curLyricsWordIndex; i++) {
                lyricsBeforeWord += lyricsWords[i];
            }
            // 当前歌词
            String lyricsNowWord = lyricsWords[curLyricsWordIndex].trim();// 去掉空格

            // 当前歌词之前的歌词长度
            float lyricsBeforeWordWidth = getTextWidth(mPaintHL, lyricsBeforeWord);

            // 当前歌词长度
            float lyricsNowWordWidth = getTextWidth(mPaintHL, lyricsNowWord);

            float len = lyricsNowWordWidth
                    / wordsDisInterval[curLyricsWordIndex]
                    * mLyricsWordHLTime;
            lineLyricsHLWidth = lyricsBeforeWordWidth + len;
        }
        float curTextX = (getWidth() - curLyricsWidth) * 0.5f;


        // save和restore是为了剪切操作不影响画布的其它元素
        canvas.save();

        // 画当前歌词
        canvas.drawText(curLyrics, curTextX, centreLastBottomY, mPaint);

        // 设置过渡的颜色和进度
        canvas.clipRect(curTextX, centreLastBottomY - getClipTextHeight(mPaint), curTextX + lineLyricsHLWidth,
                centreLastBottomY + getRealTextHeight(mPaint));

        // 画当前歌词
        canvas.drawText(curLyrics, curTextX, centreLastBottomY, mPaintHL);
        canvas.restore();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLyricsLineTreeMap == null || mLyricsLineTreeMap.size() == 0 || !isManyLineLrc || !mTouchAble) {
            return super.onTouchEvent(event);
        }
        obtainVelocityTracker(event);
        try {
            int actionId = event.getAction();
            switch (actionId) {
                case MotionEvent.ACTION_DOWN:

                    isTouchDown = true;
                    //发送还原
                    handler.removeMessages(RESETLRCVIEW);

                    mLastY = (int) event.getY();
                    mInterceptX = (int) event.getX();
                    mInterceptY = (int) event.getY();

                    break;
                case MotionEvent.ACTION_MOVE:
                    int curX = (int) event.getX();
                    int curY = (int) event.getY();
                    int deltaX = (int) (mInterceptX - curX);
                    int deltaY = (int) (mInterceptY - curY);

                    if (isTouchMove || (Math.abs(deltaY) > mTouchSlop && Math.abs(deltaX) < mTouchSlop)) {
                        isTouchMove = true;

                        int dy = mLastY - curY;

                        //创建阻尼效果
                        float finalY = mOffsetY + dy;

                        if (finalY < getTopOverScrollHeight()) {
                            dy = dy / 2;
                            isOverScroll = true;

                        } else if (finalY > getBottomOverScrollHeight()) {

                            dy = dy / 2;
                            isOverScroll = true;
                        }

                        mScroller.startScroll(0, mScroller.getFinalY(), 0, dy, 0);
                        invalidateView();

                        isFlingScroll = true;
                    }

                    mLastY = curY;
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    isTouchDown = false;


                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

                    int yVelocity = (int) velocityTracker.getYVelocity();
                    int xVelocity = (int) velocityTracker.getXVelocity();

                    if (Math.abs(yVelocity) > mMinimumVelocity) {

                        int startX = 0;
                        int startY = mScroller.getFinalY();
                        int velocityX = -xVelocity;
                        int velocityY = -yVelocity;
                        int minX = 0;
                        int maxX = 0;

                        //
                        int lrcSumHeight = (int) getLineScollHeight(mLyricsLineTreeMap.size());
                        int minY = -getHeight() / 4;
                        int maxY = lrcSumHeight + getHeight() / 4;
                        mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
                        invalidateView();

                        //发送还原
                        handler.sendEmptyMessageDelayed(RESETLRCVIEW, mResetDuration);
                    } else {

                        if (isOverScroll) {
                            isOverScroll = false;
                            resetLrcView();
                        } else {
                            //发送还原
                            handler.sendEmptyMessageDelayed(RESETLRCVIEW, mResetDuration);

                        }
                    }

                    mLastY = 0;
                    mInterceptX = 0;
                    mInterceptY = 0;
                    releaseVelocityTracker();


                    //判断是否在滑动和是否点击了播放按钮
                    if (isPlayClick(event) && isTouchMove) {

                        handler.removeMessages(RESETLRCVIEW);

                        if (mOnLrcClickListener != null) {

                            //

                            //获取当前滑动到的歌词播放行
                            int scrollLrcLineNum = getScrollLrcLineNum(mOffsetY);
                            //logger.e("LineLyrics = " + mLyricsLineTreeMap.get(scrollLrcLineNum).getLineLyrics());
                            int startTime = mLyricsLineTreeMap.get(scrollLrcLineNum).getStartTime();
                            int theFristWordTime = mLyricsLineTreeMap.get(scrollLrcLineNum).getWordsDisInterval()[0];
                            mOnLrcClickListener.onLrcPlayClicked(startTime + theFristWordTime, true);

                        }

                        isTouchMove = false;
                        //invalidateView();
                    }

                    break;
                default:
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
        //
        if (mTouchIntercept) {
            return mTouchIntercept;
        }
        return isTouchMove;
    }

    /**
     * 获取底部越界
     *
     * @return
     */
    private float getBottomOverScrollHeight() {
        return getLineScollHeight(mLyricsLineTreeMap.size());
    }

    /**
     * 获取顶部越界高度
     *
     * @return
     */
    private float getTopOverScrollHeight() {
        return 0;
    }


    /**
     * 还原歌词视图
     */
    private void resetLrcView() {

        if (mOffsetY < 0) {

            int deltaY = -mScroller.getFinalY();
            mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
            invalidateView();
        } else if (mOffsetY > getBottomOverScrollHeight()) {

            int deltaY = (int) getLineScollHeight(mLyricsLineTreeMap.size() - 1) - mScroller.getFinalY();
            mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
            invalidateView();

        }
    }

    /**
     * 获取滑动到该行所需的高度
     *
     * @param lyricsLineNum
     * @return
     */
    private float getLineScollHeight(int lyricsLineNum) {
        int scrollHeight = 0;

        for (int i = 0; i < lyricsLineNum; i++) {
            LyricsLineInfo lyricsLineInfo = mLyricsLineTreeMap
                    .get(i);
            //获取分割后的歌词列表
            List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
            scrollHeight += getLineHeight(mPaint) * lyricsLineInfos.size();

            //判断是否有翻译歌词或者音译歌词
            if (mExtraLrcStatus == SHOWTRANSLATELRC) {
                if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {
                    List<TranslateLrcLineInfo> translateLrcLineInfos = mTranslateLrcLineInfos.get(i).getSplitTranslateLrcLineInfos();
                    scrollHeight += getExtraLrcLineHeight(mExtraLrcPaint) * translateLrcLineInfos.size();
                }
            } else if (mExtraLrcStatus == SHOWTRANSLITERATIONLRC) {
                if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> transliterationLrcLineInfos = mTransliterationLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    scrollHeight += getExtraLrcLineHeight(mExtraLrcPaint) * transliterationLrcLineInfos.size();
                }
            }
        }
        return scrollHeight;
    }


    /**
     * 获取滑动的当前行
     *
     * @return
     */
    private int getScrollLrcLineNum(float offsetY) {
        if (mLyricsLineTreeMap == null || mLyricsLineTreeMap.size() == 0) {
            return 0;
        }

        int scrollLrcLineNum = -1;
        int lineHeight = 0;
        for (int i = 0; i < mLyricsLineTreeMap.size(); i++) {
            LyricsLineInfo lyricsLineInfo = mLyricsLineTreeMap
                    .get(i);
            //获取分割后的歌词列表
            List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
            lineHeight += getLineHeight(mPaint) * lyricsLineInfos.size();

            //判断是否有翻译歌词或者音译歌词
            if (mExtraLrcStatus == SHOWTRANSLATELRC) {
                if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {
                    List<TranslateLrcLineInfo> translateLrcLineInfos = mTranslateLrcLineInfos.get(i).getSplitTranslateLrcLineInfos();
                    lineHeight += getExtraLrcLineHeight(mExtraLrcPaint) * translateLrcLineInfos.size();
                }
            } else if (mExtraLrcStatus == SHOWTRANSLITERATIONLRC) {
                if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> transliterationLrcLineInfos = mTransliterationLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    lineHeight += getExtraLrcLineHeight(mExtraLrcPaint) * transliterationLrcLineInfos.size();
                }
            }


            if (lineHeight > offsetY) {
                scrollLrcLineNum = i;
                break;
            }
        }
        if (scrollLrcLineNum == -1) {
            scrollLrcLineNum = mLyricsLineTreeMap.size() - 1;
        }
        return scrollLrcLineNum;
    }


    /**
     * @param event
     */

    private void obtainVelocityTracker(MotionEvent event) {

        if (mVelocityTracker == null) {

            mVelocityTracker = VelocityTracker.obtain();

        }

        mVelocityTracker.addMovement(event);

    }


    /**
     * 释放
     */
    private void releaseVelocityTracker() {

        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;

        }

    }


    /**
     * 判断是否是播放按钮点击
     *
     * @param event
     * @return
     */
    private boolean isPlayClick(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();
        return mPlayRect.contains(x, y);

    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        // 更新当前的X轴偏移量
        if (mScroller.computeScrollOffset()) { // 返回true代表正在模拟数据，false 已经停止模拟数据

            mOffsetY = mScroller.getCurrY();

            // logger.e("mOffsetY=" + mOffsetY);
            invalidateView();
        } else {
            if (isFlingScroll && !isTouchDown) {
                isFlingScroll = false;
                resetLrcView();
            }
        }
    }


    public LyricsUtil getLyricsUtil() {
        return mLyricsUtil;
    }

    public TreeMap<Integer, LyricsLineInfo> getLyricsLineTreeMap() {
        return mLyricsLineTreeMap;
    }

    /**
     * 设置歌词
     *
     * @param mLyricsUtil
     * @param textMaxWidth 歌词最大宽度
     */
    public void setLyricsUtil(LyricsUtil mLyricsUtil, int textMaxWidth, int curPlayingTime) {
        this.mLyricsUtil = mLyricsUtil;
        this.mTextMaxWidth = textMaxWidth;
        if (mLyricsUtil != null && textMaxWidth != 0) {
            if (isManyLineLrc) {

                mLyricsLineTreeMap = mLyricsUtil.getSplitLyrics(textMaxWidth, mPaint);
                mTranslateLrcLineInfos = mLyricsUtil.getSplitTranslateLyrics(textMaxWidth, mExtraLrcPaint);
                mTransliterationLrcLineInfos = mLyricsUtil.getSplitTransliterationLyrics(textMaxWidth, mPaint);

            } else {

                mLyricsLineTreeMap = mLyricsUtil.getReconstructLyrics(textMaxWidth, mPaint);
            }
        } else {
            mLyricsLineTreeMap = null;
        }
        resetData();
        //额外歌词类型回调
        extraLrcTypeCallBack(curPlayingTime);
        invalidateView();
    }

    /**
     * 额外歌词类型回调
     */
    private void extraLrcTypeCallBack(int curPlayingTime) {
        if (mLyricsUtil != null && mExtraLyricsListener != null && mLyricsLineTreeMap != null) {
            int extraLrcType = mLyricsUtil.getExtraLrcType();
            if (extraLrcType == LyricsUtil.TRANSLATE_AND_TRANSLITERATION_LRC) {
                //有翻译歌词和音译歌词
                if (mExtraLyricsListener != null) {
                    mExtraLyricsListener.hasTranslateAndTransliterationLrcCallback();
                }
                mExtraLrcStatus = SHOWTRANSLATELRC;
            } else if (extraLrcType == LyricsUtil.TRANSLATE_LRC) {
                //有翻译歌词
                if (mExtraLyricsListener != null) {
                    mExtraLyricsListener.hasTranslateLrcCallback();
                }
                mExtraLrcStatus = SHOWTRANSLATELRC;
            } else if (extraLrcType == LyricsUtil.TRANSLITERATION_LRC) {
                //音译歌词
                if (mExtraLyricsListener != null) {
                    mExtraLyricsListener.hasTransliterationLrcCallback();
                }
                mExtraLrcStatus = SHOWTRANSLITERATIONLRC;
            } else {
                //无翻译歌词和音译歌词
                mExtraLyricsListener.noExtraLrcCallback();
                mExtraLrcStatus = NOSHOWEXTRALRC;
            }

        } else {
            if (mExtraLyricsListener != null) {
                mExtraLyricsListener.noExtraLrcCallback();
            }
            mExtraLrcStatus = NOSHOWEXTRALRC;
        }
        if (isManyLineLrc)
            setExtraLrcStatus(mExtraLrcStatus, curPlayingTime);
    }

    /**
     * 重置数据
     */
    private void resetData() {

        isTouchMove = false;
        //
        mLyricsLineNum = 0;
        mLyricsWordIndex = -1;
        mLyricsWordHLTime = 0;

        mScroller.setFinalY(0);
        mOffsetY = 0;
        mSplitLyricsLineNum = 0;
        //
        mExtraLyricsLineNum = 0;
        mExtraLyricsWordIndex = -1;
        mExtraLrcStatus = NOSHOWEXTRALRC;

    }

    /**
     * 刷新View
     */
    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            //  当前线程是主UI线程，直接刷新。
            invalidate();
        } else {
            //  当前线程是非UI线程，post刷新。
            postInvalidate();
        }
    }

    /**
     * 获取每行高度。用于y轴位置计算
     *
     * @param paint
     * @return
     */
    public int getLineHeight(Paint paint) {
        return getTextHeight(paint) + mSpaceLineHeight;
    }

    /**
     * 获取额外歌词行高度。用于y轴位置计算
     *
     * @param paint
     * @return
     */
    public int getExtraLrcLineHeight(Paint paint) {
        return getTextHeight(paint) + mExtraLrcSpaceLineHeight;
    }

    /**
     * 获取行歌词高度。用于y轴位置计算
     *
     * @param paint
     * @return
     */
    private int getTextHeight(Paint paint) {
//        Rect rect = new Rect();
//        paint.getTextBounds(text, 0, text.length(), rect);
//        return rect.height();

        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) -(fm.ascent + fm.descent);
    }

    /**
     * 获取真实的歌词高度
     *
     * @param paint
     * @return
     */
    private int getRealTextHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) (-fm.leading - fm.ascent + fm.descent);
    }

    /**
     * 获取切割的高度
     *
     * @param paint
     * @return
     */
    private int getClipTextHeight(Paint paint) {
//        Paint.FontMetrics fm = paint.getFontMetrics();
//        return (int) (-fm.leading - fm.ascent);
        return getRealTextHeight(paint);
    }


    /**
     * 获取文本宽度
     *
     * @param paint
     * @param text
     * @return
     */
    private float getTextWidth(Paint paint, String text) {
        return paint
                .measureText(text);
    }


    ////////////////////

    public void setOnLrcClickListener(OnLrcClickListener mOnLrcClickListener) {
        this.mOnLrcClickListener = mOnLrcClickListener;
    }

    public void setExtraLyricsListener(ExtraLyricsListener mExtraLyricsListener) {
        this.mExtraLyricsListener = mExtraLyricsListener;
    }

    /**
     * 更新歌词
     */
    public void updateView(int playProgress) {
        if (mLyricsUtil == null || isReconstruct || mLyricsLineTreeMap == null) return;
        //

        int newLyricsLineNum = mLyricsUtil.getLineNumber(mLyricsLineTreeMap, playProgress);
        if (newLyricsLineNum != mLyricsLineNum) {

            if (!isTouchMove && isManyLineLrc) {
                //

                int deltaY = (int) getLineScollHeight(newLyricsLineNum) - mScroller.getFinalY();
                mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);

            }

            mLyricsLineNum = newLyricsLineNum;
        }
        if (!isManyLineLrc) {
            //获取歌词字索引
            mLyricsWordIndex = mLyricsUtil.getDisWordsIndex(mLyricsLineTreeMap, mLyricsLineNum, playProgress);
        } else {

            //获取分割后的索引
            mSplitLyricsLineNum = mLyricsUtil.getSplitLyricsLineNum(mLyricsLineTreeMap, mLyricsLineNum, playProgress);
            //获取分割后的歌词字索引
            mLyricsWordIndex = mLyricsUtil.getSplitLyricsWordIndex(mLyricsLineTreeMap, mLyricsLineNum, playProgress);

            //判断是否显示音译歌词
            if (mExtraLrcStatus == SHOWTRANSLITERATIONLRC) {
                if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
                    mExtraLyricsLineNum = mLyricsUtil.getSplitTransliterationLyricsLineNum(mTransliterationLrcLineInfos, mLyricsLineNum, playProgress);
                    mExtraLyricsWordIndex = mLyricsUtil.getSplitTransliterationLyricsWordIndex(mTransliterationLrcLineInfos, mLyricsLineNum, playProgress);
                }
            }

        }
        mLyricsWordHLTime = mLyricsUtil.getDisWordsIndexLenTime(mLyricsLineTreeMap, mLyricsLineNum, playProgress);

        invalidateView();
    }

    /**
     * 设置歌词颜色
     *
     * @param color
     */
    public void setLrcColor(int color) {
        mLrcColor = color;
        initColor();
        invalidateView();
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     */
    public void setLrcFontSize(int fontSize) {
        mFontSize = fontSize;
        initFontSize();
        //重绘
        invalidateView();
    }

    /**
     * 设置歌词字体大小,有歌词时使用
     *
     * @param fontSize
     * @param curPlayingTime
     */
    public synchronized void setLrcFontSize(int fontSize, int curPlayingTime) {
        //不存在歌词时
        if (mLyricsLineTreeMap == null || mLyricsLineTreeMap.size() == 0) {
            setLrcFontSize(fontSize);
            return;
        }
        isReconstruct = true;
        mFontSize = fontSize;
        initFontSize();

        //多行歌词更新歌词集合
        if (isManyLineLrc) {
            mLyricsLineTreeMap = mLyricsUtil.getSplitLyrics(mTextMaxWidth, mPaint);
            mTranslateLrcLineInfos = mLyricsUtil.getSplitTranslateLyrics(mTextMaxWidth, mExtraLrcPaint);
            mTransliterationLrcLineInfos = mLyricsUtil.getSplitTransliterationLyrics(mTextMaxWidth, mPaint);
        } else {
            mLyricsLineTreeMap = mLyricsUtil.getReconstructLyrics(mTextMaxWidth, mPaint);
        }
        //更新歌词行索引
        int newLyricsLineNum = mLyricsUtil.getLineNumber(mLyricsLineTreeMap, curPlayingTime);
        if (newLyricsLineNum != mLyricsLineNum) {
            mLyricsLineNum = newLyricsLineNum;
        }
        //多行歌词
        if (isManyLineLrc && mLyricsLineNum != -1) {
            mOffsetY = getLineScollHeight(mLyricsLineNum);
            mScroller.setFinalY((int) mOffsetY);
        }
        isReconstruct = false;
        updateView(curPlayingTime);
    }

    /**
     * 设置默认歌词颜色
     *
     * @param color
     */
    public void setDefLrcColor(int color) {
        mDefLrcColor = color;
        initColor();
        invalidateView();
    }

    public void setTouchAble(boolean mTouchAble) {
        this.mTouchAble = mTouchAble;
    }

    /***
     *
     * 直接拦截touch操作
     */
    public void setTouchInterceptTrue() {
        mTouchIntercept = true;
    }

    /**
     * 设置是否是多行歌词
     *
     * @param manyLineLrc
     * @param curPlayingTime
     */
    public synchronized void setManyLineLrc(boolean manyLineLrc, int curPlayingTime) {
        initLrcMap(manyLineLrc, curPlayingTime);
        //额外歌词类型回调
        extraLrcTypeCallBack(curPlayingTime);
    }

    /**
     * 设置歌词集合
     *
     * @param manyLineLrc
     * @param curPlayingTime
     */
    private void initLrcMap(boolean manyLineLrc, int curPlayingTime) {
        isManyLineLrc = manyLineLrc;
        if (mLyricsUtil != null && !isManyLineLrc) {
            isReconstruct = true;
            isTouchMove = false;
            mLyricsLineTreeMap = mLyricsUtil.getReconstructLyrics(mTextMaxWidth, mPaint);
            isReconstruct = false;

        } else if (mLyricsUtil != null) {
            isReconstruct = true;
            isTouchMove = false;
            mLyricsLineTreeMap = mLyricsUtil.getSplitLyrics(mTextMaxWidth, mPaint);
            mTranslateLrcLineInfos = mLyricsUtil.getSplitTranslateLyrics(mTextMaxWidth, mExtraLrcPaint);
            mTransliterationLrcLineInfos = mLyricsUtil.getSplitTransliterationLyrics(mTextMaxWidth, mPaint);
            isReconstruct = false;

            //更新歌词行索引
            int newLyricsLineNum = mLyricsUtil.getLineNumber(mLyricsLineTreeMap, curPlayingTime);
            if (newLyricsLineNum != mLyricsLineNum) {
                mLyricsLineNum = newLyricsLineNum;
            }
            //多行歌词
            if (isManyLineLrc && mLyricsLineNum != -1) {
                mOffsetY = getLineScollHeight(mLyricsLineNum);
                mScroller.setFinalY((int) mOffsetY);
            }

        }

        updateView(curPlayingTime);
    }

    public boolean isManyLineLrc() {
        return isManyLineLrc;
    }

    /**
     * 设置额外歌词的状态
     *
     * @param mExtraLrcStatus
     * @param curPlayingTime
     */
    public void setExtraLrcStatus(int mExtraLrcStatus, int curPlayingTime) {
        this.mExtraLrcStatus = mExtraLrcStatus;
        if (mLyricsUtil == null || mLyricsLineTreeMap == null) {
            return;
        }
        if (mExtraLrcStatus != NOSHOWEXTRALRC && !isManyLineLrc) {
            initLrcMap(true, curPlayingTime);
        } else {
            isReconstruct = true;
            isTouchMove = false;
            //更新歌词行索引
            int newLyricsLineNum = mLyricsUtil.getLineNumber(mLyricsLineTreeMap, curPlayingTime);
            if (newLyricsLineNum != mLyricsLineNum) {
                mLyricsLineNum = newLyricsLineNum;
            }
            //多行歌词
            if (isManyLineLrc && mLyricsLineNum != -1) {
                mOffsetY = getLineScollHeight(mLyricsLineNum);
                mScroller.setFinalY((int) mOffsetY);
            }
            isReconstruct = false;
            updateView(curPlayingTime);
        }
    }

    /**
     * 歌词事件
     */
    public interface OnLrcClickListener {
        /**
         * 歌词快进播放
         *
         * @param progress
         */
        void onLrcPlayClicked(int progress, boolean isLrcSeekTo);
    }

    /**
     * 额外歌词事件
     */
    public interface ExtraLyricsListener {

        /**
         * 有翻译歌词回调
         */
        void hasTranslateLrcCallback();

        /**
         * 有音译歌词回调
         */
        void hasTransliterationLrcCallback();

        /**
         * 有翻译歌词和音译歌词回调
         */
        void hasTranslateAndTransliterationLrcCallback();

        /**
         * 无翻译和音译歌词回调
         */
        void noExtraLrcCallback();
    }
}

