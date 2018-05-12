package com.zlm.hp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.db.DownloadInfoDB;
import com.zlm.hp.db.DownloadThreadDB;
import com.zlm.hp.libs.download.constant.DownloadTaskConstant;
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.DownloadAudioManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.model.Category;
import com.zlm.hp.model.DownloadInfo;
import com.zlm.hp.model.DownloadMessage;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.receiver.DownloadAudioReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.utils.FileUtils;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.IconfontTextView;
import com.zlm.hp.widget.ListItemRelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 下载音乐
 * Created by zhangliangming on 2017/9/9.
 */
public class DownloadMusicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * 标题
     */
    public final static int CATEGORYTITLE = 0;
    /**
     * item正在下载
     */
    public final static int ITEMDownloading = 1;
    /**
     * item已经下载
     */
    public final static int ITEMDownloaded = 2;

    /**
     * 底部
     */
    private static final int FOOTER = 3;

    private Context mContext;
    private ArrayList<Category> mDatas;
    private HPApplication mHPApplication;


    /**
     * 播放歌曲索引
     */
    private int playIndexPosition = -1;
    private String playIndexHash = "-1";

    private CallBack mCallBack;

    public DownloadMusicAdapter(HPApplication hPApplication, Context context, ArrayList<Category> datas) {
        this.mHPApplication = hPApplication;
        this.mContext = context;
        this.mDatas = datas;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = null;
        if (viewType == FOOTER) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_local_footer, null, false);
            FooterViewHolder holder = new FooterViewHolder(view);
            return holder;
        } else if (viewType == CATEGORYTITLE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_local_title, null, false);
            CategoryTitleViewHolder holder = new CategoryTitleViewHolder(view);
            return holder;
        } else if (viewType == ITEMDownloading) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_downloading, null, false);
            return new DownloadingMusicViewHolder(view);

        } else if (viewType == ITEMDownloaded) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_localsong, null, false);

            return new DownloadedMusicViewHolder(view);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof CategoryTitleViewHolder) {
            CategoryTitleViewHolder categoryViewHolder = (CategoryTitleViewHolder) viewHolder;
            String mCategoryName = (String) getItem(position);

            categoryViewHolder.getCategoryTextTextView().setText(
                    mCategoryName + "("
                            + getItemSizeByCategoryName(mCategoryName) + ")");
        } else if (viewHolder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) viewHolder;
            int size = getCategoryItemCount();
            footerViewHolder.getFooterTextView().setText("共有" + size + "首歌曲");

        } else if (viewHolder instanceof DownloadingMusicViewHolder) {
            DownloadInfo downloadInfo = (DownloadInfo) getItem(position);
            reshViewHolder(position, (DownloadingMusicViewHolder) viewHolder, downloadInfo);
        } else if (viewHolder instanceof DownloadedMusicViewHolder) {
            DownloadInfo downloadInfo = (DownloadInfo) getItem(position);
            reshViewHolder(position, (DownloadedMusicViewHolder) viewHolder, downloadInfo);
        }


    }

    /**
     * 获取有效item的个数
     *
     * @return
     */
    private int getCategoryItemCount() {
        int count = 0;

        if (null != mDatas) {
            // 所有分类中item的总和是ListVIew Item的总个数
            for (Category category : mDatas) {
                count += category.getItemCount();
            }
        }
        // 添加了底部的菜单，所以加多一个item
        return count;
    }

    /**
     * 通过种类名称来获取该分类下的歌曲数目
     *
     * @param mCategoryName
     * @return
     */
    private int getItemSizeByCategoryName(String mCategoryName) {
        int count = 0;

        if (null != mDatas) {
            // 所有分类中item的总和是ListVIew Item的总个数
            for (Category category : mDatas) {
                if (category.getCategoryName().equals(mCategoryName)) {
                    count = category.getItemCount();
                    break;
                }
            }
        }
        return count;
    }

    /**
     * 下载中刷新ui
     *
     * @param position
     * @param viewHolder
     * @param downloadInfo
     */
    private void reshViewHolder(final int position, final DownloadingMusicViewHolder viewHolder, final DownloadInfo downloadInfo) {
        final AudioInfo audioInfo = downloadInfo.getAudioInfo();

        viewHolder.getTitleTv().setText(audioInfo.getSingerName() + " - " + audioInfo.getSongName());

        int status = DownloadAudioManager.getDownloadAudioManager(mHPApplication, mContext).taskIsDLStatus(downloadInfo.getDHash());
        if (status == DownloadTaskConstant.WAIT.getValue()) {
            viewHolder.getDownloadingImg().setVisibility(View.VISIBLE);
            viewHolder.getDownloadPauseImg().setVisibility(View.INVISIBLE);

            viewHolder.getOpTipTv().setText("等待下载");


        } else if (status == DownloadTaskConstant.DOWNLOADING.getValue()) {
            viewHolder.getDownloadingImg().setVisibility(View.INVISIBLE);
            viewHolder.getDownloadPauseImg().setVisibility(View.VISIBLE);

            viewHolder.getOpTipTv().setText("点击暂停");

        } else {
            viewHolder.getDownloadingImg().setVisibility(View.VISIBLE);
            viewHolder.getDownloadPauseImg().setVisibility(View.INVISIBLE);
            viewHolder.getOpTipTv().setText("点击继续下载");
        }

        //
        String fileSizeText = FileUtils.getFileSize(audioInfo.getFileSize());
        int downloadedSize = DownloadThreadDB.getDownloadThreadDB(mContext).getDownloadedSize(downloadInfo.getDHash(), DownloadAudioManager.threadNum);
        String downloadSizeText = FileUtils.getFileSize(downloadedSize);
        viewHolder.getDlTipTv().setText(downloadSizeText + "/" + fileSizeText);

        //列表点击事件
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int status = DownloadAudioManager.getDownloadAudioManager(mHPApplication, mContext).taskIsDLStatus(downloadInfo.getDHash());
                if (status == DownloadTaskConstant.INT.getValue()) {

                    DownloadAudioManager.getDownloadAudioManager(mHPApplication, mContext).addTask(downloadInfo.getAudioInfo());

                } else if (status == DownloadTaskConstant.WAIT.getValue()) {

                    DownloadAudioManager.getDownloadAudioManager(mHPApplication, mContext).cancelTask(downloadInfo.getDHash());

                } else if (status == DownloadTaskConstant.DOWNLOADING.getValue()) {
                    DownloadAudioManager.getDownloadAudioManager(mHPApplication, mContext).pauseTask(downloadInfo.getDHash());
                }
            }
        });

        //删除
        viewHolder.getDeleteTv().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DownloadAudioManager.getDownloadAudioManager(mHPApplication, mContext).taskIsExists(downloadInfo.getDHash())) {
                    DownloadAudioManager.getDownloadAudioManager(mHPApplication, mContext).cancelTask(downloadInfo.getDHash());
                } else {

                    //更新
                    DownloadInfoDB.getAudioInfoDB(mContext).delete(downloadInfo.getDHash());

                    DownloadMessage downloadMessage = new DownloadMessage();
                    downloadMessage.setTaskHash(downloadInfo.getDHash());
                    downloadMessage.setTaskId(downloadInfo.getDHash());

                    //发送取消广播
                    Intent cancelIntent = new Intent(DownloadAudioReceiver.ACTION_DOWMLOADMUSICDOWNCANCEL);
                    cancelIntent.putExtra(DownloadMessage.KEY, downloadMessage);
                    cancelIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mContext.sendBroadcast(cancelIntent);

                    //发送更新下载歌曲总数广播
                    Intent updateIntent = new Intent(AudioBroadcastReceiver.ACTION_DOWNLOADUPDATE);
                    updateIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mContext.sendBroadcast(updateIntent);
                }

            }
        });
    }

    /**
     * 菜单打开索引
     */
    private int mMenuOpenIndex = -1;

    /**
     * 下载完成刷新ui
     *
     * @param position
     * @param viewHolder
     * @param downloadInfo
     */
    private void reshViewHolder(final int position, final DownloadedMusicViewHolder viewHolder, final DownloadInfo downloadInfo) {
        final AudioInfo audioInfo = downloadInfo.getAudioInfo();
        //1更多按钮点击事件
        viewHolder.getItemMoreImg().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position != mMenuOpenIndex) {
                    if (mMenuOpenIndex != -1) {
                        notifyItemChanged(mMenuOpenIndex);
                    }
                    mMenuOpenIndex = position;
                    notifyItemChanged(mMenuOpenIndex);
                } else {
                    if (mMenuOpenIndex != -1) {
                        notifyItemChanged(mMenuOpenIndex);
                        mMenuOpenIndex = -1;
                    }
                }
            }
        });
        //2展开或者隐藏菜单
        if (position == mMenuOpenIndex) {

            //判断是否是喜欢歌曲
            boolean isLike = AudioInfoDB.getAudioInfoDB(mContext).isRecentOrLikeExists(audioInfo.getHash(), audioInfo.getType(), false);
            if (isLike) {
                viewHolder.getLikedImgBtn().setVisibility(View.VISIBLE);
                viewHolder.getUnLikeImgBtn().setVisibility(View.GONE);
            } else {
                viewHolder.getLikedImgBtn().setVisibility(View.GONE);
                viewHolder.getUnLikeImgBtn().setVisibility(View.VISIBLE);
            }

            //喜欢点击事件
            viewHolder.getLikedImgBtn().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.getLikedImgBtn().setVisibility(View.GONE);
                    viewHolder.getUnLikeImgBtn().setVisibility(View.VISIBLE);
                    ToastUtil.showTextToast(mContext, "取消成功");

                    AudioInfoDB.getAudioInfoDB(mContext).deleteRecentOrLikeAudio(audioInfo.getHash(), audioInfo.getType(), false);


                    //删除喜欢歌曲
                    Intent delIntent = new Intent(AudioBroadcastReceiver.ACTION_LIKEDELETE);
                    delIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mContext.sendBroadcast(delIntent);
                }
            });
            //喜欢取消事件
            viewHolder.getUnLikeImgBtn().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewHolder.getLikedImgBtn().setVisibility(View.VISIBLE);
                    viewHolder.getUnLikeImgBtn().setVisibility(View.GONE);
                    ToastUtil.showTextToast(mContext, "已添加收藏");

                    //添加喜欢歌曲
                    Intent addIntent = new Intent(AudioBroadcastReceiver.ACTION_LIKEADD);
                    addIntent.putExtra(AudioInfo.KEY, audioInfo);
                    addIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mContext.sendBroadcast(addIntent);
                }
            });
            //删除按钮
            viewHolder.getDeleteImgBtn().setVisibility(View.VISIBLE);
            viewHolder.getDeleteImgBtn().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DownloadInfoDB.getAudioInfoDB(mContext).delete(audioInfo.getHash());
                    if (playIndexPosition == position) {
                        //发送空数据广播
                        Intent nullIntent = new Intent(AudioBroadcastReceiver.ACTION_NULLMUSIC);
                        nullIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        mContext.sendBroadcast(nullIntent);
                    }

                    //发送更新下载歌曲总数广播
                    Intent updateIntent = new Intent(AudioBroadcastReceiver.ACTION_DOWNLOADUPDATE);
                    updateIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    mContext.sendBroadcast(updateIntent);

                    //
                    if (mMenuOpenIndex != -1) {
                        mMenuOpenIndex = -1;
                    }

                    //更新界面
                    if (mCallBack != null) {
                        mCallBack.delete();
                    }
                }
            });

            //详情按钮
            viewHolder.getDetailImgBtn().setVisibility(View.GONE);
            viewHolder.getDetailImgBtn().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            //
            viewHolder.getMenuLinearLayout().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getMenuLinearLayout().setVisibility(View.GONE);
        }

        if (audioInfo.getHash().equals(mHPApplication.getPlayIndexHashID())) {
            playIndexPosition = position;
            playIndexHash = mHPApplication.getPlayIndexHashID();
            //
            viewHolder.getStatusView().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getStatusView().setVisibility(View.INVISIBLE);
        }

        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mMenuOpenIndex != -1) {
                    notifyItemChanged(mMenuOpenIndex);
                    mMenuOpenIndex = -1;
                }


                if (playIndexPosition == position) {
                    if (mHPApplication.getPlayStatus() == AudioPlayerManager.PLAYING) {
                        // 当前正在播放，发送暂停

                        Intent pauseIntent = new Intent(AudioBroadcastReceiver.ACTION_PAUSEMUSIC);
                        pauseIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        mContext.sendBroadcast(pauseIntent);

                        return;
                    } else if (mHPApplication.getPlayStatus() == AudioPlayerManager.PAUSE) {
                        //当前正在暂停，发送唤醒播放

                        Intent remuseIntent = new Intent(AudioBroadcastReceiver.ACTION_RESUMEMUSIC);
                        remuseIntent.putExtra(AudioMessage.KEY, mHPApplication.getCurAudioMessage());
                        remuseIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        mContext.sendBroadcast(remuseIntent);

                        return;
                    }
                }

                //
                playIndexHash = audioInfo.getHash();
                mHPApplication.setPlayIndexHashID(playIndexHash);

                //设置界面ui
                viewHolder.getStatusView().setVisibility(View.VISIBLE);
                //
                if (playIndexPosition != -1) {
                    notifyItemChanged(playIndexPosition);
                }
                playIndexPosition = position;

                //设置当前播放列表
                List<AudioInfo> curData = new ArrayList<AudioInfo>();
                List<Object> data = mDatas.get(1).getCategoryItem();
                for (int i = 0; i < data.size(); i++) {

                    DownloadInfo downloadInfo1 = (DownloadInfo) data.get(i);
                    curData.add(downloadInfo1.getAudioInfo());
                }
                mHPApplication.setCurAudioInfos(curData);


                Intent playIntent = new Intent(AudioBroadcastReceiver.ACTION_PLAYMUSIC);
                AudioMessage audioMessage = new AudioMessage();
                audioMessage.setAudioInfo(audioInfo);
                playIntent.putExtra(AudioMessage.KEY, audioMessage);
                playIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(playIntent);

            }
        });
        viewHolder.getSingerNameTv().setText(audioInfo.getSingerName());
        viewHolder.getSongNameTv().setText(audioInfo.getSongName());
    }

    /**
     * 刷新view
     *
     * @param hash
     */
    public void reshViewHolder(String hash) {
        if (hash == null || hash.equals("")) {
            return;
        }
        int index = getPlayIndexPosition(hash);
        if (index != -1) {
            notifyItemChanged(index);
        }
    }

    /**
     * 重置数据
     */
    public void resetData() {
        playIndexPosition = -1;
        playIndexHash = "-1";
    }

    /**
     * 刷新view
     *
     * @param audioInfo
     */
    public void reshViewHolderView(AudioInfo audioInfo) {
        if (playIndexPosition != -1) {
            notifyItemChanged(playIndexPosition);
        }
        if (audioInfo == null) {
            playIndexPosition = -1;
            playIndexHash = "-1";
            return;
        }
        //;
        playIndexPosition = getPlayIndexPosition(audioInfo.getHash());
        if (playIndexPosition != -1) {
            playIndexHash = audioInfo.getHash();
            notifyItemChanged(playIndexPosition);
        }

    }

    /**
     * 通过sid获取当前的播放索引
     *
     * @param hash
     * @return
     */
    private int getPlayIndexPosition(String hash) {
        int index = -1;
        // 异常情况处理
        if (null == mDatas) {
            return -1;
        }

        int count = 0;
        for (int i = 0; i < mDatas.size(); i++) {
            Category category = mDatas.get(i);
            List<Object> downloadInfoList = category.getCategoryItem();
            int j = 0;
            for (; j < downloadInfoList.size(); j++) {
                DownloadInfo downloadInfo = (DownloadInfo) downloadInfoList.get(j);


                if (downloadInfo.getAudioInfo().getHash().equals(hash)) {

                    index = count + j + 1;

                    break;
                }
            }
            count += category.getCount();
        }

        return index;
    }


    /**
     * 根据索引获取内容
     *
     * @param position
     * @return
     */
    private Object getItem(int position) {

        // 异常情况处理
        if (null == mDatas || position < 0 || position > getItemCount()) {
            return null;
        }


        // 同一分类内，第一个元素的索引值
        int categroyFirstIndex = 0;

        for (Category category : mDatas) {
            int size = category.getCount();
            // 在当前分类中的索引值
            int categoryIndex = position - categroyFirstIndex;
            // item在当前分类内
            if (categoryIndex < size) {
                return category.getItem(categoryIndex);
            }
            // 索引移动到当前分类结尾，即下一个分类第一个元素索引
            categroyFirstIndex += size;
        }

        return null;
    }


    @Override
    public int getItemCount() {
        int count = 0;

        if (null != mDatas) {
            // 所有分类中item的总和是ListVIew Item的总个数
            for (Category category : mDatas) {
                count += category.getCount();
            }
        }
        return count + 1;
    }


    @Override
    public int getItemViewType(int position) {
        // 异常情况处理
        if (null == mDatas || position < 0 || position > getItemCount()) {
            return CATEGORYTITLE;
        }
        if (position == getItemCount() - 1)
            return FOOTER;


        int categroyFirstIndex = 0;

        for (Category category : mDatas) {
            int size = category.getCount();
            // 在当前分类中的索引值
            int categoryIndex = position - categroyFirstIndex;
            if (categoryIndex == 0) {
                return CATEGORYTITLE;
            } else if (categoryIndex < size) {
                break;
            }
            categroyFirstIndex += size;
        }

        if (getItem(position) instanceof String) {
            return CATEGORYTITLE;
        }
        DownloadInfo downloadInfo = (DownloadInfo) getItem(position);
        if (downloadInfo.getAudioInfo().getStatus() == AudioInfo.FINISH) {
            return ITEMDownloaded;
        }
        return ITEMDownloading;
    }

    class CategoryTitleViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView categoryTextTextView;
        private View lineView;

        public CategoryTitleViewHolder(View view) {
            super(view);
            this.itemView = view;
        }

        public TextView getCategoryTextTextView() {
            if (categoryTextTextView == null) {
                categoryTextTextView = itemView
                        .findViewById(R.id.category_text);
            }
            return categoryTextTextView;
        }

    }


    /**
     * 下载中
     */
    class DownloadingMusicViewHolder extends RecyclerView.ViewHolder {
        private View view;
        /**
         * item底部布局
         */
        private ListItemRelativeLayout listItemRelativeLayout;
        /**
         * 下载中
         */
        private IconfontTextView downloadingImg;

        /**
         * 下载暂停
         */
        private IconfontTextView downloadPauseImg;

        /**
         * 标题
         */
        private TextView titleTv;
        /**
         * 状态提示
         */
        private TextView opTipTv;
        /**
         * 下载提示
         */
        private TextView dlTipTv;
        /**
         * 删除
         */
        private IconfontTextView deleteTv;

        public DownloadingMusicViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public ListItemRelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }


        public IconfontTextView getDownloadingImg() {
            if (downloadingImg == null) {
                downloadingImg = view.findViewById(R.id.download_img);
            }
            return downloadingImg;
        }

        public IconfontTextView getDownloadPauseImg() {
            if (downloadPauseImg == null) {
                downloadPauseImg = view.findViewById(R.id.pause_img);
            }
            return downloadPauseImg;
        }

        public TextView getTitleTv() {
            if (titleTv == null) {
                titleTv = view.findViewById(R.id.titleName);
            }
            return titleTv;
        }

        public TextView getOpTipTv() {
            if (opTipTv == null) {
                opTipTv = view.findViewById(R.id.download_tip);
            }
            return opTipTv;
        }

        public TextView getDlTipTv() {
            if (dlTipTv == null) {
                dlTipTv = view.findViewById(R.id.downloadSizeText);
            }
            return dlTipTv;
        }

        public IconfontTextView getDeleteTv() {
            if (deleteTv == null) {
                deleteTv = view.findViewById(R.id.delete);
            }
            return deleteTv;
        }
    }

    /**
     * 下载完成
     */


    class DownloadedMusicViewHolder extends RecyclerView.ViewHolder {
        private View view;
        /**
         * item底部布局
         */
        private ListItemRelativeLayout listItemRelativeLayout;

        /**
         * 更多按钮
         */
        private ImageView moreImg;
        /**
         * 状态标记view
         */
        private View statusView;

        /**
         * 歌曲名称
         */
        private TextView songNameTv;

        /**
         * 歌手名称
         */
        private TextView singerNameTv;

        //、、、、、、、、、、、、、、、、、、、、更多菜单、、、、、、、、、、、、、、、、、、、、、、、、

        /**
         * 更多按钮
         */
        private ImageView itemMoreImg;

        /**
         * 菜单
         */
        private LinearLayout menuLinearLayout;
        /**
         * 不喜欢按钮
         */
        private IconfontImageButtonTextView unLikeImgBtn;
        /**
         * 不喜欢按钮
         */
        private IconfontImageButtonTextView likedImgBtn;

        /**
         * 详情按钮
         */
        private IconfontImageButtonTextView detailImgBtn;

        /**
         * 删除按钮
         */
        private IconfontImageButtonTextView deleteImgBtn;

        public DownloadedMusicViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public ListItemRelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }

        public ImageView getMoreImg() {
            if (moreImg == null) {
                moreImg = view.findViewById(R.id.item_more);
            }
            return moreImg;
        }

        public View getStatusView() {
            if (statusView == null) {
                statusView = view.findViewById(R.id.status);
            }
            return statusView;
        }

        public TextView getSongNameTv() {
            if (songNameTv == null) {
                songNameTv = view.findViewById(R.id.songName);
            }
            return songNameTv;
        }

        public TextView getSingerNameTv() {
            if (singerNameTv == null) {
                singerNameTv = view.findViewById(R.id.singerName);
            }
            return singerNameTv;
        }

        public ImageView getItemMoreImg() {
            if (itemMoreImg == null) {
                itemMoreImg = view.findViewById(R.id.item_more);
            }
            return itemMoreImg;
        }

        public LinearLayout getMenuLinearLayout() {
            if (menuLinearLayout == null) {
                menuLinearLayout = view.findViewById(R.id.menu);
            }
            return menuLinearLayout;
        }

        public IconfontImageButtonTextView getLikedImgBtn() {
            if (likedImgBtn == null) {
                likedImgBtn = view.findViewById(R.id.liked_menu);
            }
            likedImgBtn.setConvert(true);
            return likedImgBtn;
        }

        public IconfontImageButtonTextView getUnLikeImgBtn() {
            if (unLikeImgBtn == null) {
                unLikeImgBtn = view.findViewById(R.id.unlike_menu);
            }
            unLikeImgBtn.setConvert(true);
            return unLikeImgBtn;
        }

        public IconfontImageButtonTextView getDetailImgBtn() {
            if (detailImgBtn == null) {
                detailImgBtn = view.findViewById(R.id.detail_menu);
            }
            detailImgBtn.setConvert(true);
            return detailImgBtn;
        }

        public IconfontImageButtonTextView getDeleteImgBtn() {
            if (deleteImgBtn == null) {
                deleteImgBtn = view.findViewById(R.id.delete_menu);
            }
            deleteImgBtn.setConvert(true);
            return deleteImgBtn;
        }

    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView footerTextView;

        public FooterViewHolder(View view) {
            super(view);
            this.itemView = view;
        }

        public TextView getFooterTextView() {
            if (footerTextView == null) {
                footerTextView = itemView
                        .findViewById(R.id.list_size_text);
            }
            return footerTextView;
        }
    }

    public void setCallBack(CallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    public interface CallBack {
        void delete();
    }
}
