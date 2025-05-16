package com.github.jcestaro.url_shortener.service.producer;

import com.github.jcestaro.url_shortener.infra.kafka.template.ProducerTemplate;
import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShortUrlProducerService extends ProducerTemplate<String, UrlMapping> {

    public static final String KEY = "url-mapping-key";

    public ShortUrlProducerService(ReplyingKafkaTemplate<String, String, UrlMapping> replyingKafkaTemplate) {
        super(replyingKafkaTemplate);
    }

    @Override
    protected String getKey() {
        return KEY;
    }

}
