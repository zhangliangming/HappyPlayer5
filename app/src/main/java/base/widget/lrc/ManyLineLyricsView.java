package base.widget.lrc;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.os.Build;
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
import com.zlm.hp.mp3.lyrics.utils.LyricsUtil;
import com.zlm.hp.utils.MediaUtil;

import java.util.TreeMap;

import base.utils.ColorUtil;
import base.utils.LoggerUtil;

/**
 * 多行歌词:歌词行号和view所在位置关联,Scroller只做动画处理，不去移动view
 * Created by zhangliangming on 2017/8/19.
 */

public class ManyLineLyricsView extends View {
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
     * 当前歌词的所在行数
     */
    private int mLyricsLineNum = 0;

    /**
     * 当前歌词的第几个字
     */
    private int mLyricsWordIndex = -1;

    /**
     * 当前歌词第几个字 已经播放的长度
     */
    private float mLineLyricsHLWidth = 0;
    /**
     * 当前歌词第几个字 已经播放的时间
     */
    private float mLyricsWordHLTime = 0;


    ///////////////////////////////////////
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
    private int mDuration = 200;

    /**
     * 歌词在Y轴上的偏移量
     */
    private float mOffsetY = 0;
    /**
     * 视图y中间
     */
    private float mCentreY = 0;

    /**
     * 颜色渐变梯度
     */
    private int mMaxAlpha = 255;
    private int mMinAlpha = 50;
    //渐变的高度
    private int mShadeHeight = 0;

    /**
     * 字体的高度进行微调
     */
    private int mAdjustLrcHeightNum = 10;

    /////////////////////////////////////////////////////

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
                        int deltaY = mLyricsLineNum * getLineHeight(mPaint) - mScroller.getFinalY();
                        mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
                        invalidateView();
                    }

                    break;
            }
        }
    };

    private OnLrcClickListener mOnLrcClickListener;

    /**
     * 判断歌词集合是否在重构
     */
    private boolean isReconstruct = false;

    /**
     * 是否是多行歌词
     */
    private boolean isManyLineLrc = true;

    public ManyLineLyricsView(Context context) {
        super(context);
        init(context);
    }

    public ManyLineLyricsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ManyLineLyricsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ManyLineLyricsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        canvas.clipRect(textX, textY - textHeight - mAdjustLrcHeightNum, textX + textWidth * 0.5f,
                textY + textHeight + mAdjustLrcHeightNum);

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
        canvas.clipRect(textX, textY - textHeight - mAdjustLrcHeightNum, textX + textWidth * 0.5f,
                textY + textHeight + mAdjustLrcHeightNum);

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

            //画当前动感歌词行
            drawDGLineLrc(canvas, 0, false);

        }
    }


    /**
     * 画歌词
     *
     * @param canvas
     */
    private void drawManyLineLrcText(Canvas canvas) {
        mCentreY = (getHeight() + getTextHeight(mPaintHL)) * 0.5f + mLyricsLineNum * getLineHeight(mPaint) - mOffsetY;
        // logger.e("mCentreY=" + mCentreY);

        //获取要透明度要渐变的高度大小
        if (mShadeHeight == 0) {
            initShadeHeight();
        }

        // 画当前歌词之前的歌词
        for (int i = mLyricsLineNum - 1; i >= 0; i--) {
            String text = mLyricsLineTreeMap.get(i).getLineLyrics();
            float textY = mCentreY
                    - (mLyricsLineNum - i) * getLineHeight(mPaint);

            //超出上视图
            if (textY < getLineHeight(mPaint)) {
                break;
            }
            //超出下视图
            if (textY + mSpaceLineHeight > getHeight()) {
                continue;
            }

            //计算颜色透明度
            int alpha = mMaxAlpha;
            if (textY < mShadeHeight) {

                alpha = mMaxAlpha - (int) ((mShadeHeight - textY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);

            } else if (textY > getHeight() - mShadeHeight) {
                alpha = mMaxAlpha - (int) ((textY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            }
            alpha = Math.max(alpha, 0);
            mPaint.setAlpha(alpha);

            float textWidth = getTextWidth(mPaint, text);
            float textX = (getWidth() - textWidth) * 0.5f;
            canvas.drawText(text, textX, textY, mPaint);
        }

        //画当前行歌词
        drawDGLineLrc(canvas, mShadeHeight, true);

        // 画当前歌词之后的歌词
        for (int i = mLyricsLineNum + 1; i < mLyricsLineTreeMap.size(); i++) {
            String text = mLyricsLineTreeMap.get(i).getLineLyrics();
            float textY = mCentreY
                    + (i - mLyricsLineNum) * getLineHeight(mPaint);
            //超出上视图
            if (textY < getLineHeight(mPaint)) {
                continue;
            }
            //超出下视图
            if (textY + mSpaceLineHeight > getHeight()) {
                break;
            }

            //计算颜色透明度
            int alpha = mMaxAlpha;
            if (textY < mShadeHeight) {

                alpha = mMaxAlpha - (int) ((mShadeHeight - textY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);


            } else if (textY > getHeight() - mShadeHeight) {
                alpha = mMaxAlpha - (int) ((textY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
                // logger.e("alpha=" + alpha);
            }
            alpha = Math.max(alpha, 0);
            mPaint.setAlpha(alpha);

            float textWidth = getTextWidth(mPaint, text);
            float textX = (getWidth() - textWidth) * 0.5f;
            canvas.drawText(text, textX, textY, mPaint);
        }
    }

    /**
     * 绘画动感歌词
     *
     * @param canvas
     * @param shadeHeight
     */
    private void drawDGLineLrc(Canvas canvas, int shadeHeight, boolean isAlphaShade) {

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
            mLineLyricsHLWidth = curLyricsWidth;
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
            mLineLyricsHLWidth = lyricsBeforeWordWidth + len;
        }
        float curTextX = (getWidth() - curLyricsWidth) * 0.5f;


        //计算颜色透明度
        int alpha = mMaxAlpha;

        //颜色透明度过渡
        if (isAlphaShade) {
            if (mCentreY < shadeHeight) {
                alpha = mMaxAlpha - (int) ((shadeHeight - mCentreY) * (mMaxAlpha - mMinAlpha) / shadeHeight);
            } else if (mCentreY > getHeight() - shadeHeight) {
                alpha = mMaxAlpha - (int) ((mCentreY - (getHeight() - shadeHeight)) * (mMaxAlpha - mMinAlpha) / shadeHeight);
            }
        }

        alpha = Math.max(alpha, 0);
        mPaint.setAlpha(alpha);
        mPaintHL.setAlpha(alpha);

        // save和restore是为了剪切操作不影响画布的其它元素
        canvas.save();

        // 画当前歌词
        canvas.drawText(curLyrics, curTextX, mCentreY, mPaint);

        // 设置过渡的颜色和进度
        canvas.clipRect(curTextX, mCentreY - curLyricsHeight - mAdjustLrcHeightNum, curTextX + mLineLyricsHLWidth,
                mCentreY + curLyricsHeight + mAdjustLrcHeightNum);

        // 画当前歌词
        canvas.drawText(curLyrics, curTextX, mCentreY, mPaintHL);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLyricsLineTreeMap == null || mLyricsLineTreeMap.size() == 0 || !isManyLineLrc) {
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
                        if (finalY < -getLineHeight(mPaint)) {
                            dy = dy / 2;
                            isOverScroll = true;
                        } else if (finalY > (mLyricsLineTreeMap.size() + 1) * getLineHeight(mPaint)) {
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
                        int lrcSumHeight = mLyricsLineTreeMap.size() * getLineHeight(mPaint);
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
                        }
                        //发送还原
                        handler.sendEmptyMessageDelayed(RESETLRCVIEW, mResetDuration);


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
            logger.e(e.getMessage());
        }
        //
        if (mTouchIntercept) {
            return mTouchIntercept;
        }
        return isTouchMove;
    }

    /**
     * 还原歌词视图
     */
    private void resetLrcView() {

        if (mOffsetY < 0) {

            int deltaY = 0 * getLineHeight(mPaint) - mScroller.getFinalY();
            mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
            invalidateView();
        } else if (mOffsetY > (mLyricsLineTreeMap.size() + 1) * getLineHeight(mPaint)) {

            int deltaY = (mLyricsLineTreeMap.size() - 1) * getLineHeight(mPaint) - mScroller.getFinalY();
            mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
            invalidateView();

        }
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
        int scrollLrcLineNum = (int) (offsetY / getLineHeight(mPaint) + 0.5f);
        if (scrollLrcLineNum >= mLyricsLineTreeMap.size()) {
            scrollLrcLineNum = mLyricsLineTreeMap.size() - 1;
        } else if (scrollLrcLineNum < 0) {
            scrollLrcLineNum = 0;
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
    public void setLyricsUtil(LyricsUtil mLyricsUtil, int textMaxWidth) {
        this.mLyricsUtil = mLyricsUtil;
        this.mTextMaxWidth = textMaxWidth;
        if (mLyricsUtil != null && textMaxWidth != 0) {
            mLyricsLineTreeMap = mLyricsUtil.getReconstructLyrics(textMaxWidth, mPaint);
        } else {
            mLyricsLineTreeMap = null;
        }
        resetData();
        invalidateView();
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
        mLineLyricsHLWidth = 0;
        mScroller.setFinalY(0);
        mOffsetY = 0;
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
     * 获取每行高度
     *
     * @param paint
     * @return
     */
    public int getLineHeight(Paint paint) {
        return getTextHeight(paint) + mSpaceLineHeight;
    }

    /**
     * 获取行歌词高度
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

    public void setOnLrcClickListener(OnLrcClickListener mOnLrcClickListener) {
        this.mOnLrcClickListener = mOnLrcClickListener;
    }

    /**
     * 更新歌词
     */
    public void updateView(int playProgress) {
        if (mLyricsUtil == null || isReconstruct) return;
        //
        int newLyricsLineNum = mLyricsUtil.getLineNumber(mLyricsLineTreeMap, playProgress);
        if (newLyricsLineNum != mLyricsLineNum) {

            if (!isTouchMove) {
                //
                int deltaY = newLyricsLineNum * getLineHeight(mPaint) - mScroller.getFinalY();
                mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);

            }

            mLyricsLineNum = newLyricsLineNum;
        }
        mLyricsWordIndex = mLyricsUtil.getDisWordsIndex(mLyricsLineTreeMap, mLyricsLineNum, playProgress);
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
        mLyricsLineTreeMap = mLyricsUtil.getReconstructLyrics(mTextMaxWidth, mPaint);
        int newLyricsLineNum = mLyricsUtil.getLineNumber(mLyricsLineTreeMap, curPlayingTime);
        if (newLyricsLineNum != mLyricsLineNum) {
            mLyricsLineNum = newLyricsLineNum;
        }
        mOffsetY = mLyricsLineNum * getLineHeight(mPaint);
        mScroller.setFinalY((int) mOffsetY);
        isReconstruct = false;
        invalidateView();
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


    /***
     *
     * 直接拦截touch操作
     */
    public void setTouchInterceptTrue() {
        mTouchIntercept = true;
    }

    public void setManyLineLrc(boolean manyLineLrc) {
        isManyLineLrc = manyLineLrc;
        invalidateView();
    }

    public boolean isManyLineLrc() {
        return isManyLineLrc;
    }
}

