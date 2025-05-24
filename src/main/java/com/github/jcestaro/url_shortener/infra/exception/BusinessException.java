package com.github.jcestaro.url_shortener.infra.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

}
