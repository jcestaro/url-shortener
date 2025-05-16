package com.github.jcestaro.url_shortener.service.producer;

import com.github.jcestaro.url_shortener.base.AbstractProducerTemplateTest;
import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ShortUrlProducerServiceTest extends AbstractProducerTemplateTest<String, UrlMapping> {

    @InjectMocks
    private ShortUrlProducerService shortUrlProducerService;

    @Override
    protected void configureMocks() {
        when(kafkaConfig.replyingKafkaTemplateUrlMapping()).thenReturn(replyingKafkaTemplate);
        ReflectionTestUtils.setField(shortUrlProducerService, "requestTopic", "url-shortener-request");
    }

    @Test
    void shouldSendMessageAndReceiveResponseSuccessfully() throws Exception {
        UrlMapping expectedResponse = new UrlMapping("https://example.com", "abc123");

        when(consumerRecord.value()).thenReturn(expectedResponse);

        UrlMapping result = shortUrlProducerService.sendMessage("https://example.com");

        assertEquals(expectedResponse, result);
    }
}
