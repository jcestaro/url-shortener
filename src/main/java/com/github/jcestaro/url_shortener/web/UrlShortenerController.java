package com.github.jcestaro.url_shortener.web;

import com.github.jcestaro.url_shortener.model.UrlMapping;
import com.github.jcestaro.url_shortener.service.UrlMappingService;
import com.github.jcestaro.url_shortener.service.producer.ShortUrlProducerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.github.jcestaro.url_shortener.web.UrlShortenerController.API_URL_SHORTENER;

@RestController
@RequestMapping(API_URL_SHORTENER)
public class UrlShortenerController {

    public static final String API_URL_SHORTENER = "/api/url-shortener";

    private final UrlMappingService service;
    private final ShortUrlProducerService shortUrlProducerService;

    @Autowired
    public UrlShortenerController(UrlMappingService service, ShortUrlProducerService shortUrlProducerService) {
        this.service = service;
        this.shortUrlProducerService = shortUrlProducerService;
    }

    @PostMapping
    public ResponseEntity<String> shortenUrl(@RequestBody String url, HttpServletRequest request) throws Exception {
        UrlMapping urlMapping = shortUrlProducerService.sendMessage(url);

        String baseUrl = request.getRequestURL()
                .toString()
                .replace(request.getRequestURI(), request.getContextPath());

        String shortUrl = baseUrl + API_URL_SHORTENER + "/" + urlMapping.getShortCode();

        return ResponseEntity.ok(shortUrl);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        return service.findByShortCode(shortCode)
                .<ResponseEntity<Void>>map(urlMapping -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(urlMapping.getOriginalUrl()))
                        .build())
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
