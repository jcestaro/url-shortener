package com.github.jcestaro.url_shortener.service.producer;

import com.github.jcestaro.url_shortener.base.AbstractProducerTemplateTest;
import com.github.jcestaro.url_shortener.infra.kafka.config.response.Response;
import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ShortUrlCreatorProducerServiceTest extends AbstractProducerTemplateTest<String, Response<UrlMapping>> {

    @InjectMocks
    private ShortUrlCreatorProducerService shortUrlCreatorProducerService;

    @Override
    protected void configureMocks() {
        when(kafkaConfig.replyingKafkaTemplateUrlMappingCreator()).thenReturn(replyingKafkaTemplate);
        ReflectionTestUtils.setField(shortUrlCreatorProducerService, "requestTopic", "url-shortener-request");
    }

    @Test
    void shouldSendMessageAndReceiveResponseSuccessfully() throws Exception {
        Response<UrlMapping> expectedResponse = new Response<>(new UrlMapping("https://example.com", "abc123"));

        when(consumerRecord.value()).thenReturn(expectedResponse);

        Response<UrlMapping> result = shortUrlCreatorProducerService.sendMessage("https://example.com");

        assertEquals(expectedResponse, result);
    }
}
