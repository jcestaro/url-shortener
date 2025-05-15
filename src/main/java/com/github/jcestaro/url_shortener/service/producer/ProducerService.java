package com.github.jcestaro.url_shortener.service.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProducerService {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    public static final String TOPIC = "meu-topico";

    public ProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC, message);
    }

}
