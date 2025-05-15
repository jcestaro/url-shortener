package com.github.jcestaro.url_shortener.web;

import com.github.jcestaro.url_shortener.base.IntegrationTestBase;
import com.github.jcestaro.url_shortener.infra.UrlMappingRepository;
import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerControllerIntegrationTest extends IntegrationTestBase {

    private static final String URL_SPRING = "https://spring.io/";
    private static final String URL_GOOGLE = "https://www.google.com/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository repository;

    @Test
    @DisplayName("Should shorten a URL and return the shortened link")
    void shouldShortenUrlAndReturnShortLink() throws Exception {
        String response = mockMvc.perform(post("/api/url-shortener")
                        .content(URL_GOOGLE)
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).contains("/api/url-shortener/");
        String shortCode = response.substring(response.lastIndexOf("/") + 1);

        UrlMapping saved = repository.findByShortCode(shortCode).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getOriginalUrl()).isEqualTo(URL_GOOGLE);

        repository.delete(saved);
    }

    @Test
    @DisplayName("Should redirect to the original URL when accessing the short URL")
    void shouldRedirectToOriginalUrl() throws Exception {
        UrlMapping saved = repository.save(new UrlMapping(URL_SPRING, "test123"));

        mockMvc.perform(get("/api/url-shortener/test123"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://spring.io/"));

        repository.delete(saved);
    }

    @Test
    @DisplayName("Should return 404 when accessing a nonexistent shortCode")
    void shouldReturn404ForInvalidShortCode() throws Exception {
        String invalidShortCode = UUID.randomUUID().toString().substring(0, 6);

        mockMvc.perform(get("/api/url-shortener/" + invalidShortCode))
                .andExpect(status().isNotFound());
    }
}
