package com.zlm.hp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zlm.hp.adapter.FileManagerAdapter;
import com.zlm.hp.model.FileInfo;
import com.zlm.hp.model.StorageInfo;
import com.zlm.hp.utils.AsyncTaskUtil;
import com.zlm.hp.utils.StorageListUtil;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.libs.widget.SwipeBackLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Description: 文件管理器
 * @author: zhangliangming
 * @date: 2018-05-22 21:55
 **/
public class FileManagerActivity extends BaseActivity {
    /**
     *
     */
    private SwipeBackLayout mSwipeBackLayout;

    /**
     * 列表视图
     */
    private RecyclerView mRecyclerView;

    /**
     * 文件
     */
    private ArrayList<FileInfo> mDatas;
    private FileManagerAdapter mAdapter;
    /**
     *
     */
    private AsyncTaskUtil mAsyncTaskUtil;

    /**
     * 文件夹路径
     */
    private TextView mFileDirectoryPathTv;
    private List<String> mFilePathList;
    private IconfontImageButtonTextView mFileBack;

    /**
     * 选择文件按钮
     */
    private Button mSelectedFileBtn;

    @Override
    protected int setContentViewId() {
        return R.layout.activity_file_manager;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mSwipeBackLayout = findViewById(R.id.swipeback_layout);
        mSwipeBackLayout.setSwipeBackLayoutListener(new SwipeBackLayout.SwipeBackLayoutListener() {

            @Override
            public void finishActivity() {
                finish();
                overridePendingTransition(0, 0);
            }
        });

        TextView titleView = findViewById(R.id.title);
        titleView.setText("选择文件");

        //返回
        RelativeLayout backImg = findViewById(R.id.backImg);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwipeBackLayout.closeView();

            }
        });

        //
        mRecyclerView = findViewById(R.id.file_recyclerView);
        //初始化内容视图
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mDatas = new ArrayList<FileInfo>();
        mAdapter = new FileManagerAdapter(getApplicationContext(), mDatas);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setItemEvent(new FileManagerAdapter.ItemEvent() {
            @Override
            public void fileDirectoryClick(String filePath) {
                mFilePathList.add(mFileDirectoryPathTv.getText().toString());
                loadFileDirectoryData(filePath);
            }
        });

        //
        mFilePathList = new ArrayList<String>();
        mFileDirectoryPathTv = findViewById(R.id.file_directory_path);
        //返回上一级文件
        mFileBack = findViewById(R.id.file_back);
        mFileBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFilePathList.size() == 0) {
                    loadFileData();
                    return;
                }
                String filePath = mFilePathList.get(mFilePathList.size() - 1);
                mFilePathList.remove(mFilePathList.size() - 1);
                if (TextUtils.isEmpty(filePath)) {
                    loadFileData();
                } else {
                    loadFileDirectoryData(filePath);
                }
            }
        });

        //
        mSelectedFileBtn = findViewById(R.id.selectFile);
        mSelectedFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectFilePath = mAdapter.getSelectFilePath();
                if (TextUtils.isEmpty(selectFilePath)) {
                    Toast.makeText(getApplicationContext(), "请选择文件!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent in = new Intent();
                    in.putExtra("selectFilePath", selectFilePath);
                    setResult(Activity.RESULT_OK, in);
                    mSwipeBackLayout.closeView();
                }
            }
        });

        loadFileData();
    }

    /**
     * 加载文件夹数据
     *
     * @param filePath
     */
    private void loadFileDirectoryData(final String filePath) {
        mAdapter.setSelectFilePath("");
        mAsyncTaskUtil = new AsyncTaskUtil();
        mAsyncTaskUtil.setAsyncTaskListener(new AsyncTaskUtil.AsyncTaskListener() {
            @Override
            public void doInBackground() {
                mDatas.clear();
                File file = new File(filePath);
                File[] files = file.listFiles();
                //文件名排序
                Collections.sort(Arrays.asList(files), new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        if (o1.isDirectory() && o2.isFile())
                            return -1;
                        if (o1.isFile() && o2.isDirectory())
                            return 1;
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                //
                for (int i = 0; i < files.length; i++) {
                    File tempFile = files[i];
                    FileInfo fileInfo = new FileInfo();
                    if (tempFile.isDirectory()) {
                        fileInfo.setFile(false);
                    } else {
                        fileInfo.setFile(true);
                    }

                    String filePath = tempFile.getPath();
                    fileInfo.setFilePath(filePath);
                    fileInfo.setFileName(filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.length()));
                    mDatas.add(fileInfo);
                }


            }

            @Override
            public void onPostExecute() {

                mAdapter.notifyDataSetChanged();
                mFileDirectoryPathTv.setText(filePath);

            }
        });
        mAsyncTaskUtil.execute("");
    }

    @Override
    protected void loadData(boolean isRestoreInstance) {

    }

    /**
     * 加载文件数据
     */
    private void loadFileData() {

        mAsyncTaskUtil = new AsyncTaskUtil();
        mAsyncTaskUtil.setAsyncTaskListener(new AsyncTaskUtil.AsyncTaskListener() {
            @Override
            public void doInBackground() {
                mDatas.clear();
                List<StorageInfo> list = StorageListUtil
                        .listAvaliableStorage(getApplicationContext());
                if (list == null || list.size() == 0) {

                } else {
                    for (int i = 0; i < list.size(); i++) {
                        StorageInfo storageInfo = list.get(i);
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.setFile(false);
                        String filePath = storageInfo.path;
                        fileInfo.setFilePath(filePath);
                        fileInfo.setFileName(filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath.length()));
                        mDatas.add(fileInfo);
                    }
                }

            }

            @Override
            public void onPostExecute() {

                mAdapter.notifyDataSetChanged();
                mFileDirectoryPathTv.setText("");

            }
        });
        mAsyncTaskUtil.execute("");
    }

    @Override
    protected boolean isAddStatusBar() {
        return true;
    }

    @Override
    public int setStatusBarParentView() {
        return R.id.filemanagerlayout;
    }

    @Override
    public void onBackPressed() {
        mSwipeBackLayout.closeView();
    }

    @Override
    public void onDestroy() {
        if (mAsyncTaskUtil != null)
            mAsyncTaskUtil.cancel(true);
        super.onDestroy();
    }
}
