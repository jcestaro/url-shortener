package com.github.jcestaro.url_shortener.web;

import com.github.jcestaro.url_shortener.infra.exception.UrlNotFoundException;
import com.github.jcestaro.url_shortener.infra.kafka.config.response.ErrorInfo;
import com.github.jcestaro.url_shortener.infra.kafka.config.response.Response;
import com.github.jcestaro.url_shortener.model.UrlMapping;
import com.github.jcestaro.url_shortener.service.producer.FindUrlProducerService;
import com.github.jcestaro.url_shortener.service.producer.ShortUrlCreatorProducerService;
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

    private final FindUrlProducerService findUrlProducerService;
    private final ShortUrlCreatorProducerService shortUrlCreatorProducerService;

    @Autowired
    public UrlShortenerController(FindUrlProducerService findUrlProducerService, ShortUrlCreatorProducerService shortUrlCreatorProducerService) {
        this.findUrlProducerService = findUrlProducerService;
        this.shortUrlCreatorProducerService = shortUrlCreatorProducerService;
    }

    @PostMapping
    public ResponseEntity<String> shortenUrl(@RequestBody String url, HttpServletRequest request) throws Exception {
        Response<UrlMapping> response = shortUrlCreatorProducerService.sendMessage(url);
        throwExceptionIfResponseHasError(response);

        String baseUrl = request.getRequestURL()
                .toString()
                .replace(request.getRequestURI(), request.getContextPath());

        String shortUrl = baseUrl + API_URL_SHORTENER + "/" + response.getData().getShortCode();

        return ResponseEntity.ok(shortUrl);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) throws Exception {
        Response<UrlMapping> response = findUrlProducerService.sendMessage(shortCode);
        throwExceptionIfResponseHasError(response);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(response.getData().getOriginalUrl()))
                .build();
    }

    private void throwExceptionIfResponseHasError(Response<UrlMapping> response) {
        if (response.hasError()) {
            ErrorInfo error = response.getErrorInfo();

            if (UrlNotFoundException.class.getName().equals(error.getExceptionType())) {
                throw new UrlNotFoundException(error.getMessage());
            }

            throw new RuntimeException(error.getMessage());
        }
    }

}
