package com.github.jcestaro.url_shortener.infra.kafka.config.factory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.jcestaro.url_shortener.infra.kafka.config.response.Response;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaGenericFactoryTest {

    @InjectMocks
    private KafkaGenericFactory kafkaGenericFactory;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kafkaGenericFactory, "bootstrapServers", "localhost:9092");
    }

    @Test
    @DisplayName("should create ProducerFactory with correct configuration")
    void shouldCreateGenericProducerFactoryWithMocking() {
        ProducerFactory<String, Object> factory = kafkaGenericFactory.genericProducerFactory();
        Map<String, Object> config = factory.getConfigurationProperties();

        assertThat(config)
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
                .containsEntry(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class)
                .containsEntry(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    }

    @Test
    @DisplayName("should create ConsumerFactory with correct configuration")
    void shouldCreateGenericConsumerFactoryWithMocking() {
        var groupId = "mock-group";
        ConsumerFactory<String, String> consumerFactory = kafkaGenericFactory.genericConsumerFactory(new TypeReference<>() {
        }, groupId);

        Map<String, Object> config = consumerFactory.getConfigurationProperties();

        assertThat(config.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(config.get(ConsumerConfig.GROUP_ID_CONFIG)).isEqualTo(groupId);
        assertThat(config.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)).isEqualTo(StringDeserializer.class);
        assertThat(config.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG)).isEqualTo(JsonDeserializer.class);
    }

    @Test
    @DisplayName("should create KafkaTemplate using mocked ProducerFactory")
    void shouldCreateKafkaTemplate() {
        ProducerFactory<String, String> pf = mock(ProducerFactory.class);
        KafkaTemplate<String, String> template = kafkaGenericFactory.genericKafkaTemplate(pf);
        assertThat(template).isNotNull();
    }

    @Test
    @DisplayName("should create listener container with expected group and topic")
    void shouldCreateGenericRepliesContainer() {
        ConsumerFactory<String, Response<String>> cf = mock(ConsumerFactory.class);
        var container = kafkaGenericFactory.genericRepliesContainer(cf, "reply-topic", "reply-group");

        assertThat(container).isInstanceOf(ConcurrentMessageListenerContainer.class);
        assertThat(container.isAutoStartup()).isFalse();
        assertThat(container.getContainerProperties().getGroupId()).isEqualTo("reply-group");
    }

    @Test
    @DisplayName("should create replying kafka template with mocked container")
    void shouldCreateReplyingKafkaTemplate() {
        ProducerFactory<String, String> pf = mock(ProducerFactory.class);
        ConcurrentMessageListenerContainer<String, Response<String>> repliesContainer = mock(ConcurrentMessageListenerContainer.class);

        when(repliesContainer.getContainerProperties()).thenReturn(mock(ContainerProperties.class));

        ReplyingKafkaTemplate<String, String, Response<String>> template = kafkaGenericFactory.genericReplyingKafkaTemplate(pf, repliesContainer);

        assertThat(template).isNotNull();
    }
}
