package com.zlm.hp.ui.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.zlm.hp.R;
import com.zlm.hp.application.HPApplication;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.utils.MediaUtil;

import java.util.List;

import base.utils.ThreadUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    public Activity mActivity;
    public Context mContext;
    private Preference mMobileNetworkDownload;
    private Preference mFilterSize;
    private Preference mFilterTime;

    private ProgressDialog mProgressDialog;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
        this.mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_setting);

        mMobileNetworkDownload = findPreference(getString(R.string.setting_key_mobile_network_download));
        mFilterSize = findPreference(getString(R.string.setting_key_filter_size));
        mFilterTime = findPreference(getString(R.string.setting_key_filter_time));
        mMobileNetworkDownload.setOnPreferenceChangeListener(this);
        mFilterSize.setOnPreferenceChangeListener(this);
        mFilterTime.setOnPreferenceChangeListener(this);

        mMobileNetworkDownload.setDefaultValue(HPApplication.getInstance().isDownload());
        mFilterSize.setSummary(getSummary(MediaUtil.getFilterSize(mContext), R.array.filter_size_entries, R.array.filter_size_entry_values));
        mFilterTime.setSummary(getSummary(MediaUtil.getFilterTime(mContext), R.array.filter_time_entries, R.array.filter_time_entry_values));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mFilterSize) {
            MediaUtil.setFilterSize(mContext, (String) newValue);
            mFilterSize.setSummary(getSummary(MediaUtil.getFilterSize(mContext), R.array.filter_size_entries, R.array.filter_size_entry_values));
            onFilterChanged();
            return true;
        } else if (preference == mFilterTime) {
            MediaUtil.setFilterTime(mContext, (String) newValue);
            mFilterTime.setSummary(getSummary(MediaUtil.getFilterTime(mContext), R.array.filter_time_entries, R.array.filter_time_entry_values));
            onFilterChanged();
            return true;
        } else if (preference == mMobileNetworkDownload) {
            boolean value = (boolean) newValue;
            HPApplication.getInstance().setDownload(value);
            return true;
        }
        return false;
    }

    private String getSummary(String value, int entries, int entryValues) {
        String[] entryArray = getResources().getStringArray(entries);
        String[] entryValueArray = getResources().getStringArray(entryValues);
        for (int i = 0; i < entryValueArray.length; i++) {
            String v = entryValueArray[i];
            if (TextUtils.equals(v, value)) {
                return entryArray[i];
            }
        }
        return entryArray[0];
    }

    private void onFilterChanged() {
        showProgress();
        ThreadUtil.runInThread(new Runnable() {
            @Override
            public void run() {
                cancelProgress();
                MediaUtil.scanMusic(mContext, new MediaUtil.ForeachListener() {
                    @Override
                    public void before() {
                        AudioInfoDB.getAudioInfoDB(mContext).delete(AudioInfo.LOCAL);
                    }

                    @Override
                    public void foreach(List<AudioInfo> audioInfoList) {
                        boolean addResult = AudioInfoDB.getAudioInfoDB(mContext).add(audioInfoList);
                        //发送更新广播
                        Intent updateIntent = new Intent(AudioBroadcastReceiver.ACTION_LOCALUPDATE);
                        updateIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        mContext.sendBroadcast(updateIntent);

                        mActivity.finish();
                        mActivity.overridePendingTransition(0, 0);
                    }

                    @Override
                    public boolean filter(String hash) {
                        return AudioInfoDB.getAudioInfoDB(mContext).isExists(hash);
                    }
                });
            }
        });
    }

    private void showProgress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在扫描音乐");
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    private void cancelProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }
}
