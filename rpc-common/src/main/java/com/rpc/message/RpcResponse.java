package com.rpc.message;

import java.io.Serializable;

/**
 * 响应体
 */
public class RpcResponse implements Serializable {
    private static final long serialVersionUID = -6233082425324136452L;

    private String requestId;
    private String errMsg;
    private Object result;

    /**
     * 判断是否调用成功
     * @return true： 调用成功； false： 调用失败
     */
    public boolean isSuccess() {
        return errMsg == null;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
