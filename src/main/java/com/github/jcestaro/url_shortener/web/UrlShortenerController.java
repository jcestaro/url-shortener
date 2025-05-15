package com.github.jcestaro.url_shortener.web;

import com.github.jcestaro.url_shortener.model.UrlMapping;
import com.github.jcestaro.url_shortener.service.UrlMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        return service.findByShortCode(shortCode)
                .<ResponseEntity<Void>>map(urlMapping -> ResponseEntity.status(302)
                        .location(URI.create(urlMapping.getOriginalUrl()))
                        .build())
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
