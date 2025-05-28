package com.github.jcestaro.url_shortener.infra.kafka.config.handler;

import com.github.jcestaro.url_shortener.infra.exception.BusinessException;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;

@Component
public class KafkaErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaErrorHandler.class);
    private static final int KAFKAS_DEFAULT_PARTITION = -1;
    private final KafkaOperations<String, String> dltKafkaOperations;

    public KafkaErrorHandler(@Qualifier("dltKafkaTemplate") KafkaOperations<String, String> dltKafkaOperations) {
        this.dltKafkaOperations = dltKafkaOperations;
    }

    @Bean
    public CommonErrorHandler commonErrorHandler() {
        DefaultErrorHandler errorHandler = createDefaultErrorHandler();
        errorHandler.setCommitRecovered(true);
        errorHandler.addNotRetryableExceptions(BusinessException.class);

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                LOGGER.warn("Registry failed to process (try {}). Record: {}, Exception: {}", deliveryAttempt, record, ex.getMessage())
        );

        return errorHandler;
    }

    private DefaultErrorHandler createDefaultErrorHandler() {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dltKafkaOperations,
                (consumerRecord, _) -> {
                    LOGGER.warn("Sending topic´s message '{}' to DLT: {}.DLT", consumerRecord.topic(), consumerRecord.topic());
                    return new TopicPartition(consumerRecord.topic() + ".DLT", KAFKAS_DEFAULT_PARTITION);
                }
        );

        FixedBackOff backOff = new FixedBackOff(2000L, 2L);
        return new DefaultErrorHandler(recoverer, backOff);
    }

}