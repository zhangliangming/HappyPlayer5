package com.zlm.hp.net.model;

/**
 * @author zhangliangming
 */
public class HttpResult {
    /**
     *
     */
    public static final int STATUS_SUCCESS = 0;
    /**
     *
     */
    public static final int STATUS_ERROR = -1;

    public static final int STATUS_NONET = -2;

    public static final int STATUS_NOWIFI = -3;
    /**
     * http状态码
     */
    private int status;
    /***
     * http返回的结果
     */
    private Object result;

    /**
     * 错误信息
     */
    private String errorMsg;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

}
