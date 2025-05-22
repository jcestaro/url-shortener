package com.github.jcestaro.url_shortener.web;

import com.github.jcestaro.url_shortener.base.IntegrationTestBase;
import com.github.jcestaro.url_shortener.infra.UrlMappingRepository;
import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class UrlShortenerControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL_SPRING = "https://spring.io/";
    private static final String URL_GOOGLE = "https://www.google.com/";
    private static final String API_BASE_PATH = "/api/url-shortener";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository repository;

    @Test
    @DisplayName("Should shorten a URL, process via Kafka, and return the shortened link")
    void shouldShortenUrlAndReturnShortLink() throws Exception {
        MvcResult result = mockMvc.perform(post(API_BASE_PATH)
                        .content(URL_GOOGLE)
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).contains(API_BASE_PATH + "/");

        String shortCode = responseContent.substring(responseContent.lastIndexOf("/") + 1);
        assertThat(shortCode).isNotBlank();

        UrlMapping saved = repository.findByShortCode(shortCode).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getOriginalUrl()).isEqualTo(URL_GOOGLE);
        assertThat(saved.getShortCode()).isEqualTo(shortCode);
    }

    @Test
    @DisplayName("Should redirect to the original URL when accessing the short URL")
    void shouldRedirectToOriginalUrl() throws Exception {
        String testShortCode = "test123XYZ";
        UrlMapping savedMapping = new UrlMapping(URL_SPRING, testShortCode);
        repository.save(savedMapping);

        mockMvc.perform(get(API_BASE_PATH + "/" + testShortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", URL_SPRING));
    }

    @Test
    @DisplayName("Should return 404 when accessing a nonexistent shortCode")
    void shouldReturn404ForInvalidShortCode() throws Exception {
        String invalidShortCode = UUID.randomUUID().toString().substring(0, 7);

        mockMvc.perform(get(API_BASE_PATH + "/" + invalidShortCode))
                .andExpect(status().isNotFound());
    }
}
