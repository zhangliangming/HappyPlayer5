package com.zlm.hp.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.dialog.AlartOneButtonDialog;

/**
 * @Description: 文件权限处理类
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2018/1/8 15:39
 * @Throws:
 */

public class StoragePermissionUtil {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * 提示窗口
     */
    private AlartOneButtonDialog mAlartOneButtonDialog;

    private HPApplication mHPApplication;

    public StoragePermissionUtil(HPApplication hpApplication, Activity mActivity) {

        this.mHPApplication = hpApplication;

        mAlartOneButtonDialog = new AlartOneButtonDialog(mActivity, new AlartOneButtonDialog.ButtonDialogListener() {
            @Override
            public void ButtonClick() {

                closeApp();

            }
        });

        mAlartOneButtonDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override

            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                // 默认返回 false
                return keyCode == KeyEvent.KEYCODE_BACK;

            }

        });
    }

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to
     * grant permissions
     *
     * @param activity
     */
    public boolean verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);

            return false;
        }

        return true;
    }

    /**
     * 权限回调
     */
    public interface RequestPermissionsResult {
        /**
         * 授权成功回调
         */
        void acceptedCallback();
    }

    //用户处理权限反馈，在这里判断用户是否授予相应的权限

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, RequestPermissionsResult mRequestPermissionsResult) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (mRequestPermissionsResult != null) {
                        mRequestPermissionsResult.acceptedCallback();
                    } else {
                        closeApp();
                    }

                } else {
                    mAlartOneButtonDialog.showDialog("读写文件权限是应用的基本权限，请到应用的权限管理界面授权！！", "退出应用");

                }

                break;
            default:
                break;
        }

    }

    /**
     * 关闭应用
     */
    private void closeApp() {
        if (mHPApplication != null)
            mHPApplication.setAppClose(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

}
