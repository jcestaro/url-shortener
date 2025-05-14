package com.github.jcestaro.url_shortener.service;

import com.github.jcestaro.url_shortener.infra.UrlMappingRepository;
import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

import static org.apache.catalina.manager.Constants.CHARSET;

@Service
public class UrlMappingService {

    private static final int BASE = CHARSET.length();
    private static final int MAX_SIZE_SHORT_URL = 6;

    private final UrlMappingRepository repository;

    @Autowired
    public UrlMappingService(UrlMappingRepository repository) {
        this.repository = repository;
    }

    @Transactional(rollbackFor = Exception.class)
    public UrlMapping createShortUrl(String originalUrl) {
        String shortCode = generateShortUrl();
        UrlMapping shortUrl = new UrlMapping(originalUrl, shortCode);
        return repository.save(shortUrl);
    }

    private String generateShortUrl() {
        StringBuilder shortUrl = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < MAX_SIZE_SHORT_URL; i++) {
            int index = random.nextInt(BASE);
            shortUrl.append(CHARSET.charAt(index));
        }

        return shortUrl.toString();
    }

}
