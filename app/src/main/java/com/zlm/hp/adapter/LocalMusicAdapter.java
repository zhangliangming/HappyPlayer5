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
import com.zlm.hp.libs.utils.ToastUtil;
import com.zlm.hp.manager.AudioPlayerManager;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.model.AudioMessage;
import com.zlm.hp.model.Category;
import com.zlm.hp.receiver.AudioBroadcastReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.widget.IconfontImageButtonTextView;
import com.zlm.hp.widget.ListItemRelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地音乐
 * Created by zhangliangming on 2017/7/29.
 */
public class LocalMusicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * 标题
     */
    public final static int CATEGORYTITLE = 0;
    /**
     * item
     */
    public final static int ITEM = 1;

    /**
     * 底部
     */
    private static final int FOOTER = 2;

    /**
     * 版权
     */
    public final static int COPYRIGHT = 3;

    private Context mContext;
    private ArrayList<Category> mDatas;

    /**
     * 播放歌曲索引
     */
    private int playIndexPosition = -1;
    private String playIndexHash = "-1";
    private HPApplication mHPApplication;


    private CallBack mCallBack;
    /////////////////////////////////////////
    /**
     * 菜单打开索引
     */
    private int mMenuOpenIndex = -1;

    public LocalMusicAdapter(HPApplication hPApplication, Context context, ArrayList<Category> datas) {
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
        } else if (viewType == COPYRIGHT) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_copyright, null, false);
            CopyrightViewHolder holder = new CopyrightViewHolder(view);
            return holder;
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_localsong, null, false);
            LocalMusicViewHolder holder = new LocalMusicViewHolder(view);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof CategoryTitleViewHolder) {
            CategoryTitleViewHolder categoryViewHolder = (CategoryTitleViewHolder) viewHolder;
            String mCategoryName = (String) getItem(position);
            if (mCategoryName.equals("^")) {
                mCategoryName = "#";
            }
            categoryViewHolder.getCategoryTextTextView().setText(mCategoryName);
        } else if (viewHolder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) viewHolder;
            int size = getCategoryItemCount();
            footerViewHolder.getFooterTextView().setText("共有" + size + "首歌曲");

        } else if (viewHolder instanceof LocalMusicViewHolder) {
            AudioInfo audioInfo = (AudioInfo) getItem(position);
            reshViewHolder(position, (LocalMusicViewHolder) viewHolder, audioInfo);
        }
    }

    /**
     * 刷新ui
     *
     * @param position
     * @param viewHolder
     * @param audioInfo
     */
    private void reshViewHolder(final int position, final LocalMusicViewHolder viewHolder, final AudioInfo audioInfo) {
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
            viewHolder.getDeleteImgBtn().setVisibility(View.VISIBLE);
            viewHolder.getDeleteImgBtn().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AudioInfoDB.getAudioInfoDB(mContext).delete(audioInfo.getHash());
                    if (playIndexPosition == position) {
                        //发送空数据广播
                        Intent nullIntent = new Intent(AudioBroadcastReceiver.ACTION_NULLMUSIC);
                        nullIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        mContext.sendBroadcast(nullIntent);
                    }

                    //发送更新本地歌曲总数广播
                    Intent updateIntent = new Intent(AudioBroadcastReceiver.ACTION_LOCALUPDATE);
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


                //设置当前播放列表
                List<AudioInfo> data = AudioInfoDB.getAudioInfoDB(mContext).getAllLocalAudio();
                mHPApplication.setCurAudioInfos(data);


                //
                playIndexPosition = position;
                playIndexHash = audioInfo.getHash();
                mHPApplication.setPlayIndexHashID(playIndexHash);

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
     * 根据索引获取内容
     *
     * @param position
     * @return
     */
    private Object getItem(int position) {
        // 异常情况处理
        if (null == mDatas || position < 0 || position == getItemCount() - 1) {
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
        // 添加了底部的菜单，所以加多一个item和版权声明
        return count + 2;

    }

    @Override
    public int getItemViewType(int position) {
        // 异常情况处理
        if (null == mDatas || position < 0 || position == getItemCount() - 1) {
            return COPYRIGHT;
        }

        if (position == getItemCount() - 2)
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
        return ITEM;
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
    public int getPlayIndexPosition(AudioInfo audioInfo) {
        int count = 0;
        for (int i = 0; i < mDatas.size(); i++) {
            Category category = mDatas.get(i);
            List<Object> tempAudioInfos = category.getCategoryItem();
            int j = 0;
            for (; j < tempAudioInfos.size(); j++) {
                if (((AudioInfo) tempAudioInfos.get(j)).getHash().equals(audioInfo.getHash())) {
                    //标题分类所以要+1
                    return count + j + 1;
                }
            }
            count += category.getCount();
        }
        return -1;
    }

    class LocalMusicViewHolder extends RecyclerView.ViewHolder {
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

        public LocalMusicViewHolder(View view) {
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
