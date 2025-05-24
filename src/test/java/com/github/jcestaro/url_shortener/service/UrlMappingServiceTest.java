package com.github.jcestaro.url_shortener.service;

import com.github.jcestaro.url_shortener.infra.UrlMappingRepository;
import com.github.jcestaro.url_shortener.infra.exception.UrlNotFoundException;
import com.github.jcestaro.url_shortener.infra.kafka.config.response.ErrorInfo;
import com.github.jcestaro.url_shortener.infra.kafka.config.response.Response;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

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

        @Test
        @DisplayName("should use existing shortCode if available")
        void shouldUseExistingShortCodeIfAvailable() {
            String originalUrl = "https://www.google.com/";
            UrlMapping existingUrlMapping = new UrlMapping();
            when(repository.findByOriginalUrl(originalUrl)).thenReturn(Optional.of(existingUrlMapping));

            Response<UrlMapping> urlMapping = service.createShortUrl(originalUrl);

            verify(repository, never()).save(any());
            assertEquals(urlMapping.getData(), existingUrlMapping);
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

            Response<UrlMapping> result = service.findByShortCode(shortCode);

            assertEquals(expected.getOriginalUrl(), result.getData().getOriginalUrl());
        }

        @Test
        @DisplayName("should return response with exception when shortCode does not exist")
        void shouldReturnEmpty() {
            String shortCode = "nonexistent";

            when(repository.findByShortCode(shortCode)).thenReturn(Optional.empty());

            Response<UrlMapping> response = service.findByShortCode(shortCode);
            ErrorInfo errorInfo = response.getErrorInfo();

            assertEquals(UrlNotFoundException.class.getName(), errorInfo.getExceptionType());
            assertEquals("URL not found for code: nonexistent", errorInfo.getMessage());
        }
    }
}
