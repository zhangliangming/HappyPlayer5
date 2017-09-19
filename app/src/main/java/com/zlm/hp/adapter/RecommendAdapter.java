package com.zlm.hp.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.net.entity.RankListResult;
import com.zlm.hp.receiver.FragmentReceiver;
import com.zlm.hp.ui.R;
import com.zlm.hp.utils.ImageUtil;
import com.zlm.hp.utils.ResourceFileUtil;
import com.zlm.hp.widget.ListItemRelativeLayout;

import java.io.File;
import java.util.ArrayList;

/**
 * 排行
 * Created by zhangliangming on 2017/7/29.
 */
public class RecommendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int FOOTER = 0;
    private final int OTHER = 1;

    /**
     * 无数据
     */
    public static final int NODATA = 2;

    /**
     * 已经到底了
     */
    public static final int NOMOREDATA = 3;

    private Context mContext;
    private ArrayList<RankListResult> mDatas;
    private int state = NODATA;

    private HPApplication mHpApplication;

    public RecommendAdapter(HPApplication hpApplication, Context context, ArrayList<RankListResult> datas) {
        this.mHpApplication = hpApplication;
        this.mContext = context;
        this.mDatas = datas;
    }

    @Override
    public int getItemViewType(int position) {
        if (state == NODATA && mDatas.size() == position) {
            return NODATA;
        } else {
            if (mDatas.size() == position) {
                return FOOTER;
            }
            return OTHER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = null;
        if (viewType == FOOTER) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_copyright, null, false);
            CopyrightViewHolder holder = new CopyrightViewHolder(view);
            return holder;
        } else if (viewType == NODATA) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_nodata, null, false);
            NoDataViewHolder holder = new NoDataViewHolder(view);
            return holder;
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_recommend, null, false);
            RecommendViewHolder holder = new RecommendViewHolder(view);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof RecommendViewHolder && position < mDatas.size()) {
            RankListResult rankListResult = mDatas.get(position);
            reshViewHolder(position, (RecommendViewHolder) viewHolder, rankListResult);
        }
    }

    /**
     * 刷新ui
     *
     * @param position
     * @param viewHolder
     * @param rankListResult
     */
    private void reshViewHolder(int position, final RecommendViewHolder viewHolder, final RankListResult rankListResult) {

        viewHolder.getRankTitleTv().setText(rankListResult.getRankName());
        viewHolder.getSongName1Tv().setText(rankListResult.getSongNames()[0]);
        viewHolder.getSongName2Tv().setText(rankListResult.getSongNames()[1]);
        viewHolder.getSongName3Tv().setText(rankListResult.getSongNames()[2]);

        //item点击事件
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
                mHpApplication.setRankListResult(rankListResult);

                //打开
                Intent openIntent = new Intent(FragmentReceiver.ACTION_OPENRANKSONGFRAGMENT);
                openIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                mContext.sendBroadcast(openIntent);
            }
        });
        //
        String filePath = ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_CACHE_IMAGE,rankListResult.getImgUrl().hashCode() + ".jpg");
        ImageUtil.loadImage(mContext, viewHolder.getItemImg(), filePath, rankListResult.getImgUrl(), R.mipmap.bpz);
    }

    @Override
    public int getItemCount() {
        return mDatas.size() + 1;
    }

    class RecommendViewHolder extends RecyclerView.ViewHolder {
        private View view;
        /**
         * item底部布局
         */
        private ListItemRelativeLayout listItemRelativeLayout;

        /**
         * item图片
         */
        private ImageView itemImg;
        /**
         * 排行标题
         */
        private TextView rankTitleTv;
        /**
         * 排行歌曲名称
         */
        private TextView songName1Tv;
        /**
         * 排行歌曲名称
         */
        private TextView songName2Tv;
        /**
         * 排行歌曲名称
         */
        private TextView songName3Tv;

        public RecommendViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public ListItemRelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }

        public ImageView getItemImg() {
            if (itemImg == null) {
                itemImg = view.findViewById(R.id.item_icon);
            }
            return itemImg;
        }

        public TextView getRankTitleTv() {
            if (rankTitleTv == null) {
                rankTitleTv = view.findViewById(R.id.rankTitle);
            }
            return rankTitleTv;
        }

        public TextView getSongName1Tv() {
            if (songName1Tv == null) {
                songName1Tv = view.findViewById(R.id.songName1);
            }
            return songName1Tv;
        }

        public TextView getSongName2Tv() {
            if (songName2Tv == null) {
                songName2Tv = view.findViewById(R.id.songName2);
            }
            return songName2Tv;
        }

        public TextView getSongName3Tv() {

            if (songName3Tv == null) {
                songName3Tv = view.findViewById(R.id.songName3);
            }
            return songName3Tv;
        }
    }

    public void setState(int state) {
        this.state = state;
    }
}
