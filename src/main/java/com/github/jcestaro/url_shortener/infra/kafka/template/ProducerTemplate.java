package com.github.jcestaro.url_shortener.infra.kafka.template;

import com.github.jcestaro.url_shortener.infra.kafka.config.KafkaConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;

import java.util.concurrent.TimeUnit;

public abstract class ProducerTemplate<T, E> {

    private static final int DEFAULT_TIMEOUT = 20;

    @Value("${kafka.topic.requestreply.request}")
    private String requestTopic;

    @Autowired
    protected KafkaConfig kafkaConfig;

    protected abstract String getKey();

    protected abstract ReplyingKafkaTemplate<String, T, E> getReplyingKafkaTemplate();

    public E sendMessage(T message) throws Exception {
        ProducerRecord<String, T> record = new ProducerRecord<>(requestTopic, getKey(), message);
        RequestReplyFuture<String, T, E> future = getReplyingKafkaTemplate().sendAndReceive(record);
        ConsumerRecord<String, E> consumerRecord = future.get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        return consumerRecord.value();
    }

}
