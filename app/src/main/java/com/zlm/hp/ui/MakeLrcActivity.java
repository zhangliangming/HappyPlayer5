package com.zlm.hp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.zlm.hp.adapter.TabFragmentAdapter;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.dialog.AlartTwoButtonDialog;
import com.zlm.hp.fragment.EditLrcTextFragment;
import com.zlm.hp.fragment.MakeLrcFragment;
import com.zlm.hp.fragment.PreviewLrcFragment;
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.lyrics.formats.LyricsFileWriter;
import com.zlm.hp.lyrics.model.LyricsInfo;
import com.zlm.hp.lyrics.model.MakeLrcLineInfo;
import com.zlm.hp.lyrics.utils.FileUtils;
import com.zlm.hp.lyrics.utils.LyricsIOUtils;
import com.zlm.hp.manager.LyricsManager;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.utils.ResourceFileUtil;
import com.zlm.hp.widget.CustomViewPager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Description: 制作歌词
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2018-03-25
 * @Throws:
 */
public class MakeLrcActivity extends BaseActivity {

    /**
     *
     */
    private CustomViewPager mViewPager;

    /**
     * 编辑界面
     */
    private EditLrcTextFragment mEditLrcTextFragment;
    /**
     * 敲打节奏
     */
    private MakeLrcFragment mMakeLrcFragment;

    /**
     * 歌词预览
     */
    private PreviewLrcFragment mPreviewLrcFragment;

    /**
     * 事件回调
     */
    private MakeLrcFragmentEvent mMakeLrcFragmentEvent;

    /**
     * 歌曲路径
     */
    private String mAudioFilePath;

    private int mPageIndex;

    private final int CLOSE = 0;
    private final int PREPAGE = 1;
    private final int NEXTPAGE = 2;
    private int type = CLOSE;

    /**
     * 两个按钮弹出窗口
     */
    private AlartTwoButtonDialog mAlartTwoButtonDialog;

    /**
     *
     */
    private boolean mReloadLrcData = false;
    private String mHash;

    @Override
    protected int setContentViewId() {
        return R.layout.activity_make_lrc;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        mViewPager = findViewById(R.id.viewpage);
        mMakeLrcFragmentEvent = new MakeLrcFragmentEvent() {
            @Override
            public void close() {
                type = CLOSE;
                mAlartTwoButtonDialog.showDialog("退出后本页面数据将丢弃!", "确定", "取消");
            }

            @Override
            public void nextPage(int index) {
                type = NEXTPAGE;
                mPageIndex = index;
                if (mPageIndex == 1 && type == NEXTPAGE) {
                    mViewPager.setCurrentItem(mPageIndex, true);
                    mMakeLrcFragment.initData(mAudioFilePath, mEditLrcTextFragment.getLrcComText());
                }
            }

            @Override
            public void prePage(int index) {
                type = PREPAGE;
                mPageIndex = index;
                mAlartTwoButtonDialog.showDialog("退出后本页面数据将丢弃!", "确定", "取消");
            }

            @Override
            public void openPreView(LyricsInfo lyricsInfo) {
                type = NEXTPAGE;
                mPageIndex = 2;
                if (mPageIndex == 2 && type == NEXTPAGE) {
                    mViewPager.setCurrentItem(mPageIndex, true);
                    mPreviewLrcFragment.initData(mAudioFilePath, lyricsInfo);
                }
            }

            @Override
            public void saveLrcData(LyricsInfo lyricsInfo) {
                try {
                    //保存歌词
                    File audioFile = new File(mAudioFilePath);
                    String lrcFileName = FileUtils.removeExt(audioFile.getName()) + ".hrc";
                    //
                    lyricsInfo.setLyricsFileExt(FileUtils.getFileExt(lrcFileName));
                    lyricsInfo.setLyricsTags(new HashMap<String, Object>());

                    String saveLrcFilePath = ResourceFileUtil.getFilePath(getApplicationContext(), ResourceConstants.PATH_LYRICS, lrcFileName);
                    LyricsFileWriter lyricsFileWriter = LyricsIOUtils.getLyricsFileWriter(lrcFileName);
                    boolean result = lyricsFileWriter.writer(lyricsInfo, saveLrcFilePath);
                    if (result) {
                        Toast.makeText(getApplicationContext(), "保存成功！", Toast.LENGTH_SHORT).show();

                        if (mReloadLrcData && mHash != null && !mHash.equals("")) {

                            LyricsManager.getLyricsManager(mHPApplication,getApplicationContext()).setUseLrcUtil(mHash,saveLrcFilePath);

                        }

                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "保存失败！", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    Toast.makeText(getApplicationContext(), "保存失败！", Toast.LENGTH_SHORT).show();

                }

            }
        };

        ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        //编辑歌词界面
        mEditLrcTextFragment = new EditLrcTextFragment();
        mEditLrcTextFragment.setMakeLrcFragmentEvent(mMakeLrcFragmentEvent);
        fragments.add(mEditLrcTextFragment);

        //敲打节奏

        mMakeLrcFragment = new MakeLrcFragment();
        mMakeLrcFragment.setMakeLrcFragmentEvent(mMakeLrcFragmentEvent);
        fragments.add(mMakeLrcFragment);

        //预览歌词界面
        mPreviewLrcFragment = new PreviewLrcFragment();
        mPreviewLrcFragment.setMakeLrcFragmentEvent(mMakeLrcFragmentEvent);
        fragments.add(mPreviewLrcFragment);

        //
        TabFragmentAdapter adapter = new TabFragmentAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(adapter);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });


        //两个按钮弹出窗口
        mAlartTwoButtonDialog = new AlartTwoButtonDialog(this, new AlartTwoButtonDialog.TwoButtonDialogListener() {
            @Override
            public void oneButtonClick() {

                if (mPageIndex == 0 && type == PREPAGE) {
                    mViewPager.setCurrentItem(mPageIndex, true);
                    mMakeLrcFragment.resetData();
                } else if (mPageIndex == 1 && type == PREPAGE) {
                    mViewPager.setCurrentItem(mPageIndex, true);
                    mPreviewLrcFragment.resetData();
                } else if (type == CLOSE) {
                    finish();
                }

            }

            @Override
            public void twoButtonClick() {

            }
        });

    }

    @Override
    protected void loadData(boolean isRestoreInstance) {
        mAudioFilePath = getIntent().getStringExtra("audioFilePath");
        String lrcFilePath = getIntent().getStringExtra("lrcFilePath");
        mReloadLrcData = getIntent().getBooleanExtra("reloadLrcData", false);
        mHash = getIntent().getStringExtra("hash");

        //设置音频和歌词路径
        mEditLrcTextFragment.setAudioFilePath(mAudioFilePath);
        mEditLrcTextFragment.setLrcFilePath(lrcFilePath);
    }

    @Override
    protected boolean isAddStatusBar() {
        return false;
    }

    @Override
    public int setStatusBarParentView() {
        return 0;
    }

    @Override
    public void onBackPressed() {

    }

    /**
     *
     */
    public interface MakeLrcFragmentEvent {
        void close();

        void nextPage(int index);

        void prePage(int index);

        void openPreView(LyricsInfo lyricsInfo);

        void saveLrcData(LyricsInfo lyricsInfo);
    }
}
