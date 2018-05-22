package com.zlm.hp.model;


/**
 * @Description: 文件信息
 * @author: zhangliangming
 * @date: 2018-05-22 22:26
 **/
public class FileInfo {
    /**
     * 是否是文件
     */
    private boolean isFile;
    /**
     * 路径
     */
    private String filePath;
    /**
     * 文件名
     */
    private String fileName;

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
