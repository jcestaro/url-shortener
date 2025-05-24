package com.github.jcestaro.url_shortener.infra.kafka.config.response;

public class ErrorInfo {

    private String exceptionType;
    private String message;

    public ErrorInfo() {
    }

    public ErrorInfo(String exceptionType, String message) {
        this.exceptionType = exceptionType;
        this.message = message;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}