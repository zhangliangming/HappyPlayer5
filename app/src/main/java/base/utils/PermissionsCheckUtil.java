package base.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.zlm.hp.R;

/**
 * Created by xiongxuesong-pc on 2016/6/23.
 * 权限检查工具类
 */
public class PermissionsCheckUtil {

    public static final int SETTING_APP = 0x123;
    private static String TAG = "PermissionsCheckUtil";
    //包含在该数组的手机品牌可以跳到设置界面直接进行权限修改
    private static String[] PHONE_MTYB = new String[]{"sanxing", "xiaomi"};

    /**
     * 判断当前手机版本是否大于等于Android 6.0
     *
     * @return
     */
    private static boolean thanSDK23() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);

    }

    /**
     * @param activity
     * @param messageId  显示缺失权限提示说明
     */
    public static void showMissingPermissionDialog(final Activity activity, @StringRes int messageId) {
        boolean canSetting = true;
//        String mtyb = Build.BRAND;//手机品牌
//        for (int i = 0; i < PHONE_MTYB.length; i++) {
//            if (PHONE_MTYB[i].equalsIgnoreCase(mtyb)) {//相等可以调用到设置界面进行权限设置
//                canSetting = true;
//                break;
//            } else {
//                canSetting = false;
//            }
//        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.help);
        builder.setMessage(messageId);
        if (canSetting) {
            builder.setPositiveButton(R.string.desktop_dialog_title, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startAppSettings(activity);
                    dialog.dismiss();
                }
            });
        }
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * @param activity
     * @param message  显示缺失权限提示说明
     */
    public static void showMissingPermissionDialog(final Activity activity, String message) {
        boolean canSetting = true;
//        String mtyb = Build.BRAND;//手机品牌
//        for (int i = 0; i < PHONE_MTYB.length; i++) {
//            if (PHONE_MTYB[i].equalsIgnoreCase(mtyb)) {//相等可以调用到设置界面进行权限设置
//                canSetting = true;
//                break;
//            } else {
//                canSetting = false;
//            }
//        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.help);
        builder.setMessage(message);
        if (canSetting) {
            builder.setPositiveButton(R.string.desktop_dialog_title, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startAppSettings(activity);
                    dialog.dismiss();
                }
            });
        }
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    // 启动应用的设置
    public static void startAppSettings(Activity activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, SETTING_APP);
            activity.finish();
        } catch (Exception e) {
            Toast.makeText(activity, R.string.please_manually_go_to_permission_management_page_to_set_up, Toast.LENGTH_LONG).show();
        }
    }

}
