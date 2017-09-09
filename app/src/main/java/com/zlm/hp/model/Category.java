package com.zlm.hp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类
 * Created by zhangliangming on 2017/8/5.
 */
public class Category {

    /**
     * 分类名
     */
    private String categoryName;

    /**
     * 分类的内容
     */
    private List<Object> categoryItem = new ArrayList<Object>();

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<Object> getCategoryItem() {
        return categoryItem;
    }

    public void setCategoryItem(List<Object> categoryItem) {
        this.categoryItem = categoryItem;
    }

    /**
     * 获取总个数
     *
     * @return
     */
    public int getCount() {
        return categoryItem.size() + 1;
    }

    /**
     * 获取子个数
     *
     * @return
     */
    public int getItemCount() {
        return categoryItem.size();
    }

    /**
     * 根据索引获取子内容
     *
     * @param pPosition
     * @return
     */
    public Object getItem(int pPosition) {
        if (pPosition < 0)
            return null;
        if (pPosition == 0) {
            return categoryName;
        } else {
            if (categoryItem.size() == 0) {
                return null;
            }
            return categoryItem.get(pPosition - 1);
        }
    }
}
