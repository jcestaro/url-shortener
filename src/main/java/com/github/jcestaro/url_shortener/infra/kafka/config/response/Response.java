package com.github.jcestaro.url_shortener.infra.kafka.config.response;

public class Response<T> {

    private T data;
    private ErrorInfo errorInfo;

    public Response() {
    }

    public Response(T data) {
        this.data = data;
    }

    public Response(Exception exception) {
        if (exception != null) {
            this.errorInfo = new ErrorInfo(exception.getClass().getName(), exception.getMessage());
        }
    }

    public T getData() {
        return data;
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }

    public boolean hasError() {
        return this.errorInfo != null;
    }

}