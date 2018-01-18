package base.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zlm.hp.R;

import base.utils.ColorUtil;
import base.utils.LoggerUtil;

/**
 * @Description: 自定义进度条
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/16 23:30
 * @Throws:
 */
public class LrcSeekBar extends AppCompatSeekBar {

    private Context mContext;
    private Paint backgroundPaint;
    private int backgroundProgressColor = ColorUtil.parserColor("#e5e5e5", 255);

    private Paint progressPaint;
    private int progressColor = ColorUtil.parserColor("#0288d1", 255);

    private Paint secondProgressPaint;
    private int secondProgressColor = ColorUtil.parserColor("#b8b8b8", 255);

    private Paint thumbPaint;
    private int thumbColor = ColorUtil.parserColor("#0288d1", 255);


    //是否正在拖动
    private boolean isDrag = false;

    /**
     * 日志
     */
    private LoggerUtil logger;
    /**
     * 提示文本
     */
    private String mTipText;
    /**
     * seekbar监听事件
     */
    private OnChangeListener onChangeListener;

    /**
     * 时间窗口
     */
    private PopupWindow mTimePopupWindow;
    private LinearLayout mTimePopupWindowView;
    private int mTimePopupWindowViewColor = ColorUtil.parserColor("#0288d1", 180);

    /**
     * 时间和歌词窗口
     */
    private PopupWindow mTimeAndLrcPopupWindow;
    private LinearLayout mTimeAndLrcPopupWindowView;
    private int mTimeAndLrcPopupWindowViewColor = ColorUtil.parserColor("#0288d1", 180);
    /**
     * 时间提示
     */
    private TextView mTimeTextView;

    /**
     * 歌词提示
     */
    private TextView mLrcTextView;

    private final int SHOWTIMEANDLRCVIEW = 0;
    private final int SHOWTIMEVIEW = 1;
    private final int HIDEVIEW = 2;
    private final int UPDATEVIEW = 3;

    /**
     *
     */
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case SHOWTIMEANDLRCVIEW:

                    showTimeLrcView();

                    break;
                case SHOWTIMEVIEW:

                    showTimeView();

                    break;
                case HIDEVIEW:

                    hideView();

                    break;
                case UPDATEVIEW:

                    upDateView();

                    break;
                default:
                    break;
            }

        }
    };


    public LrcSeekBar(Context context) {
        super(context);
        init(context);

    }

    public LrcSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LrcSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化
     */
    private void init(Context context) {
        this.mContext = context;
        logger = LoggerUtil.getZhangLogger(context);
        initPaint();
    }

    private void initPaint() {

        //
        backgroundPaint = new Paint();
        backgroundPaint.setDither(true);
        backgroundPaint.setAntiAlias(true);

        //
        progressPaint = new Paint();
        progressPaint.setDither(true);
        progressPaint.setAntiAlias(true);

        //
        secondProgressPaint = new Paint();
        secondProgressPaint.setDither(true);
        secondProgressPaint.setAntiAlias(true);

        //
        thumbPaint = new Paint();
        thumbPaint.setDither(true);
        thumbPaint.setAntiAlias(true);

        //
        setBackgroundProgressColorColor(backgroundProgressColor);
        setSecondProgressColor(secondProgressColor);
        setProgressColor(progressColor);
        setThumbColor(thumbColor);

        //
        setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (isDrag) {
                    //
                    String timeText = null;
                    String lrcText = null;
                    if (onChangeListener != null) {
                        timeText = onChangeListener.getTimeText();
                        lrcText = onChangeListener.getLrcText();
                    }
                    if (timeText != null && !timeText.equals("") && lrcText != null && !lrcText.equals("")) {
                        mHandler.sendEmptyMessage(SHOWTIMEANDLRCVIEW);
                    } else if (timeText != null && !timeText.equals("")) {
                        mHandler.sendEmptyMessage(SHOWTIMEVIEW);
                    }
                    mHandler.sendEmptyMessage(UPDATEVIEW);
                }
                if (onChangeListener != null) {
                    onChangeListener.onProgressChanged();
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                isDrag = true;
                invalidate();


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                new Thread() {

                    @Override
                    public void run() {
                        postInvalidate();
                        mHandler.sendEmptyMessage(HIDEVIEW);
                        //
                        if (onChangeListener != null) {
                            onChangeListener.dragFinish();
                        }
                        try {
                            // 延迟100ms才更新进度
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        isDrag = false;
                        postInvalidate();
                    }

                }.start();
            }
        });
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {


        int rSize = getHeight() / 4;
        if (isDrag) {
            rSize = getHeight() / 2;
        }
        int height = 2;
        int leftPadding = rSize;

        if (getProgress() > 0) {
            leftPadding = 0;
        }

        RectF backgroundRect = new RectF(leftPadding, getHeight() / 2 - height, getWidth(),
                getHeight() / 2 + height);
        //backgroundPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(backgroundRect, rSize, rSize, backgroundPaint);


        if (getMax() != 0) {
            RectF secondProgressRect = new RectF(leftPadding, getHeight() / 2 - height,
                    getSecondaryProgress() * getWidth() / getMax(), getHeight()
                    / 2 + height);
            canvas.drawRoundRect(secondProgressRect, rSize, rSize, secondProgressPaint);

            RectF progressRect = new RectF(leftPadding, getHeight() / 2 - height,
                    getProgress() * getWidth() / getMax(), getHeight() / 2
                    + height);
            canvas.drawRoundRect(progressRect, rSize, rSize, progressPaint);


            int cx = getProgress() * getWidth() / getMax();
            if ((cx + rSize) > getWidth()) {
                cx = getWidth() - rSize;
            } else {
                cx = Math.max(cx, rSize);
            }
            int cy = getHeight() / 2;
            canvas.drawCircle(cx, cy, rSize, thumbPaint);
        }
    }

    @Override
    public synchronized void setProgress(int progress) {
        if (!isDrag)
            super.setProgress(progress);
    }

    /**
     * 显示时间view
     */
    private void showTimeView() {
        if (mTimeAndLrcPopupWindow != null && mTimeAndLrcPopupWindow.isShowing()) {
            mTimeAndLrcPopupWindow.dismiss();
        }

        //
        int popHeight = (int) mContext.getResources().getDimension(R.dimen.pop_height);
        //
        if (mTimePopupWindow == null) {

            mTimeTextView = new TextView(mContext);

            int timeWidth = (int) (mTimeTextView.getTextSize()) * "00:00".length();
            //
            LinearLayout.LayoutParams popLayout = new LinearLayout.LayoutParams(timeWidth, popHeight);
            mTimePopupWindowView = new LinearLayout(mContext);
            mTimePopupWindowView.setLayoutParams(popLayout);


            //

            LinearLayout.LayoutParams timeLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

            mTimeTextView.setLayoutParams(timeLayout);
            mTimeTextView.setTextColor(Color.WHITE);
            mTimeTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER | Gravity.CENTER_HORIZONTAL);
            mTimeTextView.setSingleLine(true);
            mTimeTextView.setEllipsize(TextUtils.TruncateAt.END);
            mTimePopupWindowView.addView(mTimeTextView);

            mTimePopupWindow = new PopupWindow(mTimePopupWindowView, timeWidth,
                    popHeight, true);

        }
        //////////////////////////

        int strokeWidth = 1; // 3dp 边框宽度
        float[] roundRadius = {15, 15, 15, 15, 15, 15, 15, 15}; // 圆角半径
        int strokeColor = Color.TRANSPARENT;

        GradientDrawable gd = new GradientDrawable();// 创建drawable
        gd.setColor(mTimePopupWindowViewColor);
        gd.setCornerRadii(roundRadius);
        gd.setStroke(strokeWidth, strokeColor);

        ///////////////////////////////
        mTimePopupWindowView.setBackgroundDrawable(gd);

        if (mTimePopupWindow != null && !mTimePopupWindow.isShowing()) {
            int[] location = new int[2];
            this.getLocationOnScreen(location);

            int leftX = location[0] + getWidth() * getProgress() / getMax();

            mTimePopupWindow.showAtLocation(this, Gravity.NO_GRAVITY, leftX, location[1]
                    - mTimePopupWindow.getHeight() * 3 / 2);
        }
    }

    /**
     * 显示时间和歌词view
     */
    private void showTimeLrcView() {

        if (mTimePopupWindow != null && mTimePopupWindow.isShowing()) {
            mTimePopupWindow.dismiss();
        }
        //
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int screenWidth = display.getWidth();
        //
        int popHeight = (int) mContext.getResources().getDimension(R.dimen.pop_height);
        int popPadding = (int) mContext.getResources().getDimension(R.dimen.pop_Padding);
        //
        if (mTimeAndLrcPopupWindow == null) {

            //
            LinearLayout.LayoutParams popLayout = new LinearLayout.LayoutParams(screenWidth - popPadding * 2, popHeight);
            mTimeAndLrcPopupWindowView = new LinearLayout(mContext);
            mTimeAndLrcPopupWindowView.setLayoutParams(popLayout);


            //
            mTimeTextView = new TextView(mContext);
            LinearLayout.LayoutParams timeLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            timeLayout.leftMargin = popPadding;
            mTimeTextView.setLayoutParams(timeLayout);
            mTimeTextView.setTextColor(Color.WHITE);
            mTimeTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER | Gravity.CENTER_HORIZONTAL);
            mTimeTextView.setSingleLine(true);
            mTimeTextView.setEllipsize(TextUtils.TruncateAt.END);
            mTimeAndLrcPopupWindowView.addView(mTimeTextView);

            //
            mLrcTextView = new TextView(mContext);
            LinearLayout.LayoutParams lrcLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            mLrcTextView.setLayoutParams(lrcLayout);
            mLrcTextView.setTextColor(Color.WHITE);
            mLrcTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER | Gravity.CENTER_HORIZONTAL);
            mLrcTextView.setSingleLine(true);
            mLrcTextView.setEllipsize(TextUtils.TruncateAt.END);
            mTimeAndLrcPopupWindowView.addView(mLrcTextView);

            mTimeAndLrcPopupWindow = new PopupWindow(mTimeAndLrcPopupWindowView, screenWidth - popPadding * 2,
                    popHeight, true);

        }
        //////////////////////////

        int strokeWidth = 1; // 3dp 边框宽度
        float[] roundRadius = {15, 15, 15, 15, 15, 15, 15, 15}; // 圆角半径
        int strokeColor = Color.TRANSPARENT;

        GradientDrawable gd = new GradientDrawable();// 创建drawable
        gd.setColor(mTimeAndLrcPopupWindowViewColor);
        gd.setCornerRadii(roundRadius);
        gd.setStroke(strokeWidth, strokeColor);

        ///////////////////////////////
        mTimeAndLrcPopupWindowView.setBackgroundDrawable(gd);

        if (mTimeAndLrcPopupWindow != null && !mTimeAndLrcPopupWindow.isShowing()) {
            int[] location = new int[2];
            this.getLocationOnScreen(location);

            mTimeAndLrcPopupWindow.showAtLocation(this, Gravity.NO_GRAVITY, popPadding, location[1]
                    - mTimeAndLrcPopupWindow.getHeight() * 3 / 2);
        }

    }

    /**
     * 隐藏view
     */
    private void hideView() {
        if (mTimeAndLrcPopupWindow != null && mTimeAndLrcPopupWindow.isShowing()) {
            mTimeAndLrcPopupWindow.dismiss();
        }

        if (mTimePopupWindow != null && mTimePopupWindow.isShowing()) {
            mTimePopupWindow.dismiss();
        }
    }

    /**
     * 更新view
     */
    private void upDateView() {
        if ((mTimeAndLrcPopupWindow != null && mTimeAndLrcPopupWindow.isShowing()) || (mTimePopupWindow != null && mTimePopupWindow.isShowing())) {

            String timeText = null;
            String lrcText = null;
            if (onChangeListener != null) {
                timeText = onChangeListener.getTimeText();
                lrcText = onChangeListener.getLrcText();
            }
            if (timeText == null) {
                return;
            }

            mTimeTextView.setText(timeText);


            //如果时间窗口正在显示
            if (mTimePopupWindow != null && mTimePopupWindow.isShowing()) {
                int[] location = new int[2];
                this.getLocationOnScreen(location);

                int leftX = location[0] + getWidth() * getProgress() / getMax() - mTimePopupWindow.getWidth() / 2;

                //判断是否越界
                if ((leftX + mTimePopupWindow.getWidth() / 2) > (location[0] + getWidth())) {
                    leftX = (location[0] + getWidth()) - mTimePopupWindow.getWidth() / 2;
                } else if (leftX < location[0]) {
                    leftX = location[0];
                }
                //logger.e("leftX=" + leftX);

                //更新弹出窗口的位置
                mTimePopupWindow.update(leftX, location[1]
                        - mTimePopupWindow.getHeight() * 3 / 2, -1, -1);
            }

            if (lrcText == null) {

                return;
            }

            mLrcTextView.setText(lrcText);
        }
    }

    public interface OnChangeListener {

        void onProgressChanged();

        String getTimeText();

        String getLrcText();

        void dragFinish();
    }


    public void setOnChangeListener(OnChangeListener onChangeListener) {
        this.onChangeListener = onChangeListener;
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

    /////////////////////////////////////////////////////////////


    public void setBackgroundProgressColorColor(int color) {
        backgroundProgressColor = color;
        backgroundPaint.setColor(backgroundProgressColor);
        invalidateView();
    }

    public void setProgressColor(int color) {
        progressColor = color;
        progressPaint.setColor(progressColor);
        invalidateView();
    }

    public void setSecondProgressColor(int color) {
        secondProgressColor = color;
        secondProgressPaint.setColor(secondProgressColor);
        invalidateView();
    }

    public void setThumbColor(int color) {
        thumbColor = color;
        thumbPaint.setColor(thumbColor);
        invalidateView();
    }

    public void setTimePopupWindowViewColor(int fillColor) {

        mTimePopupWindowViewColor = fillColor;

    }

    public void setTimeAndLrcPopupWindowViewColor(int fillColor) {

        mTimeAndLrcPopupWindowViewColor = fillColor;


    }


}
