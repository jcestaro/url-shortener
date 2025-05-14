package com.github.jcestaro.url_shortener.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/url-shortener")
public class UrlShortenerController {

    @PostMapping
    public ResponseEntity<String> shortenUrl(@RequestBody String url) {
        return ResponseEntity.ok("Hello, world!");
    }

}
