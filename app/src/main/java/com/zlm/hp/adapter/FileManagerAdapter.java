package com.zlm.hp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.zlm.hp.model.FileInfo;
import com.zlm.hp.ui.R;
import com.zlm.hp.widget.ListItemRelativeLayout;

import java.util.ArrayList;

/**
 * @Description: 文件管理器适配器
 * @author: zhangliangming
 * @date: 2018-05-22 22:29
 **/
public class FileManagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private ArrayList<FileInfo> mDatas;
    private ItemEvent mItemEvent;

    private final int FILETYPE = 0;
    private final int FILEDIRECTORYTYPE = 1;
    private final int NODATA = -1;

    private String mSelectFilePath;

    public FileManagerAdapter(Context context, ArrayList<FileInfo> datas) {
        this.mContext = context;
        this.mDatas = datas;

        mSelectFilePath = "";
    }


    @Override
    public int getItemViewType(int position) {
        if (mDatas.size() == 0) return NODATA;

        FileInfo fileInfo = mDatas.get(position);
        if (fileInfo.isFile()) {
            return FILETYPE;
        } else {
            return FILEDIRECTORYTYPE;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == FILETYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_file, null, false);
            FileViewHolder holder = new FileViewHolder(view);
            return holder;
        } else if (viewType == FILEDIRECTORYTYPE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_file_directory, null, false);
            FileDirectoryViewHolder holder = new FileDirectoryViewHolder(view);
            return holder;
        } else {
            view = LayoutInflater.from(mContext).inflate(R.layout.layout_lvitem_nodata, null, false);
            NoDataViewHolder holder = new NoDataViewHolder(view);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof FileViewHolder && position < mDatas.size()) {
            reshFileViewHolder((FileViewHolder) viewHolder, position);
        } else if (viewHolder instanceof FileDirectoryViewHolder && position < mDatas.size()) {
            reshFileDirectoryViewHolder((FileDirectoryViewHolder) viewHolder, position);
        }
    }

    /**
     * 文件夹
     *
     * @param viewHolder
     * @param position
     */
    private void reshFileDirectoryViewHolder(FileDirectoryViewHolder viewHolder, int position) {
        final FileInfo fileInfo = mDatas.get(position);
        viewHolder.getFileDirectoryNameTextView().setText(fileInfo.getFileName());
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemEvent != null) {
                    mItemEvent.fileDirectoryClick(fileInfo.getFilePath());
                }
            }
        });
    }

    /**
     * 文件
     *
     * @param viewHolder
     * @param position
     */
    private void reshFileViewHolder(final FileViewHolder viewHolder, int position) {
        final FileInfo fileInfo = mDatas.get(position);
        viewHolder.getFimeNamTextView().setText(fileInfo.getFileName());
        if (mSelectFilePath.equals(fileInfo.getFilePath())) {
            viewHolder.getFileRadioButton().setChecked(true);
        } else {
            viewHolder.getFileRadioButton().setChecked(false);
        }
        viewHolder.getListItemRelativeLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = viewHolder.getFileRadioButton().isChecked();
                if (flag) {
                    viewHolder.getFileRadioButton().setChecked(false);
                } else {
                    viewHolder.getFileRadioButton().setChecked(true);
                    for (int i = 0; i < mDatas.size(); i++) {
                        FileInfo temp = mDatas.get(i);
                        if (temp.getFilePath().equals(mSelectFilePath)) {
                            notifyItemChanged(i,0);
                            break;
                        }
                    }
                    mSelectFilePath = fileInfo.getFilePath();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDatas.size() == 0 ? 1 : mDatas.size();
    }

    /**
     * @Description: 文件
     * @author: zhangliangming
     * @date: 2018-05-22 22:34
     **/
    class FileViewHolder extends RecyclerView.ViewHolder {
        private View view;
        /**
         * item底部布局
         */
        private ListItemRelativeLayout listItemRelativeLayout;

        /**
         * 文件名
         */
        private TextView fimeNamTextView;
        /**
         * 文件选择按钮
         */
        private RadioButton fileRadioButton;

        public FileViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public ListItemRelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }

        public TextView getFimeNamTextView() {
            if (fimeNamTextView == null) {
                fimeNamTextView = view.findViewById(R.id.filename);
            }
            return fimeNamTextView;
        }

        public RadioButton getFileRadioButton() {
            if (fileRadioButton == null) {
                fileRadioButton = view.findViewById(R.id.fileRadioButton);
            }
            return fileRadioButton;
        }
    }


    /**
     * @Description: 文件夹
     * @author: zhangliangming
     * @date: 2018-05-22 22:34
     **/
    class FileDirectoryViewHolder extends RecyclerView.ViewHolder {
        private View view;
        /**
         * item底部布局
         */
        private ListItemRelativeLayout listItemRelativeLayout;

        /**
         * 文件夹名
         */
        private TextView fileDirectoryNameTextView;


        public FileDirectoryViewHolder(View view) {
            super(view);
            this.view = view;
        }

        public ListItemRelativeLayout getListItemRelativeLayout() {
            if (listItemRelativeLayout == null) {
                listItemRelativeLayout = view.findViewById(R.id.itemBG);
            }
            return listItemRelativeLayout;
        }

        public TextView getFileDirectoryNameTextView() {
            if (fileDirectoryNameTextView == null) {
                fileDirectoryNameTextView = view.findViewById(R.id.filedirectoryname);
            }
            return fileDirectoryNameTextView;
        }
    }

    public String getSelectFilePath() {
        return mSelectFilePath;
    }

    public void setSelectFilePath(String mSelectFilePath) {
        this.mSelectFilePath = mSelectFilePath;
    }

    public void setItemEvent(ItemEvent mItemEvent) {
        this.mItemEvent = mItemEvent;
    }

    public interface ItemEvent {
        public void fileDirectoryClick(String filePath);
    }
}
