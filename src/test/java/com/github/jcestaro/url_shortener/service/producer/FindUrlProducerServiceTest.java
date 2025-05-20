package com.github.jcestaro.url_shortener.service.producer;

import com.github.jcestaro.url_shortener.base.AbstractProducerTemplateTest;
import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class FindUrlProducerServiceTest extends AbstractProducerTemplateTest<String, UrlMapping> {

    @InjectMocks
    private FindUrlProducerService findUrlProducerService;

    @Override
    protected void configureMocks() {
        when(kafkaConfig.replyingKafkaTemplateUrlMappingFinder()).thenReturn(replyingKafkaTemplate);
        ReflectionTestUtils.setField(findUrlProducerService, "requestTopic", "url-shortener-request");
    }

    @Test
    void shouldSendMessageAndReceiveResponseSuccessfully() throws Exception {
        UrlMapping expectedResponse = new UrlMapping("https://example.com", "abc123");

        when(consumerRecord.value()).thenReturn(expectedResponse);

        UrlMapping result = findUrlProducerService.sendMessage("https://example.com");

        assertEquals(expectedResponse, result);
    }

}