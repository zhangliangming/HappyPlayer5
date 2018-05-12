package com.zlm.hp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.db.AudioInfoDB;
import com.zlm.hp.db.DownloadInfoDB;
import com.zlm.hp.db.DownloadThreadDB;
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.manager.DownloadAudioManager;
import com.zlm.hp.manager.OnLineAudioManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.ListItemRelativeLayout;

import java.util.ArrayList;

/**
 * 排行歌曲
 * Created by zhangliangming on 2017/7/31.
 */
public class RankSongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<AudioInfo> mDatas;

    /**
     * 有更多数据
     */
    public static final int HASMOREDATA = 0;
    /**
     * 无数据
     */
    public static final int NODATA = 1;
    /**
     * 已经到底了
     */
    public static final int NOMOREDATA = 2;
    /**
     * 版权声明
     */
    public static final int COPYRIGHT = 3;
    /**
     * 内容item
     */
    public static final int OTHER = 4;

    public static final int LOADING = 5;
    private int state = NODATA;

    /**
     * 加载更多
     */
    private RankSongListener mRankSongListener;

    /**
     * 播放歌曲索引
     */
    private int playIndexPosition = -1;
    private String playIndexHash = "-1";
    private HPApplication mHPApplication;

    /////////////////////////////////////////
    /**
     * 菜单打开索引
     */
    private int mMenuOpenIndex = -1;

    public RankSongAdapter(HPApplication hPApplication, Context context, ArrayList<AudioInfo> datas) {
        this.mHPApplication = hPApplication;
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public int getItemViewType(int position) {
        if (state == NODATA && mDatas.size() == position) {
            return NODATA;
        } else {
            if (mDatas.size() + 1 == position) {
                return COPYRIGHT;
            } else if (mDatas.size() == position) {
                return state;
            }
            return OTHER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = null;
        if (viewType == NODATA) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_nodata, null, false);
            NoDataViewHolder holder = new NoDataViewHolder(view);
            return holder;
        } else if (viewType == HASMOREDATA) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_hasmoredata, null, false);
            LoadMoreViewHolder holder = new LoadMoreViewHolder(view);
            return holder;
        } else if (viewType == NOMOREDATA) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_nomoredata, null, false);
            NoDataViewHolder holder = new NoDataViewHolder(view);
            return holder;
        } else if (viewType == COPYRIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_copyright, null, false);
            CopyrightViewHolder holder = new CopyrightViewHolder(view);
            return holder;
        } else if (viewType == LOADING) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_loadingdata, null, false);
            LoadingViewHolder holder = new LoadingViewHolder(view);
            return holder;
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_netsong, null, false);
            RankSongViewHolder holder = new RankSongViewHolder(view);
            return holder;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof RankSongViewHolder && position < mDatas.size()) {
            AudioInfo audioInfo = mDatas.get(position);
            reshViewHolder(position, (RankSongViewHolder) viewHolder, audioInfo);
        } else if (viewHolder instanceof LoadMoreViewHolder) {
            LoadMoreViewHolder tempHolder = (LoadMoreViewHolder) viewHolder;
            tempHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mRankSongListener != null) {
                        mRankSongListener.loadMoreData();
                    }
                }
            });
        }
    }

    /**
     * 刷新
     *
     * @param position
     * @param viewHolder
     * @param audioInfo
     */
    private void reshViewHolder(final int position, final RankSongViewHolder viewHolder, final AudioInfo audioInfo) {
        //设置菜单界面
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
            viewHolder.getDeleteImgBtn().setVisibility(View.GONE);
            //详情按钮
            viewHolder.getDetailImgBtn().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            if (audioInfo.getType() == AudioInfo.NET) {
                viewHolder.getDownloadParentRl().setVisibility(View.VISIBLE);
                //
                //下载
                if (DownloadInfoDB.getAudioInfoDB(mContext).isExists(audioInfo.getHash()) || AudioInfoDB.getAudioInfoDB(mContext).isNetAudioExists(audioInfo.getHash())) {

                    viewHolder.getDownloadedImg().setVisibility(View.VISIBLE);
                    viewHolder.getDownloadImg().setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.getDownloadedImg().setVisibility(View.INVISIBLE);
                    viewHolder.getDownloadImg().setVisibility(View.VISIBLE);
                }

            } else {
                viewHolder.getDownloadParentRl().setVisibility(View.GONE);
            }

            //下载按钮
            viewHolder.getDownloadImg().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DownloadAudioManager.getDownloadAudioManager(mHPApplication, mContext).addTask(audioInfo);
                    viewHolder.getDownloadedImg().setVisibility(View.VISIBLE);
                    viewHolder.getDownloadImg().setVisibility(View.INVISIBLE);
                }
            });

            //下载按钮
            viewHolder.getDownloadedImg().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DownloadAudioManager.getDownloadAudioManager(mHPApplication, mContext).addTask(audioInfo);
                }
            });


            //
            viewHolder.getMenuLinearLayout().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getMenuLinearLayout().setVisibility(View.GONE);
        }

        //判断是否已缓存到本地或者下载到本地
        if (AudioInfoDB.getAudioInfoDB(mContext).isNetAudioExists(audioInfo.getHash())) {
            viewHolder.getIslocalImg().setVisibility(View.VISIBLE);
        } else {
            int downloadSize = DownloadThreadDB.getDownloadThreadDB(mContext).getDownloadedSize(audioInfo.getHash(), OnLineAudioManager.threadNum);
            if (downloadSize >= audioInfo.getFileSize()) {
                viewHolder.getIslocalImg().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getIslocalImg().setVisibility(View.GONE);
            }
        }

        //显示歌曲索引
        viewHolder.getSongIndexTv().setText(((position + 1) < 10 ? "0" + (position + 1) : (position + 1) + ""));
        viewHolder.getSongIndexTv().setVisibility(View.VISIBLE);

        if (audioInfo.getHash().equals(mHPApplication.getPlayIndexHashID())) {
            playIndexPosition = position;
            playIndexHash = mHPApplication.getPlayIndexHashID();
            //
            viewHolder.getStatusView().setVisibility(View.VISIBLE);
        } else {
            viewHolder.getStatusView().setVisibility(View.INVISIBLE);
        }

        viewHolder.getSongNameTv().setText(audioInfo.getSongName());
        viewHolder.getSingerNameTv().setText(audioInfo.getSingerName());
        //item点击事件
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


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

                //设置界面ui
                viewHolder.getStatusView().setVisibility(View.VISIBLE);
                //
                if (playIndexPosition != -1) {
                    notifyItemChanged(playIndexPosition);
                }
                //
                mHPApplication.setCurAudioInfos(mDatas);
                //
                playIndexPosition = position;
                playIndexHash = audioInfo.getHash();
                mHPApplication.setPlayIndexHashID(playIndexHash);

                //发送播放广播
                Intent playIntent = new Intent(AudioBroadcastReceiver.ACTION_PLAYMUSIC);
                AudioMessage audioMessage = new AudioMessage();
                audioMessage.setAudioInfo(audioInfo);
                playIntent.putExtra(AudioMessage.KEY, audioMessage);
                playIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(playIntent);
            }
        });
    }


    /**
     * 刷新view
     *
     * @param audioInfo
     */
    public void reshViewHolder(AudioInfo audioInfo) {
        if (playIndexPosition != -1) {
            notifyItemChanged(playIndexPosition);
        }
        if (audioInfo == null) {
            playIndexPosition = -1;
            playIndexHash = "-1";
            return;
        }
        //;
        playIndexPosition = getPlayIndexPosition(audioInfo);
        if (playIndexPosition != -1) {
            playIndexHash = audioInfo.getHash();
            notifyItemChanged(playIndexPosition);
        }

    }

    /**
     * 获取当前播放索引
     *
     * @param audioInfo
     * @return
     */
    private int getPlayIndexPosition(AudioInfo audioInfo) {
        for (int i = 0; i < mDatas.size(); i++) {

            if (mDatas.get(i).getHash().equals(audioInfo.getHash())) {

                return i;
            }
        }

        return -1;
    }

    @Override
    public int getItemCount() {
        if (state == NODATA) return mDatas.size() + 1;
        return mDatas.size() + 2;
    }


    class RankSongViewHolder extends RecyclerView.ViewHolder {
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
         * 歌曲索引
         */
        private TextView songIndexTv;

        /**
         * 歌曲名称
         */
        private TextView songNameTv;

        /**
         * 歌手名称
         */
        private TextView singerNameTv;

        /**
         * 是否存在本地
         */
        private ImageView islocalImg;

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
         * 下载布局
         */
        private RelativeLayout downloadParentRl;

        /**
         * 下载完成按钮
         */
        private ImageView downloadedImg;
        /**
         * 下载按钮
         */
        private ImageView downloadImg;
        /**
         * 详情按钮
         */
        private IconfontImageButtonTextView detailImgBtn;

        /**
         * 删除按钮
         */
        private IconfontImageButtonTextView deleteImgBtn;

        public RankSongViewHolder(View view) {
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

        public ImageView getIslocalImg() {
            if (islocalImg == null) {
                islocalImg = view.findViewById(R.id.islocal);
            }
            return islocalImg;
        }

        public TextView getSongIndexTv() {

            if (songIndexTv == null) {
                songIndexTv = view.findViewById(R.id.songIndex);
            }
            return songIndexTv;
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

        public RelativeLayout getDownloadParentRl() {
            if (downloadParentRl == null) {
                downloadParentRl = view.findViewById(R.id.downloadParent);
            }
            return downloadParentRl;
        }

        public ImageView getDownloadedImg() {
            if (downloadedImg == null) {
                downloadedImg = view.findViewById(R.id.downloaded_menu);
            }
            return downloadedImg;
        }

        public ImageView getDownloadImg() {
            if (downloadImg == null) {
                downloadImg = view.findViewById(R.id.download_menu);
            }
            return downloadImg;
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

    /////////////////////////////////////////////////////


    public void setState(int state) {
        this.state = state;
    }

    public void setRankSongListener(RankSongListener mRankSongListener) {
        this.mRankSongListener = mRankSongListener;
    }

    public interface RankSongListener {
        void loadMoreData();
    }
}
