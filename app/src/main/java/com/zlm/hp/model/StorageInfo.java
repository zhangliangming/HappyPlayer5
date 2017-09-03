package com.zlm.hp.model;

import java.io.Serializable;

public class StorageInfo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 路径
     */
    public String path;
    /**
     * 挂载状态
     */
    public String state;
    /**
     * 是否移除
     */
    public boolean isRemoveable;

    public StorageInfo(String path) {
        this.path = path;
    }

    public boolean isMounted() {
        return "mounted".equals(state);
    }

    @Override
    public String toString() {
        return "StorageInfo{" +
                "path='" + path + '\'' +
                ", state='" + state + '\'' +
                ", isRemoveable=" + isRemoveable +
                '}';
    }
}