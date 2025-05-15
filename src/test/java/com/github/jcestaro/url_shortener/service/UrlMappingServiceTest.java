package com.github.jcestaro.url_shortener.service;

import com.github.jcestaro.url_shortener.infra.UrlMappingRepository;
import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UrlMappingServiceTest {

    @Mock
    private UrlMappingRepository repository;

    @InjectMocks
    private UrlMappingService service;

    @Captor
    private ArgumentCaptor<UrlMapping> urlMappingArgumentCaptor;

    @Nested
    @DisplayName("createShortUrl")
    class CreateShortUrl {

        @Test
        @DisplayName("should generate shortCode and save to repository")
        void shouldGenerateShortCodeAndSave() {
            String originalUrl = "https://www.google.com/";

            service.createShortUrl(originalUrl);

            verify(repository).save(urlMappingArgumentCaptor.capture());
            UrlMapping saved = urlMappingArgumentCaptor.getValue();

            assertEquals(originalUrl, saved.getOriginalUrl());
            assertNotNull(saved.getShortCode());
            assertEquals(6, saved.getShortCode().length());
        }
    }

    @Nested
    @DisplayName("findByShortCode")
    class FindByShortCode {

        @Test
        @DisplayName("should return UrlMapping when shortCode exists")
        void shouldReturnUrlMapping() {
            String shortCode = "abc123";
            UrlMapping expected = new UrlMapping("https://www.google.com/", shortCode);

            when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(expected));

            Optional<UrlMapping> result = service.findByShortCode(shortCode);

            assertTrue(result.isPresent());
            assertEquals(expected.getOriginalUrl(), result.get().getOriginalUrl());
        }

        @Test
        @DisplayName("should return empty when shortCode does not exist")
        void shouldReturnEmpty() {
            String shortCode = "nonexistent";

            when(repository.findByShortCode(shortCode)).thenReturn(Optional.empty());

            Optional<UrlMapping> result = service.findByShortCode(shortCode);

            assertTrue(result.isEmpty());
        }
    }
}
