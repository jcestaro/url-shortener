package com.github.jcestaro.url_shortener.service;

import com.github.jcestaro.url_shortener.infra.UrlMappingRepository;
import com.github.jcestaro.url_shortener.infra.exception.UrlNotFoundException;
import com.github.jcestaro.url_shortener.infra.kafka.config.response.Response;
import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;


@Service
public class UrlMappingService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALPHABET.length();
    private static final int MAX_SIZE_SHORT_URL = 6;

    private final UrlMappingRepository repository;

    @Autowired
    public UrlMappingService(UrlMappingRepository repository) {
        this.repository = repository;
    }

    @SendTo
    @Transactional(readOnly = true)
    @KafkaListener(
            topics = "${kafka.topic.requestreply.findurl.request}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactoryString"
    )
    public Response<UrlMapping> findByShortCode(String shortCode) {
        try {
            UrlMapping urlMapping = repository.findByShortCode(shortCode)
                    .orElseThrow(() -> new UrlNotFoundException(shortCode));

            return new Response<>(urlMapping);
        } catch (Exception ex) {
            return new Response<>(ex);
        }
    }

    @SendTo
    @Transactional
    @KafkaListener(
            topics = "${kafka.topic.requestreply.shorturlcreator.request}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactoryString"
    )
    public Response<UrlMapping> createShortUrl(String originalUrl) {
        try {
            Optional<UrlMapping> possibleExistingMapping = repository.findByOriginalUrl(originalUrl);

            if (possibleExistingMapping.isPresent()) {
                return new Response<>(possibleExistingMapping.get());
            }

            String shortCode = generateShortUrl();
            UrlMapping shortUrl = new UrlMapping(originalUrl, shortCode);
            return new Response<>(repository.save(shortUrl));
        } catch (Exception ex) {
            return new Response<>(ex);
        }
    }

    private String generateShortUrl() {
        StringBuilder shortUrl = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < MAX_SIZE_SHORT_URL; i++) {
            int index = random.nextInt(BASE);
            shortUrl.append(ALPHABET.charAt(index));
        }

        return shortUrl.toString();
    }

}
