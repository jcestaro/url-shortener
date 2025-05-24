package com.github.jcestaro.url_shortener.infra.kafka.config.handler;

import com.github.jcestaro.url_shortener.infra.exception.BusinessException;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.backoff.FixedBackOff;

import java.util.logging.Logger;

@Component
public class KafkaErrorHandler {

    private static final Logger LOGGER = Logger.getLogger(KafkaErrorHandler.class.getName());

    @Bean
    public CommonErrorHandler commonErrorHandler() {
        ConsumerRecordRecoverer recoverer = (record, ex) -> LOGGER.severe("Failed to process record: " + record + ", exception: " + ex.getMessage());

        // Retry 3 times with 2s delay
        FixedBackOff backOff = new FixedBackOff(2000L, 3);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.setCommitRecovered(false);
        errorHandler.addNotRetryableExceptions(BusinessException.class);

        return errorHandler;
    }
}
