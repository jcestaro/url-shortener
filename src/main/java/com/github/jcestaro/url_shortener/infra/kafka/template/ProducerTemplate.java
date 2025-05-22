package com.github.jcestaro.url_shortener.infra.kafka.template;

import com.github.jcestaro.url_shortener.infra.kafka.config.KafkaConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class ProducerTemplate<T, E> {

    private static final int DEFAULT_TIMEOUT_SECONDS = 20;

    @Autowired
    protected KafkaConfig kafkaConfig;

    protected abstract String getKey();

    protected abstract String getRequestTopic();

    protected abstract ReplyingKafkaTemplate<String, T, E> getReplyingKafkaTemplate();

    public E sendMessage(T message) throws Exception {
        ProducerRecord<String, T> record = new ProducerRecord<>(getRequestTopic(), getKey(), message);
        RequestReplyFuture<String, T, E> future = getReplyingKafkaTemplate().sendAndReceive(record);

        try {
            ConsumerRecord<String, E> consumerRecord = future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return consumerRecord.value();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Process interrupted while waiting for kafka´s answer", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Timeout (" + DEFAULT_TIMEOUT_SECONDS + "s) waiting for kafka´s reply for the topic: " + getRequestTopic(), e);
        } catch (ExecutionException e) {
            handleRealException(e);
            throw new RuntimeException("Error processing Kafka´s reply (ExecutionException): " + e.getMessage(), e);
        }
    }

    private void handleRealException(ExecutionException e) throws Exception {
        Throwable cause = e.getCause();
        e.printStackTrace();

        if (cause instanceof ListenerExecutionFailedException) {
            Throwable listenerActualCause = cause.getCause();

            if (listenerActualCause instanceof RuntimeException) {
                throw (RuntimeException) listenerActualCause;
            } else if (listenerActualCause instanceof Exception) {
                throw (Exception) listenerActualCause;
            }

            throw new RuntimeException("Listener Kafka failed (ListenerExecutionFailedException without detailed cause): " + cause.getMessage(), cause);
        }
    }

}
