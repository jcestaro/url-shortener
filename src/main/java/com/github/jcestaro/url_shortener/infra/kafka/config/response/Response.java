package com.github.jcestaro.url_shortener.infra.kafka.config.response;

public class Response<T> {

    private T data;
    private Exception exception;

    public Response() {
    }

    public Response(T data) {
        this.data = data;
    }

    public Response(Exception exception) {
        this.exception = exception;
    }

    public T getData() {
        return data;
    }

    public Exception getException() {
        return exception;
    }

}