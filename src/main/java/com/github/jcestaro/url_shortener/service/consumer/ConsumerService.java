package com.github.jcestaro.url_shortener.service.consumer;

import com.github.jcestaro.url_shortener.service.producer.ProducerService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {

    @KafkaListener(topics = ProducerService.TOPIC, groupId = "url-shortener-group")
    public void listen(String message) {
        System.out.println("Mensagem recebida: " + message);
    }

}
