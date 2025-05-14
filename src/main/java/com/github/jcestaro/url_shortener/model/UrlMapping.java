package com.github.jcestaro.url_shortener.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "url_mappings")
public class UrlMapping {

    @Id
    private UUID id;
    private String originalUrl;
    private String shortCode;
    private LocalDateTime createdAt = LocalDateTime.now();

    public UrlMapping() {
    }

    public UrlMapping(String originalUrl, String shortCode) {
        this.id = UUID.randomUUID();
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}