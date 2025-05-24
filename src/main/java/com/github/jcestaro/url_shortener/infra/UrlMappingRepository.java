package com.github.jcestaro.url_shortener.infra;

import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface UrlMappingRepository extends MongoRepository<UrlMapping, UUID> {

    Optional<UrlMapping> findByShortCode(String shortCode);

    Optional<UrlMapping> findByOriginalUrl(String originalUrl);

}