package base.download;

import android.content.Context;
import android.support.v4.util.ArrayMap;

import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.download.DownloadListener;
import com.yanzhenjie.nohttp.download.DownloadRequest;
import com.yanzhenjie.nohttp.error.NetworkError;
import com.yanzhenjie.nohttp.error.ServerError;
import com.yanzhenjie.nohttp.error.StorageReadWriteError;
import com.yanzhenjie.nohttp.error.StorageSpaceNotEnoughError;
import com.yanzhenjie.nohttp.error.TimeoutError;
import com.yanzhenjie.nohttp.error.URLError;
import com.yanzhenjie.nohttp.error.UnKnownHostError;
import com.zlm.hp.R;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import base.download.interfaces.IDownloadListener;
import base.download.nohttp.CallServer;

/**
 * Created by KathLine on 2018/2/1.
 */

public class DownloadManager {

    /**
     * 下载请求.
     */
    private Map<Integer, DownloadRequest> mDownloadRequests;
    private IDownloadListener mIDownloadListeners;
    private Context mContext;
    private Map<Integer, DownloadTask> mDownloadTasks;

    public DownloadManager(Context context, IDownloadListener IDownloadListener) {
        mContext = context;
        mDownloadRequests = new ArrayMap<>();
        mIDownloadListeners = IDownloadListener;
        mDownloadTasks = new ArrayMap<>();
    }

    public void startDownload(int what, DownloadTask task) {
        String fileFolder = task.getTaskPath();
        String fileName = null;
        Matcher m = Pattern.compile("\\.[A-Za-z0-9]{2,}$").matcher(fileFolder);
        if(m.find()) {
            String[] split = fileFolder.split("/");
            fileName = split[split.length - 1];
            fileFolder = fileFolder.substring(0, fileFolder.length()-fileName.length());
        }
        DownloadRequest downloadRequest = new DownloadRequest(task.getTaskUrl(), RequestMethod.GET,
                fileFolder, fileName, true, true);
        mDownloadRequests.put(what, downloadRequest);
        mDownloadTasks.put(what, task);
        if(mIDownloadListeners != null) {
            mIDownloadListeners.onBefore(what);
        }
        // what 区分下载。
        // downloadRequest 下载请求对象。
        // downloadListener 下载监听。
        CallServer.getInstance().download(what, downloadRequest, mDownloadListener);
    }

    public void stopDownload(int what){//没有暂停这一说法，本质是取消，然后重新下载
        DownloadRequest downloadRequest = mDownloadRequests.get(what);
        // 开始下载了，但是任务没有完成，代表正在下载，那么暂停下载。
        if (downloadRequest != null
                /*&& downloadRequest.isStarted() && !downloadRequest.isFinished()*/) {
            // 暂停下载。
            downloadRequest.cancel();
        }
    }

    public Map<Integer, DownloadTask> getDownloadTasks() {
        return mDownloadTasks;
    }

    /**
     * 下载监听
     */
    private DownloadListener mDownloadListener = new DownloadListener() {

        @Override
        public void onStart(int what, boolean isResume, long beforeLength, Headers headers, long allCount) {
            if(mIDownloadListeners != null) {
                mIDownloadListeners.onStart(what, isResume, beforeLength, headers, allCount);
            }
        }

        @Override
        public void onDownloadError(int what, Exception exception) {
            Logger.e(exception);

            String message = mContext.getString(R.string.download_error);
            String messageContent;
            if (exception instanceof ServerError) {
                messageContent = mContext.getString(R.string.download_error_server);
            } else if (exception instanceof NetworkError) {
                messageContent = mContext.getString(R.string.download_error_network);
            } else if (exception instanceof StorageReadWriteError) {
                messageContent = mContext.getString(R.string.download_error_storage);
            } else if (exception instanceof StorageSpaceNotEnoughError) {
                messageContent = mContext.getString(R.string.download_error_space);
            } else if (exception instanceof TimeoutError) {
                messageContent = mContext.getString(R.string.download_error_timeout);
            } else if (exception instanceof UnKnownHostError) {
                messageContent = mContext.getString(R.string.download_error_un_know_host);
            } else if (exception instanceof URLError) {
                messageContent = mContext.getString(R.string.download_error_url);
            } else {
                messageContent = mContext.getString(R.string.download_error_un);
            }
            message = String.format(Locale.getDefault(), message, messageContent);

            if(mIDownloadListeners != null) {
                mIDownloadListeners.onDownloadError(what, exception, message);
            }
        }

        @Override
        public void onProgress(int what, int progress, long fileCount, long speed) {
            updateProgress(progress, speed);
            if(mIDownloadListeners != null) {
                mIDownloadListeners.onProgress(what, progress, fileCount, speed);
            }
        }

        @Override
        public void onFinish(int what, String filePath) {
            Logger.d("Download finish, file path: " + filePath);
            if(mIDownloadListeners != null) {
                mIDownloadListeners.onFinish(what, filePath);
            }
        }

        @Override
        public void onCancel(int what) {
            if(mIDownloadListeners != null) {
                mIDownloadListeners.onCancel(what);
            }
        }

    };

    public String updateProgress(int progress, long speed) {
        double newSpeed = speed / 1024D;
        DecimalFormat decimalFormat = new DecimalFormat("###0.00");
        String sProgress = mContext.getString(R.string.download_progress);
        sProgress = String.format(Locale.getDefault(), sProgress, progress, decimalFormat.format(newSpeed));
        return sProgress;
    }

}
