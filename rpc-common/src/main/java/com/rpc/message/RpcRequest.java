package com.rpc.message;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

/**
 * 请求体
 */
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = -3706960744622561066L;
    /**
     * 唯一的请求ID
     */
    private String requestId;
    /**
     * 类名
     */
    private String className;
    /**
     * 方法名
     */
    private String methodName;
    /**
     * 参数
     */
    private Object[] params;
    /**
     * 参数类型
     */
    private Class<?>[] parameterizedTypes;
    /**
     * 版本
     */
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Class<?>[] getParameterizedTypes() {
        return parameterizedTypes;
    }

    public void setParameterizedTypes(Class<?>[] parameterizedTypes) {
        this.parameterizedTypes = parameterizedTypes;
    }
}
