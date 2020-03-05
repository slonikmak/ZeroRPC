package com.oceanos.jeroRPC;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class RPCMessage {

    private Class<?> returnType;

    private String methodName;

    @JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, /*include = JsonTypeInfo.As.PROPERTY,*/ property = "type")
    private Object[] args;

    private Class<?>[] parameterTypes;


    Class<?> getReturnType() {
        return returnType;
    }

    void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    String getMethodName() {
        return methodName;
    }

    void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    Object[] getArgs() {
        return args;
    }

    void setArgs(Object[] args) {
        this.args = args;
    }

    Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
}
