package base.lyrics.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import java.util.List;

import base.lyrics.LyricsReader;
import base.lyrics.model.LyricsInfo;
import base.lyrics.model.LyricsLineInfo;
import base.lyrics.utils.ColorUtils;
import base.lyrics.utils.LyricsUtils;
import base.lyrics.utils.TimeUtils;

/**
 * 多行歌词，支持翻译和音译歌词
 * Created by zhangliangming on 2018-02-24.
 */

public class ManyLyricsView extends AbstractLrcView {
    /**
     * 初始
     */
    private final int TOUCHEVENTSTATUS_INIT = 0;

    /**
     * 滑动越界
     */
    private final int TOUCHEVENTSTATUS_OVERSCROLL = 1;
    /**
     * 快速滑动
     */
    private final int TOUCHEVENTSTATUS_FLINGSCROLL = 2;

    /**
     * 触摸状态
     */
    private int mTouchEventStatus = TOUCHEVENTSTATUS_INIT;

    /////////////////////////////////////////////////
    /**
     * 画时间线指示器
     ***/
    private Paint mPaintIndicator;
    /**
     * 画线
     */
    private Paint mPaintLine;
    /**
     * 绘画播放按钮
     */
    private Paint mPaintPlay;
    /**
     * 播放按钮区域
     */
    private Rect mPlayBtnRect;

    /**
     * 是否在播放按钮区域
     */
    private boolean isInPlayBtnRect = false;
    /**
     * 播放按钮区域字体大小
     */
    private int mPlayRectSize = 25;
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
    private int mDuration = 350;

    ///////////////////////////////////////////////////
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

    //////////////////////////////////////////////////////
    /**
     * 是否修改scroller的y
     */
    private boolean isChangeScrollerFinalY = false;

    /**
     * 还原歌词视图
     */
    private int RESETLRCVIEW = 1;
    /**
     *
     */
    private int mResetDuration = 2500;

    /**
     * Handler处理滑动指示器隐藏和歌词滚动到当前播放的位置
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
                    if (mScroller.computeScrollOffset()) {
                        //发送还原
                        mHandler.sendEmptyMessageDelayed(RESETLRCVIEW, mResetDuration);
                    } else {
                        //
                        mTouchIntercept = false;
                        mTouchEventStatus = TOUCHEVENTSTATUS_INIT;
                        int deltaY = getLineAtHeightY(mLyricsLineNum) - mScroller.getFinalY();
                        mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
                        invalidateView();
                    }

                    break;
            }
        }
    };
    /**
     * 歌词快进事件
     */
    private OnLrcClickListener mOnLrcClickListener;

    public ManyLyricsView(Context context) {
        super(context);
    }

    public ManyLyricsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void viewInit(Context context) {
        //
        mScroller = new Scroller(context, new LinearInterpolator());
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        final ViewConfiguration configuration = ViewConfiguration
                .get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        //画指时间示器
        mPaintIndicator = new Paint();
        mPaintIndicator.setDither(true);
        mPaintIndicator.setAntiAlias(true);

        //画线
        mPaintLine = new Paint();
        mPaintLine.setDither(true);
        mPaintLine.setAntiAlias(true);
        mPaintLine.setStyle(Paint.Style.FILL);


        //绘画播放按钮
        mPaintPlay = new Paint();
        mPaintPlay.setDither(true);
        mPaintPlay.setAntiAlias(true);
        mPaintPlay.setStrokeWidth(2);


        setGotoSearchTextColor(Color.WHITE, false);
        setGotoSearchTextPressedColor(ColorUtils.parserColor("#0288d1"), false);
    }

    @Override
    protected void viewLoadFinish() {

        //设置画笔大小
        mPaintIndicator.setTextSize(mPlayRectSize);
        mPaintLine.setTextSize(mPlayRectSize);
        mPaintPlay.setTextSize(mPlayRectSize);

        mShadeHeight = getHeight() / 4;
        //设置歌词的最大宽度
        mTextMaxWidth = getWidth() / 3 * 2;
    }

    @Override
    protected void onViewDrawLrc(Canvas canvas) {
        //获取中间位置
        mCentreY = (getHeight() + getTextHeight(mPaintHL)) * 0.5f + getLineAtHeightY(mLyricsLineNum) - mOffsetY;


        //画当前行歌词
        //获取分割后的歌词列表
        LyricsLineInfo lyricsLineInfo = mLrcLineInfos
                .get(mLyricsLineNum);
        List<LyricsLineInfo> splitLyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
        float lineBottomY = drawDownLyrics(canvas, mPaint, mPaintHL, splitLyricsLineInfos, mSplitLyricsLineNum, mSplitLyricsWordIndex, mSpaceLineHeight, mLyricsWordHLTime, mCentreY);
        //画额外歌词
        lineBottomY = drawDownExtraLyrics(canvas, mExtraLrcPaint, mExtraLrcPaintHL, mLyricsLineNum, mExtraSplitLyricsLineNum, mExtraSplitLyricsWordIndex, mExtraLrcSpaceLineHeight, mLyricsWordHLTime, mTranslateLyricsWordHLTime, lineBottomY);


        //画当前行正面的歌词
        for (int i = mLyricsLineNum + 1; i < mLrcLineInfos.size(); i++) {
            LyricsLineInfo downLyricsLineInfo = mLrcLineInfos
                    .get(i);
            //获取分割后的歌词列表
            List<LyricsLineInfo> lyricsLineInfos = downLyricsLineInfo.getSplitLyricsLineInfos();
            lineBottomY = drawDownLyrics(canvas, mPaint, mPaintHL, lyricsLineInfos, -1, -2, mSpaceLineHeight, -1, lineBottomY);
            //画额外歌词
            lineBottomY = drawDownExtraLyrics(canvas, mExtraLrcPaint, mExtraLrcPaintHL, i, -1, -2, mExtraLrcSpaceLineHeight, -1, -1, lineBottomY);
        }


        // 画当前歌词之前的歌词
        float lineTopY = mCentreY;
        for (int i = mLyricsLineNum - 1; i >= 0; i--) {
            LyricsLineInfo upLyricsLineInfo = mLrcLineInfos
                    .get(i);
            //获取分割后的歌词列表
            List<LyricsLineInfo> lyricsLineInfos = upLyricsLineInfo.getSplitLyricsLineInfos();
            lineTopY = drawUpExtraLyrics(canvas, mExtraLrcPaint, lyricsLineInfos, i, mExtraLrcSpaceLineHeight, lineTopY);
        }

        //绘画时间、播放按钮等
        if (mTouchIntercept || mTouchEventStatus != TOUCHEVENTSTATUS_INIT) {
            drawIndicator(canvas);
        }

    }

    /**
     * 绘画时间、播放按钮等
     *
     * @param canvas
     */
    private void drawIndicator(Canvas canvas) {
        //画当前时间
        int scrollLrcLineNum = getScrollLrcLineNum(mOffsetY);
        int startTime = mLrcLineInfos.get(scrollLrcLineNum).getStartTime();
        String timeString = TimeUtils.parseMMSSString(startTime);
        int textHeight = getTextHeight(mPaintIndicator);
        float textWidth = getTextWidth(mPaintIndicator, timeString);
        int padding = 10;
        float textX = padding;
        float textY = (getHeight() + textHeight) / 2;
        canvas.drawText(timeString, textX, textY, mPaintIndicator);

        mPaintPlay.setStyle(Paint.Style.STROKE);
        //圆形矩形
        if (mPlayBtnRect == null)
            mPlayBtnRect = new Rect();
        //圆半径
        int circleR = mPlayRectSize;
        int linePadding = padding * 2;
        int rectR = getWidth() - linePadding;
        int rectL = rectR - circleR * 2;
        int rectT = getHeight() / 2;
        int rectB = rectT + circleR * 2;
        mPlayBtnRect.set(rectL - padding, rectT - padding, rectR + padding, rectB + padding);

        //画圆
        int cx = rectL + (rectR - rectL) / 2;
        int cy = rectT;
        canvas.drawCircle(cx, cy, circleR, mPaintPlay);

        //画三角形
        Path trianglePath = new Path();
        float startX = cx + circleR / 2;
        float startY = rectT;
        trianglePath.moveTo(startX, startY);// 此点为多边形的起点
        float pleftX = startX - (float) circleR / 4 * 3;
        float ptopY = startY - circleR * (float) Math.sqrt(3) / 4;
        float pbomY = startY + circleR * (float) Math.sqrt(3) / 4;
        trianglePath.lineTo(pleftX, ptopY);
        trianglePath.lineTo(pleftX, pbomY);
        trianglePath.close();// 使这些点构成封闭的多边形
        if (isInPlayBtnRect) {
            mPaintPlay.setStyle(Paint.Style.FILL);
        } else {
            mPaintPlay.setStyle(Paint.Style.STROKE);
        }
        canvas.drawPath(trianglePath, mPaintPlay);

        //画线
        int lineH = 2;
        float lineY = (getHeight() - lineH) / 2;
        float lineLeft = textX + textWidth + linePadding;
        float lineR = rectL - linePadding;
        LinearGradient linearGradientHL = new LinearGradient(lineLeft, lineY + lineH, lineR, lineY + lineH, new int[]{ColorUtils.parserColor(Color.WHITE, 200), ColorUtils.parserColor(Color.WHITE, 10), ColorUtils.parserColor(Color.WHITE, 200)}, new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
        mPaintLine.setShader(linearGradientHL);
        canvas.drawRect(lineLeft, lineY, lineR, lineY + lineH, mPaintLine);

    }

    /**
     * 向下绘画动感歌词
     *
     * @param canvas
     * @param paint
     * @param paintHL
     * @param splitLyricsLineInfos 分隔歌词集合
     * @param splitLyricsLineNum   分隔歌词行索引
     * @param splitLyricsWordIndex 分隔歌词字索引
     * @param spaceLineHeight      空行高度
     * @param lyricsWordHLTime     歌词高亮时间
     * @param fristLineTextY       第一行文字位置
     * @return
     */
    private float drawDownLyrics(Canvas canvas, Paint paint, Paint paintHL, List<LyricsLineInfo> splitLyricsLineInfos, int splitLyricsLineNum, int splitLyricsWordIndex, int spaceLineHeight, float lyricsWordHLTime, float fristLineTextY) {

        float lineBottomY = 0;

        int curLyricsLineNum = splitLyricsLineNum;
        if (splitLyricsWordIndex == -1) {
            //设置为最后索引，防止跳转到下一句时，前面歌词不是高亮的问题
            curLyricsLineNum = splitLyricsLineInfos.size() - 1;
        }

        //歌词和空行高度
        int lineHeight = getTextHeight(paint) + spaceLineHeight;
        //往下绘画歌词
        for (int i = 0; i < splitLyricsLineInfos.size(); i++) {

            String text = splitLyricsLineInfos.get(i).getLineLyrics();

            lineBottomY = fristLineTextY + i * lineHeight;

            //超出上视图
            if (lineBottomY < lineHeight) {
                continue;
            }
            //超出下视图
            if (lineBottomY + spaceLineHeight > getHeight()) {
                break;
            }

            //计算颜色透明度
            int alpha = mMaxAlpha;

            //颜色透明度过渡

            if (lineBottomY < mShadeHeight) {
                alpha = mMaxAlpha - (int) ((mShadeHeight - lineBottomY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            } else if (lineBottomY > getHeight() - mShadeHeight) {
                alpha = mMaxAlpha - (int) ((lineBottomY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            }

            alpha = Math.max(alpha, 0);
            paint.setAlpha(alpha);
            paintHL.setAlpha(alpha);

            float textWidth = getTextWidth(paint, text);
            float textX = (getWidth() - textWidth) * 0.5f;
            //
            if (i < curLyricsLineNum) {
                drawText(canvas, paint, text, textX, lineBottomY);
                drawHLText(canvas, paintHL, text, textX, lineBottomY);

            } else if (i == curLyricsLineNum) {
                //绘画动感歌词
                float lineLyricsHLWidth = getLineLyricsHLWidth(mLyricsReader.getLyricsType(), paint, splitLyricsLineInfos.get(i), splitLyricsWordIndex, lyricsWordHLTime);
                drawDynamicText(canvas, paint, paintHL, text, lineLyricsHLWidth, textX, lineBottomY);

            } else if (i > curLyricsLineNum) {
                drawText(canvas, paint, text, textX, lineBottomY);
            }

//            canvas.drawLine(0, lineBottomY - getTextHeight(paint), 720, lineBottomY - getTextHeight(paint), paint);
//            canvas.drawLine(0, lineBottomY, 720, lineBottomY, paint);
        }
        //考虑部分歌词越界，导致高度不正确，这里重新获取基本歌词结束后的y轴位置
        lineBottomY = fristLineTextY + lineHeight * (splitLyricsLineInfos.size());

        return lineBottomY;
    }

    /**
     * 绘画向下的额外歌词
     *
     * @param canvas
     * @param paint
     * @param paintHL
     * @param lyricsLineNum
     * @param extraSplitLyricsLineNum
     * @param extraSplitLyricsWordIndex
     * @param extraLrcSpaceLineHeight
     * @param lyricsWordHLTime
     * @param translateLyricsWordHLTime
     * @param lineBottomY
     * @return
     */
    private float drawDownExtraLyrics(Canvas canvas, Paint paint, Paint paintHL, int lyricsLineNum, int extraSplitLyricsLineNum, int extraSplitLyricsWordIndex, int extraLrcSpaceLineHeight, float lyricsWordHLTime, float translateLyricsWordHLTime, float lineBottomY) {
        if (mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLATELRC) {
            //画翻译歌词
            if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {
                //以动感歌词的形式显示翻译歌词
                List<LyricsLineInfo> translateSplitLyricsLineInfos = mTranslateLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineBottomY += mExtraLrcSpaceLineHeight - mSpaceLineHeight;
                if (mLyricsReader.getLyricsType() == LyricsInfo.DYNAMIC && mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLATELRC && mTranslateDrawType == TRANSLATE_DRAW_TYPE_DYNAMIC) {
                    lineBottomY = drawDownLyrics(canvas, paint, paintHL, translateSplitLyricsLineInfos, extraSplitLyricsLineNum, extraSplitLyricsWordIndex, extraLrcSpaceLineHeight, translateLyricsWordHLTime, lineBottomY);
                } else {
                    //画lrc歌词
                    lineBottomY = drawDownLyrics(canvas, paint, paintHL, translateSplitLyricsLineInfos, -1, -2, extraLrcSpaceLineHeight, -1, lineBottomY);
                }
                lineBottomY += mSpaceLineHeight - mExtraLrcSpaceLineHeight;
            }
        } else if (mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
            //画音译歌词
            if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
                //获取分割后的音译歌词行
                List<LyricsLineInfo> transliterationSplitLrcLineInfos = mTransliterationLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineBottomY += mExtraLrcSpaceLineHeight - mSpaceLineHeight;
                lineBottomY = drawDownLyrics(canvas, paint, paintHL, transliterationSplitLrcLineInfos, extraSplitLyricsLineNum, extraSplitLyricsWordIndex, extraLrcSpaceLineHeight, lyricsWordHLTime, lineBottomY);
                lineBottomY += mSpaceLineHeight - mExtraLrcSpaceLineHeight;
            }
        }
        return lineBottomY;
    }

    /**
     * 绘画向上的额外歌词
     *
     * @param canvas
     * @param paint
     * @param splitLyricsLineInfos
     * @param lyricsLineNum
     * @param extraLrcSpaceLineHeight
     * @param lineTopY                @return
     */
    private float drawUpExtraLyrics(Canvas canvas, Paint paint, List<LyricsLineInfo> splitLyricsLineInfos, int lyricsLineNum, int extraLrcSpaceLineHeight, float lineTopY) {
        if (mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLATELRC) {
            //画翻译歌词
            if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {
                //以动感歌词的形式显示翻译歌词
                List<LyricsLineInfo> translateSplitLyricsLineInfos = mTranslateLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineTopY -= (getTextHeight(mPaint) + mSpaceLineHeight);
                lineTopY = drawUpLyrics(canvas, paint, translateSplitLyricsLineInfos, extraLrcSpaceLineHeight, lineTopY);
                lineTopY -= (getTextHeight(paint) + mExtraLrcSpaceLineHeight);

                //
                lineTopY = drawUpLyrics(canvas, mPaint, splitLyricsLineInfos, mSpaceLineHeight, lineTopY);
            }
        } else if (mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
            //画音译歌词
            if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
                //获取分割后的音译歌词行
                List<LyricsLineInfo> transliterationSplitLrcLineInfos = mTransliterationLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineTopY -= (getTextHeight(mPaint) + mSpaceLineHeight);
                lineTopY = drawUpLyrics(canvas, paint, transliterationSplitLrcLineInfos, extraLrcSpaceLineHeight, lineTopY);
                lineTopY -= (getTextHeight(paint) + mExtraLrcSpaceLineHeight);

                //
                lineTopY = drawUpLyrics(canvas, mPaint, splitLyricsLineInfos, mSpaceLineHeight, lineTopY);
            }
        } else {
            lineTopY -= (getTextHeight(mPaint) + mSpaceLineHeight);
            lineTopY = drawUpLyrics(canvas, mPaint, splitLyricsLineInfos, mSpaceLineHeight, lineTopY);
        }
        return lineTopY;
    }

    /**
     * 向上绘画歌词
     *
     * @param canvas
     * @param paint
     * @param splitLyricsLineInfos 分隔歌词集合
     * @param spaceLineHeight      空行高度
     * @param fristLineTextY       第一行文字位置
     * @return
     */
    private float drawUpLyrics(Canvas canvas, Paint paint, List<LyricsLineInfo> splitLyricsLineInfos, int spaceLineHeight, float fristLineTextY) {

        float lineTopY = fristLineTextY;
        //歌词和空行高度
        int lineHeight = getTextHeight(paint) + spaceLineHeight;
        for (int i = splitLyricsLineInfos.size() - 1; i >= 0; i--) {
            if (i != splitLyricsLineInfos.size() - 1) {
                lineTopY -= lineHeight;
            }

            //超出上视图
            if (lineTopY < lineHeight) {
                continue;
            }
            //超出下视图
            if (lineTopY + spaceLineHeight > getHeight()) {
                break;
            }

            String text = splitLyricsLineInfos.get(i).getLineLyrics();
            //计算颜色透明度
            int alpha = mMaxAlpha;

            //颜色透明度过渡

            if (lineTopY < mShadeHeight) {
                alpha = mMaxAlpha - (int) ((mShadeHeight - lineTopY) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            } else if (lineTopY > getHeight() - mShadeHeight) {
                alpha = mMaxAlpha - (int) ((lineTopY - (getHeight() - mShadeHeight)) * (mMaxAlpha - mMinAlpha) / mShadeHeight);
            }

            alpha = Math.max(alpha, 0);
            paint.setAlpha(alpha);

            float textWidth = getTextWidth(paint, text);
            float textX = (getWidth() - textWidth) * 0.5f;

            canvas.drawText(text, textX, lineTopY, paint);

//            canvas.drawLine(0, lineTopY - getTextHeight(paint), 720, lineTopY - getTextHeight(paint), paint);
//            canvas.drawLine(0, lineTopY, 720, lineTopY, paint);

        }

        //考虑部分歌词越界，导致高度不正确，这里重新获取基本歌词结束后的y轴位置
        lineTopY = fristLineTextY - lineHeight * (splitLyricsLineInfos.size() - 1);
        return lineTopY;
    }

    /**
     * 获取所在歌词行的高度
     *
     * @param lyricsLineNum
     * @return
     */
    private int getLineAtHeightY(int lyricsLineNum) {
        int lineAtHeightY = 0;

        for (int i = 0; i < lyricsLineNum; i++) {
            LyricsLineInfo lyricsLineInfo = mLrcLineInfos
                    .get(i);
            //获取分割后的歌词列表
            List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
            lineAtHeightY += (getTextHeight(mPaint) + mSpaceLineHeight) * lyricsLineInfos.size();

            //判断是否有翻译歌词或者音译歌词
            if (mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLATELRC) {
                if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> translateLrcLineInfos = mTranslateLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    lineAtHeightY += (getTextHeight(mExtraLrcPaint) + mExtraLrcSpaceLineHeight) * translateLrcLineInfos.size();
                }
            } else if (mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
                if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> transliterationLrcLineInfos = mTransliterationLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    lineAtHeightY += (getTextHeight(mExtraLrcPaint) + mExtraLrcSpaceLineHeight) * transliterationLrcLineInfos.size();
                }
            }
        }
        return lineAtHeightY;
    }

    @Override
    protected boolean onViewTouchEvent(MotionEvent event) {
        if (!mTouchAble || mLrcStatus != LRCSTATUS_LRC)
            return true;
        obtainVelocityTracker(event);
        int actionId = event.getAction();
        switch (actionId) {
            case MotionEvent.ACTION_DOWN:

                mLastY = (int) event.getY();
                mInterceptX = (int) event.getX();
                mInterceptY = (int) event.getY();

                //发送还原
                mHandler.removeMessages(RESETLRCVIEW);


                if (mPlayBtnRect != null && isPlayClick(event)) {
                    isInPlayBtnRect = true;
                    invalidateView();
                }

                break;
            case MotionEvent.ACTION_MOVE:
                int curX = (int) event.getX();
                int curY = (int) event.getY();
                int deltaX = (int) (mInterceptX - curX);
                int deltaY = (int) (mInterceptY - curY);

                if (mTouchIntercept || (Math.abs(deltaY) > mTouchSlop && Math.abs(deltaX) < mTouchSlop)) {
                    mTouchIntercept = true;

                    int dy = mLastY - curY;

                    //创建阻尼效果
                    float finalY = mOffsetY + dy;

                    if (finalY < getTopOverScrollHeightY() || finalY > getBottomOverScrollHeightY()) {
                        dy = dy / 2;
                        mTouchEventStatus = TOUCHEVENTSTATUS_OVERSCROLL;


                    }

                    mScroller.startScroll(0, mScroller.getFinalY(), 0, dy, 0);
                    invalidateView();

                }

                mLastY = curY;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

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
                    int lrcSumHeight = getLineAtHeightY(mLrcLineInfos.size());
                    int minY = -getHeight() / 4;
                    int maxY = lrcSumHeight + getHeight() / 4;
                    mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
                    invalidateView();

                    mTouchEventStatus = TOUCHEVENTSTATUS_FLINGSCROLL;

                    //发送还原
                    mHandler.sendEmptyMessageDelayed(RESETLRCVIEW, mResetDuration);
                } else {

                    if (mTouchEventStatus == TOUCHEVENTSTATUS_OVERSCROLL) {
                        resetLrcView();
                    } else {
                        //发送还原
                        mHandler.sendEmptyMessageDelayed(RESETLRCVIEW, mResetDuration);

                    }
                }
                releaseVelocityTracker();


                //判断是否在滑动和是否点击了播放按钮
                if (isInPlayBtnRect) {

                    mHandler.removeMessages(RESETLRCVIEW);

                    if (mOnLrcClickListener != null) {

                        //获取当前滑动到的歌词播放行
                        int scrollLrcLineNum = getScrollLrcLineNum(mOffsetY);
                        int startTime = mLrcLineInfos.get(scrollLrcLineNum).getStartTime();
                        mOnLrcClickListener.onLrcPlayClicked(startTime);

                    }

                    mTouchEventStatus = TOUCHEVENTSTATUS_INIT;
                    mTouchIntercept = false;

                }
                isInPlayBtnRect = false;
                invalidateView();
                mLastY = 0;
                mInterceptX = 0;
                mInterceptY = 0;

                break;
            default:
        }

        return true;
    }


    /**
     * 判断是否是播放按钮点击
     *
     * @param event
     * @return
     */
    private boolean isPlayClick(MotionEvent event) {
        if (mPlayBtnRect == null) return false;
        int x = (int) event.getX();
        int y = (int) event.getY();
        return mPlayBtnRect.contains(x, y);

    }

    /**
     * 获取底部越界
     *
     * @return
     */
    private float getBottomOverScrollHeightY() {
        if (mLrcLineInfos == null) return 0;
        return getLineAtHeightY(mLrcLineInfos.size());
    }

    /**
     * 获取顶部越界高度
     *
     * @return
     */
    private float getTopOverScrollHeightY() {
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
        } else if (mOffsetY > getBottomOverScrollHeightY()) {

            int deltaY = getLineAtHeightY(mLrcLineInfos.size() - 1) - mScroller.getFinalY();
            mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, mDuration);
            invalidateView();

        }
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
     * 设置歌词读取器
     *
     * @param lyricsReader
     */
    @Override
    public synchronized void setLyricsReader(LyricsReader lyricsReader) {
        super.setLyricsReader(lyricsReader);
        if (lyricsReader != null && lyricsReader.getLyricsType() == LyricsInfo.DYNAMIC) {
            //翻译歌词以动感歌词形式显示
            if (mExtraLrcType == EXTRALRCTYPE_BOTH || mExtraLrcType == EXTRALRCTYPE_TRANSLATELRC) {
                setTranslateDrawType(TRANSLATE_DRAW_TYPE_DYNAMIC);
            }
        }
    }

    @Override
    public void resetData() {
        mScroller.setFinalY(0);
        mOffsetY = 0;
        mCentreY = 0;
        mTouchEventStatus = TOUCHEVENTSTATUS_INIT;
        super.resetData();
    }

    @Override
    public void setPaintColor(int[] colors, boolean isInvalidateView) {
        super.setPaintColor(colors, isInvalidateView);
    }

    @Override
    public void setPaintHLColor(int[] colors, boolean isInvalidateView) {
        mPaintIndicator.setColor(colors[0]);
        mPaintPlay.setColor(colors[0]);
        super.setPaintHLColor(colors, isInvalidateView);
    }

    @Override
    protected void updateView(int playProgress) {
        int newLyricsLineNum = LyricsUtils.getLineNumber(mLyricsReader.getLyricsType(), mLrcLineInfos, playProgress, mLyricsReader.getPlayOffset());
        if (newLyricsLineNum != mLyricsLineNum) {
            if (mTouchEventStatus == TOUCHEVENTSTATUS_INIT && !isChangeScrollerFinalY && !mTouchIntercept) {
                //初始状态
                int duration = mDuration * getLineSizeNum(mLyricsLineNum);
                int deltaY = getLineAtHeightY(newLyricsLineNum) - mScroller.getFinalY();
                mScroller.startScroll(0, mScroller.getFinalY(), 0, deltaY, duration);
            }

            mLyricsLineNum = newLyricsLineNum;
        }
        //
        if (isChangeScrollerFinalY) {

            //字体大小、额外歌词显示或者空行大小改变，则对歌词的位置进行修改
            mOffsetY = getLineAtHeightY(newLyricsLineNum);
            mScroller.setFinalY((int) mOffsetY);
            isChangeScrollerFinalY = false;
        }
        updateSplitData(playProgress);
    }

    /**
     * 判断该行总共有多少行歌词（原始歌词 + 分隔歌词）
     *
     * @param lyricsLineNum
     * @return
     */
    private int getLineSizeNum(int lyricsLineNum) {
        int lineSizeNum = 0;
        LyricsLineInfo lyricsLineInfo = mLrcLineInfos
                .get(lyricsLineNum);
        //获取分割后的歌词列表
        List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
        lineSizeNum += lyricsLineInfos.size();

        //判断是否有翻译歌词或者音译歌词
        if (mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLATELRC) {
            if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {
                List<LyricsLineInfo> translateLrcLineInfos = mTranslateLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineSizeNum += translateLrcLineInfos.size();
            }
        } else if (mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
            if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
                List<LyricsLineInfo> transliterationLrcLineInfos = mTransliterationLrcLineInfos.get(lyricsLineNum).getSplitLyricsLineInfos();
                lineSizeNum += transliterationLrcLineInfos.size();
            }
        }

        return lineSizeNum;
    }

    /**
     * 获取滑动的当前行
     *
     * @return
     */
    private int getScrollLrcLineNum(float offsetY) {
        int scrollLrcLineNum = -1;
        int lineHeight = 0;
        for (int i = 0; i < mLrcLineInfos.size(); i++) {
            LyricsLineInfo lyricsLineInfo = mLrcLineInfos
                    .get(i);
            //获取分割后的歌词列表
            List<LyricsLineInfo> lyricsLineInfos = lyricsLineInfo.getSplitLyricsLineInfos();
            lineHeight += (getTextHeight(mPaint) + mSpaceLineHeight) * lyricsLineInfos.size();

            //判断是否有翻译歌词或者音译歌词
            if (mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLATELRC) {
                if (mTranslateLrcLineInfos != null && mTranslateLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> translateLrcLineInfos = mTranslateLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    lineHeight += (getTextHeight(mExtraLrcPaint) + mExtraLrcSpaceLineHeight) * translateLrcLineInfos.size();
                }
            } else if (mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLITERATIONLRC) {
                if (mTransliterationLrcLineInfos != null && mTransliterationLrcLineInfos.size() > 0) {
                    List<LyricsLineInfo> transliterationLrcLineInfos = mTransliterationLrcLineInfos.get(i).getSplitLyricsLineInfos();
                    lineHeight += (getTextHeight(mExtraLrcPaint) + mExtraLrcSpaceLineHeight) * transliterationLrcLineInfos.size();
                }
            }

            if (lineHeight > offsetY) {
                scrollLrcLineNum = i;
                break;
            }
        }
        if (scrollLrcLineNum == -1) {
            scrollLrcLineNum = mLrcLineInfos.size() - 1;
        }
        return scrollLrcLineNum;
    }

    public void setOnLrcClickListener(OnLrcClickListener mOnLrcClickListener) {
        this.mOnLrcClickListener = mOnLrcClickListener;
    }

    @Override
    public void setFontSize(int fontSize, boolean isReloadLrcData, boolean isInvalidateView) {
        isChangeScrollerFinalY = true;
        super.setFontSize(fontSize, isReloadLrcData, isInvalidateView);
    }

    @Override
    public void setExtraLrcFontSize(int extraLrcFontSize, boolean isReloadLrcData, boolean isInvalidateView) {
        isChangeScrollerFinalY = true;
        super.setExtraLrcFontSize(extraLrcFontSize, isReloadLrcData, isInvalidateView);
    }

    @Override
    public void setFontSize(int fontSize, int extraLrcFontSize, boolean isReloadLrcData, boolean isInvalidateView) {
        isChangeScrollerFinalY = true;
        super.setFontSize(fontSize, extraLrcFontSize, isReloadLrcData, isInvalidateView);
    }

    @Override
    public void setTypeFace(Typeface typeFace, boolean isInvalidateView) {
        super.setTypeFace(typeFace, false);
        setFontSize(getFontSize(), true, isInvalidateView);
    }

    @Override
    public void setSpaceLineHeight(int mSpaceLineHeight, boolean isInvalidateView) {
        super.setSpaceLineHeight(mSpaceLineHeight, false);
        setFontSize(getFontSize(), true, isInvalidateView);
    }

    @Override
    public void setExtraLrcSpaceLineHeight(int mExtraLrcSpaceLineHeight, boolean isInvalidateView) {
        super.setExtraLrcSpaceLineHeight(mExtraLrcSpaceLineHeight, false);
        setFontSize(getFontSize(), true, isInvalidateView);
    }

    @Override
    public void setExtraLrcStatus(int mExtraLrcStatus) {
        isChangeScrollerFinalY = true;
        super.setExtraLrcStatus(mExtraLrcStatus);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        // 更新当前的X轴偏移量
        if (mScroller.computeScrollOffset()) { // 返回true代表正在模拟数据，false 已经停止模拟数据
            mOffsetY = mScroller.getCurrY();

            invalidateView();
        } else {
            if (mTouchEventStatus == TOUCHEVENTSTATUS_FLINGSCROLL) {
                resetLrcView();
            }
        }
    }

    public void setTouchAble(boolean mTouchAble) {
        this.mTouchAble = mTouchAble;
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
        void onLrcPlayClicked(int progress);
    }
}
