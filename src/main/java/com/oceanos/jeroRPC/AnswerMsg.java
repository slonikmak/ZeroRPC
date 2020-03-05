package com.oceanos.jeroRPC;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AnswerMsg {
    private Object result;

    @JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    private Exception exception;

    public AnswerMsg() {
    }

    Object getResult() {
        return result;
    }

    void setResult(Object result) {
        this.result = result;
    }

    Exception getException() {
        return exception;
    }

    void setException(Exception exception) {
        this.exception = exception;
    }
}
