package com.zlm.hp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: tab适配器
 * @Param:
 * @Return:
 * @Author: zhangliangming
 * @Date: 2017/7/16 20:33
 * @Throws:
 */
public class TabFragmentAdapter extends FragmentPagerAdapter {
    //存储所有的fragment
    private List<Fragment> list;

    public TabFragmentAdapter(FragmentManager fm, ArrayList<Fragment> list) {
        super(fm);
        this.list = list;

    }

    @Override
    public Fragment getItem(int index) {

        return list.get(index);
    }

    @Override
    public int getCount() {

        return list.size();
    }

}
