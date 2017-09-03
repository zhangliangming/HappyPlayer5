package com.zlm.hp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zlm.hp.ui.R;
import com.zlm.hp.widget.ListItemRelativeLayout;

/**
 * 加载中
 * Created by zhangliangming on 2017/8/1.
 */
public class LoadingViewHolder extends RecyclerView.ViewHolder {
    private View view;
    /**
     * item底部布局
     */
    private ListItemRelativeLayout listItemRelativeLayout;

    public LoadingViewHolder(View view) {
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
