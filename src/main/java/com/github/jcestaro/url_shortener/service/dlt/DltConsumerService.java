package com.github.jcestaro.url_shortener.service.dlt;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class DltConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DltConsumerService.class);
    private static final String DLT_CONSUMER_GROUP_ID = "${spring.kafka.consumer.group-id}.DLT";

    @KafkaListener(
            topics = "${kafka.topic.requestreply.shorturlcreator.request}.DLT",
            groupId = DLT_CONSUMER_GROUP_ID,
            containerFactory = "dltKafkaListenerContainerFactory"
    )
    public void consumeCreateUrlRequestDlt(ConsumerRecord<String, String> record,
                                           @Header(KafkaHeaders.RECEIVED_TOPIC) String receivedTopic,
                                           @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionFqcn,
                                           @Header(KafkaHeaders.DLT_EXCEPTION_STACKTRACE) String exceptionStacktrace,
                                           @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                           @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic,
                                           @Header(KafkaHeaders.DLT_ORIGINAL_PARTITION) int originalPartition,
                                           @Header(KafkaHeaders.DLT_ORIGINAL_OFFSET) long originalOffset) {

        LOGGER.error("""
                        DLT Received (CreateUrlRequest):
                          DLT Topic: {}
                          Original Topic: {}-{}@{}
                          Exception: {} - {}
                          Payload: {}
                          Stacktrace: {}""",
                receivedTopic,
                originalTopic, originalPartition, originalOffset,
                exceptionFqcn, exceptionMessage,
                record.value(),
                exceptionStacktrace);
    }

    @KafkaListener(
            topics = "${kafka.topic.requestreply.findurl.request}.DLT",
            groupId = DLT_CONSUMER_GROUP_ID,
            containerFactory = "dltKafkaListenerContainerFactory"
    )
    public void consumeFindUrlRequestDlt(ConsumerRecord<String, String> record,
                                         @Header(KafkaHeaders.RECEIVED_TOPIC) String receivedTopic,
                                         @Header(KafkaHeaders.DLT_EXCEPTION_FQCN) String exceptionFqcn,
                                         @Header(KafkaHeaders.DLT_EXCEPTION_STACKTRACE) String exceptionStacktrace,
                                         @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage,
                                         @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic,
                                         @Header(KafkaHeaders.DLT_ORIGINAL_PARTITION) int originalPartition,
                                         @Header(KafkaHeaders.DLT_ORIGINAL_OFFSET) long originalOffset) {
        LOGGER.error("""
                        DLT Received (FindUrlRequest):
                          DLT Topic: {}
                          Original Topic: {}-{}@{}
                          Exception: {} - {}
                          Payload: {}
                          Stacktrace: {}""",
                receivedTopic,
                originalTopic, originalPartition, originalOffset,
                exceptionFqcn, exceptionMessage,
                record.value(),
                exceptionStacktrace);
    }

}