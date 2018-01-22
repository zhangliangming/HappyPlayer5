package base.widget.dialog;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.zlm.hp.R;

import java.io.Serializable;

/**
 * Created by KathLine on 2016/11/17.</br>
 * <p>使用说明</p>
 * <pre>
CustomDialogFragment.Builder builder = new CustomDialogFragment.Builder(mContext);
mGratuityDialogFragment = builder.setFullScreen(true)
.setContentView(R.layout.dialogfragment_gratuity)
.setExistDialogLined(true)
.setBackgroundDrawable(true)
.setLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
.build(fragmentManager, "GratuityDialogFragment");
mGratuityDialogFragment.setOnInitListener(new CustomDialogFragment.OnInitListener() {
        @Override
        public void init(View view) {

        }
});
 </pre>
 */

public class CustomDialogFragment extends DialogFragment {

    protected Builder builder;
    protected int layoutId;
    protected int gravity;
    protected int animId;
    protected boolean backgroundDrawableable;
    protected float dimAmount;
    protected boolean cancelable;
    protected boolean existDialogLined;
    protected boolean isFullScreen;
    protected int offsetX;
    protected int offsetY;
    protected int width;
    protected int height;

    public CustomDialogFragment() {

    }

    public interface OnInitListener{
        void init(View view);
    }

    public OnInitListener mOnInitListener;

    public void setOnInitListener(OnInitListener listener){
        mOnInitListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Builder builder = (Builder) getArguments().getSerializable("builder");
        if (builder != null) {
            this.builder = builder;
            layoutId = builder.layoutId;
            gravity = builder.gravity;
            animId = builder.animId;
            backgroundDrawableable = builder.backgroundDrawableable;
            dimAmount = builder.dimAmount;
            cancelable = builder.cancelable;
            existDialogLined = builder.existDialogLined;
            isFullScreen = builder.isFullScreen;
            offsetX = builder.offsetX;
            offsetY = builder.offsetY;
            width = builder.width;
            height = builder.height;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setLayout();
        View view = inflater.inflate(layoutId, container);
        if (mOnInitListener != null){
            mOnInitListener.init(view);
        }
        return view;
    }

    private void setLayout() {
        if (existDialogLined) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        Window window = getDialog().getWindow();
        window.setGravity(gravity);
        if(animId != 0) {
            window.setWindowAnimations(animId);
        }
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        if (width != 0 && height != 0) {
            lp.width = width;
            lp.height = height;
        }
        if (isFullScreen) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏，即没有系统状态栏
        }
        if (backgroundDrawableable) {
            window.setBackgroundDrawable(new ColorDrawable(0));
        }
        if (dimAmount < 0f || dimAmount > 1f) {
            throw new RuntimeException("透明度必须在0~1之间");
        } else {
            lp.dimAmount = dimAmount;
        }
        if (offsetX != 0) {
            lp.x = offsetX;
        }
        if (offsetY != 0) {
            lp.y = offsetY;
        }
        window.setAttributes(lp);
        setCancelable(cancelable);
        if (cancelable) {
            getDialog().setCanceledOnTouchOutside(true);
        }
    }

    public static class Builder implements Serializable {
        public Context context;
        public int layoutId;
        public int gravity;
        public int animId;
        public boolean backgroundDrawableable;
        public float dimAmount;
        public boolean cancelable;
        public boolean existDialogLined;
        public boolean isFullScreen;
        public int offsetX;
        public int offsetY;
        public int width;
        public int height;

        public Builder(Context context) {
            this.context = context;
            layoutId = R.layout.dialog_base;
            gravity = Gravity.CENTER;
            animId = 0;
            backgroundDrawableable = false;
            dimAmount = 0.5f;
            cancelable = true;
            existDialogLined = true;
            isFullScreen = false;
            width = WindowManager.LayoutParams.WRAP_CONTENT;
            height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        public Builder setContentView(@LayoutRes int layoutId) {
            this.layoutId = layoutId;
            return this;
        }

        /**
         * 必须使用Gravity的静态常量，默认在中间弹出
         *
         * @param gravity 详见{@link Gravity}
         * @return
         * @see Gravity
         */
        public Builder setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        /**
         * 设置Dialog弹出和Dialog退出的动画
         *
         * @param animId
         * @return
         */
        public Builder setAnimId(int animId) {
            this.animId = animId;
            return this;
        }

        /**
         * Creates a new set of layout parameters with the specified width
         * and height.
         *
         * @param width  the width, either set WindowManager.LayoutParams.WRAP_CONTENT or
         *               WindowManager.LayoutParams.FILL_PARENT (replaced by WindowManager.LayoutParams.MATCH_PARENT in
         *               API Level 8), or a fixed size in pixels
         * @param height the height, either set WindowManager.LayoutParams.WRAP_CONTENT or
         *               WindowManager.LayoutParams.FILL_PARENT (replaced by WindowManager.LayoutParams.MATCH_PARENT in
         *               API Level 8), or a fixed size in pixels
         * @return
         */
        public Builder setLayoutParams(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * 设置Dialog的位置<br>
         * lp.x与lp.y表示相对于原始位置的偏移.
         * 当参数值包含Gravity.LEFT时,对话框出现在左边,所以lp.x就表示相对左边的偏移,负值无效.
         * 当参数值包含Gravity.RIGHT时,对话框出现在右边,所以lp.x就表示相对右边的偏移,负值无效.
         * 当参数值包含Gravity.TOP时,对话框出现在上边,所以lp.y就表示相对上边的偏移,负值无效.
         * 当参数值包含Gravity.BOTTOM时,对话框出现在下边,所以lp.y就表示相对下边的偏移,负值无效.
         * 当参数值包含Gravity.CENTER_HORIZONTAL时
         * ,对话框水平居中,所以lp.x就表示在水平居中的位置移动lp.x像素,正值向右移动,负值向左移动.
         * 当参数值包含Gravity.CENTER_VERTICAL时
         * ,对话框垂直居中,所以lp.y就表示在垂直居中的位置移动lp.y像素,正值向右移动,负值向左移动.
         * gravity的默认值为Gravity.CENTER
         *
         * @param x x小于0左移，大于0右移
         * @param y y小于0上移，大于0下移
         * @return
         */
        public Builder setOffset(int x, int y) {
            this.offsetX = x;
            this.offsetY = y;
            return this;
        }

        /**
         * 是否给Dialog的背景设置透明，默认false
         *
         * @param backgroundDrawableable
         * @return
         */
        public Builder setBackgroundDrawable(boolean backgroundDrawableable) {
            this.backgroundDrawableable = backgroundDrawableable;
            return this;
        }

        /**
         * 设置Dialog之外的背景透明度，0~1之间，默认值 0.5f，半透明
         *
         * @param dimAmount
         * @return
         */
        public Builder setDimAmount(float dimAmount) {
            this.dimAmount = dimAmount;
            return this;
        }

        /**
         * 设置Dialog是否可以关闭在Dialog之外的区域，默认true
         *
         * @param cancelable
         * @return
         */
        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        /**
         * 如果存在Holo主题下Dialog有蓝色线(含有标题栏)可以尝试调用该方法，默认不存在
         *
         * @param existDialogLined
         * @return
         */
        public Builder setExistDialogLined(boolean existDialogLined) {
            this.existDialogLined = existDialogLined;
            return this;
        }

        /**
         * 是否设置全屏模式，指的是去除系统状态栏，默认不去除
         *
         * @param isFullScreen
         * @return
         */
        public Builder setFullScreen(boolean isFullScreen) {
            this.isFullScreen = isFullScreen;
            return this;
        }

        public CustomDialogFragment build(FragmentManager manager, String tag){
            //谷歌推荐使用这种方式保存传进来的数据
            Bundle bundle = new Bundle();
            bundle.putSerializable("builder", this);

            CustomDialogFragment dialogFragment = new CustomDialogFragment();
            dialogFragment.setArguments(bundle);

            dialogFragment.show(manager, tag);
            return dialogFragment;
        }
    }

    @Override
    public void dismiss() {
        if(getFragmentManager() != null) {
            super.dismiss();
        }
    }
}
