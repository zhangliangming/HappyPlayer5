package com.zlm.hp.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.zlm.hp.ui.R;

/**
 * 自定义置顶RecyclerView
 */
public class LinearLayoutRecyclerView extends RecyclerView {

    private LinearLayoutManager linearLayoutManager;

    public LinearLayoutRecyclerView(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle);
    }

    public LinearLayoutRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutRecyclerView(Context context) {
        super(context);
    }

    public LinearLayoutManager getLinearLayoutManager() {
        return linearLayoutManager;
    }

    public void setLinearLayoutManager(LinearLayoutManager linearLayoutManager) {
        this.linearLayoutManager = linearLayoutManager;

        setLayoutManager(linearLayoutManager);
    }

    /**
     * 跳转
     *
     * @param position 位置
     */
    public void moveToPosition(int position) {
        // 先从RecyclerView的LayoutManager中获取第一项和最后一项的Position
        int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
        int lastItem = linearLayoutManager.findLastVisibleItemPosition();
        // 然后区分情况
        if (position <= firstItem) {
            // 当要置顶的项在当前显示的第一个项的前面时
            scrollToPosition(position);
        } else if (position <= lastItem) {
            // 当要置顶的项已经在屏幕上显示时
            int top = getChildAt(position - firstItem).getTop();
            scrollBy(0, top);
        } else {
            // 当要置顶的项在当前显示的最后一项的后面时
            scrollToPosition(position);
        }
    }

    /**
     * 移动到中间
     *
     * @param position
     */
    public void moveToMiddle(int position) {
        int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
        int lastItem = linearLayoutManager.findLastVisibleItemPosition();
        int middleItem = (firstItem + lastItem) / 2;
        // 然后区分情况
        if (position <= firstItem) {
            // 当要置顶的项在当前显示的第一个项的前面时
            scrollToPosition(position);
        } else if (middleItem <= position && position <= lastItem) {
            //
            float viewHeight = getResources().getDimension(R.dimen.songitem_height);
            scrollBy(0, (int) viewHeight);

        } else {
            // 当要置顶的项在当前显示的最后一项的后面时
            scrollToPosition(position);
        }
    }
}
