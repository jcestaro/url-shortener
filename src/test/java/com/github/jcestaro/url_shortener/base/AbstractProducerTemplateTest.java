package com.github.jcestaro.url_shortener.base;

import com.github.jcestaro.url_shortener.infra.kafka.config.KafkaConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Base test class for services extending ProducerTemplate.
 */
public abstract class AbstractProducerTemplateTest<T, E> {

    @Mock
    protected ReplyingKafkaTemplate<String, T, E> replyingKafkaTemplate;

    @Mock
    protected RequestReplyFuture<String, T, E> requestReplyFuture;

    @Mock
    protected ConsumerRecord<String, E> consumerRecord;

    @Mock
    protected KafkaConfig kafkaConfig;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(replyingKafkaTemplate.sendAndReceive(any(ProducerRecord.class))).thenReturn(requestReplyFuture);
        when(requestReplyFuture.get(anyLong(), any())).thenReturn(consumerRecord);

        configureMocks();
    }

    protected abstract void configureMocks();

}
