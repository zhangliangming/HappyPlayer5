package base.lyrics.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.List;

import base.lyrics.LyricsReader;
import base.lyrics.model.LyricsInfo;
import base.lyrics.model.LyricsLineInfo;
import base.lyrics.utils.LyricsUtils;

/**
 * 双行歌词，支持翻译（该歌词在这里只以动感歌词的形式显示）和音译歌词（注：不支持lrc歌词的显示）
 * Created by zhangliangming on 2018-02-24.
 */

public class FloatLyricsView extends AbstractLrcView {


    public FloatLyricsView(Context context) {
        super(context);
    }

    public FloatLyricsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void viewInit(Context context) {

    }

    @Override
    protected void viewLoadFinish() {
        //设置歌词的最大宽度
        mTextMaxWidth = getWidth() / 3 * 2;
        //字体大小
        mFontSize = getHeight() / 4;
        mSpaceLineHeight = mFontSize / 2;

        //设置额外歌词字体大小和空行高度
        mExtraLrcFontSize = mFontSize;
        mExtraLrcSpaceLineHeight = mSpaceLineHeight;

        //设置额外歌词字体大小和空行高度
        setFontSize(mFontSize, false, false);
        setExtraLrcFontSize(mExtraLrcFontSize, false, false);
    }

    @Override
    protected void onViewDrawLrc(Canvas canvas) {

        //绘画歌词
        if (mExtraLrcStatus == EXTRALRCSTATUS_NOSHOWEXTRALRC) {
            //只显示默认歌词
            drawDynamicLyrics(canvas);
        } else {
            //显示翻译歌词 OR 音译歌词
            drawDynamiAndExtraLyrics(canvas);
        }

    }


    /**
     * 绘画歌词
     *
     * @param canvas
     */
    private void drawDynamicLyrics(Canvas canvas) {
        // 先设置当前歌词，之后再根据索引判断是否放在左边还是右边
        List<LyricsLineInfo> splitLyricsLineInfos = mLrcLineInfos.get(mLyricsLineNum).getSplitLyricsLineInfos();
        LyricsLineInfo lyricsLineInfo = splitLyricsLineInfos.get(mSplitLyricsLineNum);
        //获取行歌词高亮宽度
        mLineLyricsHLWidth = getLineLyricsHLWidth(mLyricsReader.getLyricsType(), mPaint, lyricsLineInfo, mSplitLyricsWordIndex, mLyricsWordHLTime);
        // 当行歌词
        String curLyrics = lyricsLineInfo.getLineLyrics();
        float curLrcTextWidth = getTextWidth(mPaint, curLyrics);
        // 当前歌词行的x坐标
        float textX = 0;
        // 当前歌词行的y坐标
        float textY = 0;
        int splitLyricsRealLineNum = getSplitLyricsRealLineNum(mLyricsLineNum, mSplitLyricsLineNum);
        float topPadding = (getHeight() - mSpaceLineHeight - 2 * getTextHeight(mPaint)) / 2;
        if (splitLyricsRealLineNum % 2 == 0) {

            textX = mPaddingLeftOrRight;
            textY = topPadding + getTextHeight(mPaint);
            float nextLrcTextY = textY + mSpaceLineHeight + getTextHeight(mPaint);

            // 画下一句的歌词，该下一句还在该行的分割集合里面
            if (mSplitLyricsLineNum + 1 < splitLyricsLineInfos.size()) {
                String lrcRightText = splitLyricsLineInfos.get(
                        mSplitLyricsLineNum + 1).getLineLyrics();
                float lrcRightTextWidth = mPaint
                        .measureText(lrcRightText);
                float textRightX = getWidth() - lrcRightTextWidth - mPaddingLeftOrRight;

                drawOutline(canvas, mPaintOutline, lrcRightText, textRightX, nextLrcTextY);

                drawText(canvas, mPaint, lrcRightText, textRightX,
                        nextLrcTextY);

            } else if (mLyricsLineNum + 1 < mLrcLineInfos.size()) {
                // 画下一句的歌词，该下一句不在该行分割歌词里面，需要从原始下一行的歌词里面找
                List<LyricsLineInfo> nextSplitLyricsLineInfos = mLrcLineInfos.get(mLyricsLineNum + 1).getSplitLyricsLineInfos();
                String lrcRightText = nextSplitLyricsLineInfos.get(0).getLineLyrics();
                float lrcRightTextWidth = mPaint
                        .measureText(lrcRightText);
                float textRightX = getWidth() - lrcRightTextWidth - mPaddingLeftOrRight;

                drawOutline(canvas, mPaintOutline, lrcRightText, textRightX,
                        nextLrcTextY);

                drawText(canvas, mPaint, lrcRightText, textRightX, nextLrcTextY);
            }

        } else {

            textX = getWidth() - curLrcTextWidth - mPaddingLeftOrRight;
            float preLrcTextY = topPadding + getTextHeight(mPaint);
            textY = preLrcTextY + mSpaceLineHeight + getTextHeight(mPaint);

            // 画下一句的歌词，该下一句还在该行的分割集合里面
            if (mSplitLyricsLineNum + 1 < splitLyricsLineInfos.size()) {
                String lrcLeftText = splitLyricsLineInfos.get(
                        mSplitLyricsLineNum + 1).getLineLyrics();

                drawOutline(canvas, mPaintOutline, lrcLeftText, mPaddingLeftOrRight,
                        preLrcTextY);
                drawText(canvas, mPaint, lrcLeftText, mPaddingLeftOrRight,
                        preLrcTextY);

            } else if (mLyricsLineNum + 1 < mLrcLineInfos.size()) {
                // 画下一句的歌词，该下一句不在该行分割歌词里面，需要从原始下一行的歌词里面找
                List<LyricsLineInfo> nextSplitLyricsLineInfos = mLrcLineInfos.get(mLyricsLineNum + 1).getSplitLyricsLineInfos();
                String lrcLeftText = nextSplitLyricsLineInfos.get(0).getLineLyrics();
                drawOutline(canvas, mPaintOutline, lrcLeftText, mPaddingLeftOrRight,
                        preLrcTextY);
                drawText(canvas, mPaintHL, lrcLeftText, mPaddingLeftOrRight,
                        preLrcTextY);
            }
        }
        //画歌词
        drawOutline(canvas, mPaintOutline, curLyrics, textX, textY);
        drawDynamicText(canvas, mPaint, mPaintHL, curLyrics, mLineLyricsHLWidth, textX, textY);
    }

    /**
     * 获取分隔后的歌词的真正行号
     *
     * @param lyricsLineNum
     * @param splitLyricsLineNum
     * @return
     */
    private int getSplitLyricsRealLineNum(int lyricsLineNum, int splitLyricsLineNum) {
        int realLineNum = 0;
        for (int i = 0; i < mLrcLineInfos.size(); i++) {
            if (i != lyricsLineNum) {
                realLineNum += mLrcLineInfos.get(i).getSplitLyricsLineInfos().size();
            } else if (i == lyricsLineNum) {
                realLineNum += splitLyricsLineNum;
                break;
            }
        }
        return realLineNum;
    }

    /**
     * 绘画歌词和额外歌词
     *
     * @param canvas
     */
    private void drawDynamiAndExtraLyrics(Canvas canvas) {

        float topPadding = (getHeight() - mExtraLrcSpaceLineHeight - getTextHeight(mPaint) - getTextHeight(mExtraLrcPaint)) / 2;
        // 当前歌词行的y坐标
        float lrcTextY = topPadding + getTextHeight(mPaint);
        //额外歌词行的y坐标
        float extraLrcTextY = lrcTextY + mExtraLrcSpaceLineHeight + getTextHeight(mExtraLrcPaint);

        LyricsLineInfo lyricsLineInfo = mLrcLineInfos.get(mLyricsLineNum);
        //画默认歌词
        drawDynamiLyrics(canvas, mPaint, mPaintHL, mPaintOutline, lyricsLineInfo, mLineLyricsHLWidth, mLyricsWordIndex, mLyricsWordHLTime, mHighLightLrcMoveX, lrcTextY);

        //显示翻译歌词
        if (mLyricsReader.getLyricsType() == LyricsInfo.DYNAMIC && mExtraLrcStatus == EXTRALRCSTATUS_SHOWTRANSLATELRC && mTranslateDrawType == TRANSLATE_DRAW_TYPE_DYNAMIC) {

            LyricsLineInfo translateLyricsLineInfo = mTranslateLrcLineInfos.get(mLyricsLineNum);
            //画翻译歌词
            drawDynamiLyrics(canvas, mExtraLrcPaint, mExtraLrcPaintHL, mExtraLrcPaintOutline, translateLyricsLineInfo, mExtraLyricsLineHLWidth, mExtraLyricsWordIndex, mTranslateLyricsWordHLTime, mExtraLyricsHighLightMoveX, extraLrcTextY);

        } else {
            LyricsLineInfo transliterationLineInfo = mTransliterationLrcLineInfos.get(mLyricsLineNum);
            //画音译歌词
            drawDynamiLyrics(canvas, mExtraLrcPaint, mExtraLrcPaintHL, mExtraLrcPaintOutline, transliterationLineInfo, mExtraLyricsLineHLWidth, mExtraLyricsWordIndex, mLyricsWordHLTime, mExtraLyricsHighLightMoveX, extraLrcTextY);

        }

    }

    /**
     * 画动感歌词
     *
     * @param canvas
     * @param paint
     * @param paintHL
     * @param paintOutline
     * @param lyricsLineInfo    歌词行数据
     * @param lyricsLineHLWidth 高亮宽度
     * @param lyricsWordIndex   歌词字索引
     * @param lyricsWordHLTime  歌词高亮时长
     * @param highLightMoveX    歌词移动x位置
     * @param textY             歌词y位置
     */
    private void drawDynamiLyrics(Canvas canvas, Paint paint, Paint paintHL, Paint paintOutline, LyricsLineInfo lyricsLineInfo, float lyricsLineHLWidth, int lyricsWordIndex, float lyricsWordHLTime, float highLightMoveX, float textY) {
        //获取行歌词高亮宽度
        lyricsLineHLWidth = getLineLyricsHLWidth(mLyricsReader.getLyricsType(), paint, lyricsLineInfo, lyricsWordIndex, lyricsWordHLTime);
        // 当行歌词
        String curLyrics = lyricsLineInfo.getLineLyrics();
        float curLrcTextWidth = getTextWidth(paint, curLyrics);
        // 当前歌词行的x坐标
        float textX = getHLMoveTextX(curLrcTextWidth, lyricsLineHLWidth, highLightMoveX);
        drawOutline(canvas, paintOutline, curLyrics, textX, textY);
        drawDynamicText(canvas, paint, paintHL, curLyrics, lyricsLineHLWidth, textX, textY);
    }

    @Override
    protected boolean onViewTouchEvent(MotionEvent event) {
        return true;
    }


    @Override
    protected void updateView(int playProgress) {
        //不在转换中，则进行歌词的绘画
        mLyricsLineNum = LyricsUtils.getLineNumber(mLyricsReader.getLyricsType(), mLrcLineInfos, playProgress, mLyricsReader.getPlayOffset());
        updateSplitData(playProgress);
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
        }else {
            setLrcStatus(LRCSTATUS_NONSUPPORT);
        }
    }
}
