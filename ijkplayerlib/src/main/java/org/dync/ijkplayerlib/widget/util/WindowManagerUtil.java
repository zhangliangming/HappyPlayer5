package org.dync.ijkplayerlib.widget.util;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import java.lang.reflect.Field;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class WindowManagerUtil {

    /**
     * 小悬浮窗View的实例
     */
    private static IjkWindowVideoView smallWindow;

    /**
     * 小悬浮窗View的参数
     */
    private static LayoutParams smallWindowParams;

    /**
     * 用于控制在屏幕上添加或移除悬浮窗
     */
    private static WindowManager mWindowManager;

    /**
     * 用于获取手机可用内存
     */
    private static ActivityManager mActivityManager;

    /**
     * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
     *
     * @param context 必须为应用程序的Context.
     */
    public static void createSmallWindow(Context context) {
        createSmallWindow(context, null);
    }

    public static void createSmallWindow(final Context context, IMediaPlayer mediaPlayer) {
        mWindowManager = getWindowManager(context);
        int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
        int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
        if (smallWindowParams == null) {
            smallWindowParams = new LayoutParams();
            smallWindowParams.type = LayoutParams.TYPE_PHONE;
            smallWindowParams.format = PixelFormat.RGBA_8888;
            smallWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
            //小窗口摆放的位置，手机屏幕中央
            smallWindowParams.x = screenWidth / 2 - 350 / 2;
            smallWindowParams.y = screenHeight / 2 - 280 / 2;
            smallWindowParams.width = 250;
            smallWindowParams.height = 200;
        }
        smallWindow = new IjkWindowVideoView(context);
        if(mediaPlayer != null) {
            smallWindow.setMediaPlayer(mediaPlayer);
        }
        smallWindow.setLayoutParams(smallWindowParams);
        mWindowManager.addView(smallWindow, smallWindowParams);
    }

    private static int statusBarHeight;
    private static int screenWidth;
    private static int screenHeight;
    private static int lastX;
    private static int lastY;

    public static void createSmallWindow(ViewGroup view, IMediaPlayer mediaPlayer) {
        Context context = view.getContext();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels - getStatusBarHeight(context);
        smallWindow = new IjkWindowVideoView(context);
        smallWindow.setFocusableInTouchMode(false);
        if(mediaPlayer != null) {
            smallWindow.setMediaPlayer(mediaPlayer);
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = 250;
        layoutParams.height = 200;
        view.addView(smallWindow);
        view.setClickable(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action=event.getAction();
                Log.i("TAG", "Touch:"+action);
                switch(action){
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    /**
                     * layout(l,t,r,b)
                     * l  Left position, relative to parent
                     t  Top position, relative to parent
                     r  Right position, relative to parent
                     b  Bottom position, relative to parent
                     * */
                    case MotionEvent.ACTION_MOVE:
                        int dx =(int)event.getRawX() - lastX;
                        int dy =(int)event.getRawY() - lastY;

                        int left = v.getLeft() + dx;
                        int top = v.getTop() + dy;
                        int right = v.getRight() + dx;
                        int bottom = v.getBottom() + dy;
                        if(left < 0){
                            left = 0;
                            right = left + v.getWidth();
                        }
                        if(right > screenWidth){
                            right = screenWidth;
                            left = right - v.getWidth();
                        }
                        if(top < 0){
                            top = 0;
                            bottom = top + v.getHeight();
                        }
                        if(bottom > screenHeight){
                            bottom = screenHeight;
                            top = bottom - v.getHeight();
                        }
                        v.layout(left, top, right, bottom);
                        Log.i("TAG", "position: " + left +", " + top + ", " + right + ", " + bottom);
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 将小悬浮窗从屏幕上移除。
     *
     * @param context 必须为应用程序的Context.
     */
    public static void removeSmallWindow(Context context) {
        if (smallWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(smallWindow);
            smallWindow = null;
        }
    }

    /**
     * 更新小悬浮窗的TextView上的数据，显示内存使用的百分比。
     *
     * @param mAppContext
     *            可传入应用程序上下文。
     */
//	public static void updateUsedPercent(Context mAppContext) {
//		if (smallWindow != null) {
//			TextView percentView = (TextView) smallWindow.findViewById(R.id.percent);
//			percentView.setText(getUsedPercentValue(mAppContext));
//		}
//	}

    /**
     * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
     *
     * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
     */
//	public static boolean isWindowShowing() {
//		return smallWindow != null || bigWindow != null;
//	}

    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @param context 必须为应用程序的Context.
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 如果ActivityManager还未创建，则创建一个新的ActivityManager返回。否则返回当前已创建的ActivityManager。
     *
     * @param context 可传入应用程序上下文。
     * @return ActivityManager的实例，用于获取手机可用内存。
     */
    private static ActivityManager getActivityManager(Context context) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return mActivityManager;
    }

    /**
     * 计算已使用内存的百分比，并返回。
     *
     * @param mAppContext
     *            可传入应用程序上下文。
     * @return 已使用内存的百分比，以字符串形式返回。
     */
//	public static String getUsedPercentValue(Context mAppContext) {
//		String dir = "/proc/meminfo";
//		try {
//			FileReader fr = new FileReader(dir);
//			BufferedReader br = new BufferedReader(fr, 2048);
//			String memoryLine = br.readLine();
//			String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
//			br.close();
//			long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
//			long availableSize = getAvailableMemory(mAppContext) / 1024;
//			int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 100);
//			return percent + "%";
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return "悬浮窗";
//	}

    /**
     * 获取当前可用内存，返回数据以字节为单位。
     *
     * @param mAppContext
     *            可传入应用程序上下文。
     * @return 当前可用内存。
     */
//	private static long getAvailableMemory(Context mAppContext) {
//		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
//		getActivityManager(mAppContext).getMemoryInfo(mi);
//		return mi.availMem;
//	}

    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    private static int getStatusBarHeight(Context context) {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = context.getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

}
