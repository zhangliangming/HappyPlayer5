package base.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * 
 * 自定义置顶RecyclerView
 * 
 */
public class LinearLayoutRecyclerView extends RecyclerView {

	public static final int smoothScroll = 0;
	public static final int scroll = 1;

	private int type = smoothScroll;
	private int mIndex = 0;
	private boolean move = false;

	private LinearLayoutManager linearLayoutManager;

	private OnLinearLayoutRecyclerViewScrollListener listener;

	public void OnLinearLayoutRecyclerViewScrollListener(
			OnLinearLayoutRecyclerViewScrollListener listener) {
		this.listener = listener;
	}

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
		setOnScrollListener(new RecyclerViewListener());
	}

	public void move(int position, int type) {
		mIndex = position;
		if (type == 0) {
			smoothMoveToPosition(position);
		} else {
			moveToPosition(position);
		}
	}

	private void smoothMoveToPosition(int n) {

		int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
		int lastItem = linearLayoutManager.findLastVisibleItemPosition();
		if (n <= firstItem) {
			smoothScrollToPosition(n);
		} else if (n <= lastItem) {
			int top = getChildAt(n - firstItem).getTop();
			smoothScrollBy(0, top);
		} else {
			smoothScrollToPosition(n);
			move = true;
		}

	}

	private void moveToPosition(int n) {

		// 先从RecyclerView的LayoutManager中获取第一项和最后一项的Position
		int firstItem = linearLayoutManager.findFirstVisibleItemPosition();
		int lastItem = linearLayoutManager.findLastVisibleItemPosition();
		// 然后区分情况
		if (n <= firstItem) {
			// 当要置顶的项在当前显示的第一个项的前面时
			scrollToPosition(n);
		} else if (n <= lastItem) {
			// 当要置顶的项已经在屏幕上显示时
			int top = getChildAt(n - firstItem).getTop();
			scrollBy(0, top);
		} else {
			// 当要置顶的项在当前显示的最后一项的后面时
			scrollToPosition(n);
			// 这里这个变量是用在RecyclerView滚动监听里面的
			move = true;
		}

	}

	class RecyclerViewListener extends OnScrollListener {
		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			super.onScrollStateChanged(recyclerView, newState);

			// if (newState == RecyclerView.SCROLL_STATE_IDLE) {
			if (listener != null) {
				// int firstIndex = linearLayoutManager
				// .findFirstVisibleItemPosition();
				// listener.onScrollEnd(firstIndex);
			}
			// }

			if (move && newState == RecyclerView.SCROLL_STATE_IDLE
					&& type == smoothScroll) {
				move = false;
				int n = mIndex
						- linearLayoutManager.findFirstVisibleItemPosition();
				if (0 <= n && n < getChildCount()) {
					int top = getChildAt(n).getTop();
					smoothScrollBy(0, top);
				}

			}
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);
			if (move && type == scroll) {
				move = false;
				int n = mIndex
						- linearLayoutManager.findFirstVisibleItemPosition();
				if (0 <= n && n < getChildCount()) {
					int top = getChildAt(n).getTop();
					scrollBy(0, top);
				}
			}
		}
	}

	public interface OnLinearLayoutRecyclerViewScrollListener {
		public void onScrollEnd(int firstIndex);
	}

}
