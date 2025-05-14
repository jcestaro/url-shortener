package com.github.jcestaro.url_shortener.web;

import com.github.jcestaro.url_shortener.model.UrlMapping;
import com.github.jcestaro.url_shortener.service.UrlMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/url-shortener")
public class UrlShortenerController {

    private final UrlMappingService service;

    @Autowired
    public UrlShortenerController(UrlMappingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> shortenUrl(@RequestBody String url) {
        UrlMapping urlMapping = service.createShortUrl(url);
        return ResponseEntity.ok(urlMapping.getShortCode());
    }

}
