package com.zlm.hp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zlm.hp.lyrics.model.MakeLrcLineInfo;
import com.zlm.hp.lyrics.widget.MakeLrcPreView;
import com.zlm.hp.ui.R;

import java.util.ArrayList;

/**
 * 制作歌词适配器
 * Created by zhangliangming on 2018-03-28.
 */

public class MakeLrcAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * 歌词数据集合
     */
    private ArrayList<MakeLrcLineInfo> mMakeLrcs;

    private Context mContext;
    /**
     * 选中索引
     */
    private int mSelectedIndex = 0;

    public MakeLrcAdapter(Context context, ArrayList<MakeLrcLineInfo> makeLrcs) {
        this.mContext = context;
        this.mMakeLrcs = makeLrcs;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_makelrc, null, false);
        MakeLrcViewHolder makeLrcViewHolder = new MakeLrcViewHolder(view);
        return makeLrcViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MakeLrcViewHolder && position < mMakeLrcs.size()) {
            MakeLrcLineInfo makeLrcLineInfo = mMakeLrcs.get(position);
            reshViewHolder((MakeLrcViewHolder) holder, position, makeLrcLineInfo);
        }
    }

    /**
     * @param viewHolder
     * @param position
     * @param makeLrcLineInfo
     */
    private void reshViewHolder(final MakeLrcViewHolder viewHolder, final int position, final MakeLrcLineInfo makeLrcLineInfo) {
        viewHolder.getIndexTv().setText(String.format("%0" + (mMakeLrcs.size() + "").length() + "d", (position + 1)));
        final MakeLrcPreView makeLrcPreView = viewHolder.getMakeLrcPreView();
        makeLrcPreView.setMakeLrcInfo(makeLrcLineInfo);
        //该行歌词已经录制完成
        if (makeLrcLineInfo.getStatus() == MakeLrcLineInfo.STATUS_FINISH || mSelectedIndex == position) {
            viewHolder.getSelectRB().setChecked(true);
            makeLrcLineInfo.setStatus(MakeLrcLineInfo.STATUS_SELECTED);
        } else if (mSelectedIndex != position) {

            viewHolder.getSelectRB().setChecked(false);
            makeLrcLineInfo.setStatus(MakeLrcLineInfo.STATUS_NONE);
        }
        boolean select = viewHolder.getSelectRB().isChecked();
        if (!select) {

            viewHolder.getItemBG().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int oldIndex = mSelectedIndex;
                    mSelectedIndex = position;

                    //刷新旧的一行数据
                    notifyItemChanged(oldIndex);

                    //更新新一行的数据
                    viewHolder.getSelectRB().setChecked(true);
                    makeLrcLineInfo.setStatus(MakeLrcLineInfo.STATUS_SELECTED);
                    makeLrcPreView.postInvalidate();

                }
            });
        }

        viewHolder.getResetBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeLrcLineInfo.reset();
                notifyItemChanged(position);
            }
        });

        makeLrcPreView.postInvalidate();
    }

    /**
     * 获取当前的制作歌词行
     *
     * @return
     */
    public MakeLrcLineInfo getCurMakeLrcLineInfo() {
        if (mSelectedIndex >= 0 && mSelectedIndex < mMakeLrcs.size()) {
            return mMakeLrcs.get(mSelectedIndex);
        }
        return null;
    }

    public void setNextSelectIndex() {
        int oldIndex = mSelectedIndex;
        //刷新旧的一行数据
        notifyItemChanged(oldIndex);
        mSelectedIndex++;
        if (mSelectedIndex >= 0 && mSelectedIndex < mMakeLrcs.size()) {

            //刷新新的一行数据
            notifyItemChanged(mSelectedIndex);

        } else {
            mSelectedIndex = mMakeLrcs.size() - 1;
        }
    }

    /**
     * 更新
     */
    public void reshSelectedIndexView() {
        //刷新新的一行数据
        notifyItemChanged(mSelectedIndex);
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    @Override
    public int getItemCount() {
        return mMakeLrcs.size();
    }

    public void reset(){
        mSelectedIndex = 0;
        notifyDataSetChanged();
    }

    class MakeLrcViewHolder extends RecyclerView.ViewHolder {

        private View view;
        /**
         * 索引
         */
        private TextView indexTv;
        /**
         * 选择按钮
         */
        private RadioButton selectRB;
        /**
         * 制作歌词预览视图
         */
        private MakeLrcPreView makeLrcPreView;
        /**
         * 重置按钮
         */
        private Button resetBtn;

        private RelativeLayout itemBG;


        public MakeLrcViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
        }

        public RelativeLayout getItemBG() {
            if (itemBG == null) {
                itemBG = view.findViewById(R.id.itemBG);
            }
            return itemBG;
        }

        public TextView getIndexTv() {
            if (indexTv == null) {
                indexTv = view.findViewById(R.id.index);
            }
            return indexTv;
        }

        public RadioButton getSelectRB() {
            if (selectRB == null) {
                selectRB = view.findViewById(R.id.select);
            }
            return selectRB;
        }

        public MakeLrcPreView getMakeLrcPreView() {
            if (makeLrcPreView == null) {
                makeLrcPreView = view.findViewById(R.id.makeLrcPreView);
            }
            return makeLrcPreView;
        }

        public Button getResetBtn() {
            if (resetBtn == null) {
                resetBtn = view.findViewById(R.id.reset);
            }
            return resetBtn;
        }
    }
}
