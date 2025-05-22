package com.github.jcestaro.url_shortener.infra.exception;

import java.io.Serializable;

public class UrlNotFoundException extends RuntimeException implements Serializable {

    public UrlNotFoundException(String shortCode) {
        super("URL not found for code: " + shortCode);
    }

}
