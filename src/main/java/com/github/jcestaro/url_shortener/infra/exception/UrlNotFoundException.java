package com.github.jcestaro.url_shortener.infra.exception;

public class UrlNotFoundException extends RuntimeException {

    public UrlNotFoundException(String shortCode) {
        super("URL not found for code: " + shortCode);
    }

}
