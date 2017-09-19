package com.zlm.hp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zlm.hp.application.HPApplication;
import com.zlm.hp.constants.ResourceConstants;
import com.zlm.hp.model.AudioInfo;
import com.zlm.hp.net.entity.SearchArtistPicResult;
import com.zlm.hp.ui.R;
import com.zlm.hp.ui.SearchSingerActivity;
import com.zlm.hp.utils.ImageUtil;
import com.zlm.hp.utils.ResourceFileUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 搜索歌手写真
 */
public class SearchSingerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * 无数据
     */
    public static final int NODATA = 1;
    /**
     * 已经到底了
     */
    public static final int NOMOREDATA = 2;
    /**
     * 内容item
     */
    public static final int OTHER = 4;
    private Context mContext;
    private List<SearchArtistPicResult> mDatas;
    private int state = NOMOREDATA;

    private HPApplication mHPApplication;
    private AudioInfo mAudioInfo;

    private SearchSingerActivity.SearchSingerListener mSearchSingerListener;
    private Map<String, String> mSelectDatas;

    public SearchSingerAdapter(HPApplication hPApplication, Context context, List<SearchArtistPicResult> datas, AudioInfo audioInfo, Map<String, String> selectDatas, SearchSingerActivity.SearchSingerListener searchSingerListener) {
        this.mAudioInfo = audioInfo;
        this.mHPApplication = hPApplication;
        this.mContext = context;
        this.mDatas = datas;
        this.mSearchSingerListener = searchSingerListener;
        this.mSelectDatas = selectDatas;
    }

    @Override
    public int getItemViewType(int position) {
        if (state == NODATA && mDatas.size() == position) {
            return NODATA;
        } else {
            if (mDatas.size() == position) {
                return NOMOREDATA;
            }
            return OTHER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = null;
        if (viewType == NODATA) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_singerimg_nodata, null, false);
            NoDataViewHolder holder = new NoDataViewHolder(view);
            return holder;
        } else if (viewType == NOMOREDATA) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_singerimg_nomoredata, null, false);
            NoDataViewHolder holder = new NoDataViewHolder(view);
            return holder;
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_singerimg, null, false);
            SearchSingerViewHolder holder = new SearchSingerViewHolder(view);
            return holder;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof SearchSingerViewHolder && position < mDatas.size()) {
            SearchArtistPicResult searchArtistPicResult = mDatas.get(position);
            reshViewHolder(position, (SearchSingerViewHolder) viewHolder, searchArtistPicResult);
        }
    }

    /**
     * 刷新
     *
     * @param position
     * @param viewHolder
     * @param searchArtistPicResult
     */
    private void reshViewHolder(final int position, final SearchSingerViewHolder viewHolder, final SearchArtistPicResult searchArtistPicResult) {
        String filePath = ResourceFileUtil.getFilePath(mContext, ResourceConstants.PATH_SINGER, (searchArtistPicResult.getSinger() + File.separator + searchArtistPicResult.getImgUrl().hashCode() + ".jpg"));
        ImageUtil.loadImage(mContext, viewHolder.getSingPicImg(), filePath, searchArtistPicResult.getImgUrl(), R.mipmap.picture_manager_default);
        if (mSelectDatas.containsKey(searchArtistPicResult.getImgUrl().hashCode() + "")) {
            viewHolder.getSelectedImg().setVisibility(View.VISIBLE);
            viewHolder.getUnSelectImg().setVisibility(View.INVISIBLE);
        } else {
            viewHolder.getSelectedImg().setVisibility(View.INVISIBLE);
            viewHolder.getUnSelectImg().setVisibility(View.VISIBLE);
        }
        //
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.getSelectedImg().getVisibility() == View.VISIBLE) {
                    viewHolder.getSelectedImg().setVisibility(View.INVISIBLE);
                    viewHolder.getUnSelectImg().setVisibility(View.VISIBLE);
                } else {
                    viewHolder.getSelectedImg().setVisibility(View.VISIBLE);
                    viewHolder.getUnSelectImg().setVisibility(View.INVISIBLE);
                }

                if (mSearchSingerListener != null) {
                    mSearchSingerListener.onClick(searchArtistPicResult.getImgUrl());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size() + 1;
    }

    public void setState(int state) {
        this.state = state;
    }

    /////////////////////////////////////////////////////

    class SearchSingerViewHolder extends RecyclerView.ViewHolder {
        private View view;
        private ImageView singPicImg;
        /**
         * item底部布局
         */
        private RelativeLayout listItemRelativeLayout;
        /**
         * 未选择
         */
        private ImageView unSelectImg;
        /**
         * 选择
         */
        private ImageView selectedImg;

        public SearchSingerViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public ImageView getSingPicImg() {
            if (singPicImg == null) {
                singPicImg = view.findViewById(R.id.singPic);
            }
            return singPicImg;
        }

        public RelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }

        public ImageView getUnSelectImg() {

            if (unSelectImg == null) {
                unSelectImg = view.findViewById(R.id.unselect);
            }
            return unSelectImg;
        }

        public ImageView getSelectedImg() {
            if (selectedImg == null) {
                selectedImg = view.findViewById(R.id.selected);
            }
            return selectedImg;
        }
    }

}
