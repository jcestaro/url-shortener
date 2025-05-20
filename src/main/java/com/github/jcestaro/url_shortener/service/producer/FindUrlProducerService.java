package com.github.jcestaro.url_shortener.service.producer;

import com.github.jcestaro.url_shortener.infra.kafka.template.ProducerTemplate;
import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class FindUrlProducerService extends ProducerTemplate<String, UrlMapping> {

    private static final String KEY = "url-mapping-short-url-finder-key";

    @Value("${kafka.topic.requestreply.findurl.request}")
    private String requestTopic;

    @Override
    protected String getKey() {
        return KEY;
    }

    @Override
    protected String getRequestTopic() {
        return requestTopic;
    }

    @Override
    protected ReplyingKafkaTemplate<String, String, UrlMapping> getReplyingKafkaTemplate() {
        return kafkaConfig.replyingKafkaTemplateUrlMappingFinder();
    }

}
