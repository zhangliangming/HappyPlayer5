package com.zlm.hp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zlm.hp.ui.R;
import com.zlm.hp.widget.ListItemRelativeLayout;

/**
 * 加载更多
 * Created by zhangliangming on 2017/8/1.
 */
public class LoadMoreViewHolder extends RecyclerView.ViewHolder {
    private View view;
    /**
     * item底部布局
     */
    private ListItemRelativeLayout listItemRelativeLayout;

    public LoadMoreViewHolder(View view) {
        super(view);
        this.view = view;
    }

    public ListItemRelativeLayout getListItemRelativeLayout() {
        if (listItemRelativeLayout == null) {
            listItemRelativeLayout = view.findViewById(R.id.itemBG);
        }
        return listItemRelativeLayout;
    }

}
