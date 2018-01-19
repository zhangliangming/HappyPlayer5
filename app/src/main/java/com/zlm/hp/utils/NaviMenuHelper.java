package com.zlm.hp.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;

import com.soundcloud.android.crop.Crop;
import com.zlm.hp.R;
import com.zlm.hp.constants.PreferencesConstants;
import com.zlm.hp.ui.AboutActivity;
import com.zlm.hp.ui.BaseActivity;
import com.zlm.hp.ui.MainActivity;
import com.zlm.hp.ui.SkinActivity;

import base.utils.PreferencesUtil;

/**
 * 导航菜单执行器
 * Created by hzwangchenyan on 2016/1/14.
 */
public class NaviMenuHelper {

    public static boolean onNavigationItemSelected(MenuItem item, BaseActivity activity) {
        switch (item.getItemId()) {
            case R.id.action_setting:
                startActivity(activity, AboutActivity.class);
                return true;
            case R.id.action_skin_peeler:
                updateSkin(activity);
                return true;
            case R.id.action_night:
//                nightMode(activity);
                return true;
            case R.id.action_timer:
//                timerDialog(activity);
                return true;
            case R.id.action_exit:
                exit(activity);
                return true;
            case R.id.action_about:
                startActivity(activity, AboutActivity.class);
                return true;
        }
        return false;
    }

    private static void startActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.putExtra(PreferencesConstants.shadowEnable_KEY, false);
        context.startActivity(intent);
    }

    private static void nightMode(final BaseActivity activity) {
        boolean nightMode = PreferencesUtil.getBooleanValue(activity, PreferencesConstants.nightMode_KEY, false);
        PreferencesUtil.putBooleanVaule(activity, PreferencesConstants.nightMode_KEY, !nightMode);
        activity.recreate();
    }

    private static void timerDialog(final BaseActivity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.menu_timer)
                .setItems(activity.getResources().getStringArray(R.array.timer_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int[] times = activity.getResources().getIntArray(R.array.timer_int);
                        startTimer(activity, times[which]);
                    }
                })
                .show();
    }

    private static void updateSkin(BaseActivity activity) {
        Intent intent = new Intent(activity, SkinActivity.class);
        activity.startActivity(intent);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        activity.startActivityForResult(intent, MainActivity.PHOTO_REQUEST_GALLERY);
        Crop.pickImage(activity, MainActivity.PHOTO_REQUEST_GALLERY);
    }

    private static void startTimer(Context context, int minute) {
        QuitTimer.getInstance().start(minute * 60 * 1000);
        if (minute > 0) {
            ToastShowUtil.showTextToast(context, context.getString(R.string.timer_set, String.valueOf(minute)));
        } else {
            ToastShowUtil.showTextToast(context, context.getString(R.string.timer_cancel));
        }
    }

    private static void exit(BaseActivity activity) {
        activity.finish();
//        PlayService service = AppCache.get().getPlayService();
//        if (service != null) {
//            service.quit();
//        }
    }
}
