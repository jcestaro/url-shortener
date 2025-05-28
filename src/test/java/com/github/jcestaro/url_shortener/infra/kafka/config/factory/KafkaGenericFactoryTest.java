package com.github.jcestaro.url_shortener.infra.kafka.config.factory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
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

    @Mock
    private ObjectMapper mockObjectMapper;

    @InjectMocks
    private KafkaGenericFactory kafkaGenericFactory;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kafkaGenericFactory, "bootstrapServers", "localhost:9092");
    }

    @Test
    @DisplayName("should create ProducerFactory with correct configuration")
    void shouldCreateGenericProducerFactory() {
        ProducerFactory<String, Object> factory = kafkaGenericFactory.genericProducerFactory();
        assertThat(factory).isNotNull();
        assertThat(factory).isInstanceOf(DefaultKafkaProducerFactory.class);

        Map<String, Object> config = factory.getConfigurationProperties();
        assertThat(config)
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
                .containsEntry(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class)
                .containsKey(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)
                .satisfies(valueConfig -> assertThat(valueConfig.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)).isEqualTo(JsonSerializer.class));
    }

    @Test
    @DisplayName("should create ConsumerFactory with correct configuration and use injected ObjectMapper")
    void shouldCreateGenericConsumerFactory() {
        String groupId = "mock-group";
        TypeReference<String> typeReference = new TypeReference<>() {
        };

        ConsumerFactory<String, String> consumerFactory = kafkaGenericFactory.genericConsumerFactory(typeReference, groupId);

        assertThat(consumerFactory).isNotNull();
        assertThat(consumerFactory).isInstanceOf(DefaultKafkaConsumerFactory.class);

        Map<String, Object> config = consumerFactory.getConfigurationProperties();
        assertThat(config.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo("localhost:9092");
        assertThat(config.get(ConsumerConfig.GROUP_ID_CONFIG)).isEqualTo(groupId);
        assertThat(config.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)).isEqualTo(StringDeserializer.class);
        assertThat(config.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG)).isEqualTo(JsonDeserializer.class);
    }

    @Test
    @DisplayName("should create KafkaTemplate using provided ProducerFactory")
    void shouldCreateKafkaTemplate() {
        ProducerFactory<String, String> mockPf = mock(ProducerFactory.class);
        KafkaTemplate<String, String> template = kafkaGenericFactory.genericKafkaTemplate(mockPf);
        assertThat(template).isNotNull();
    }

    @Test
    @DisplayName("should create ConcurrentKafkaListenerContainerFactory with provided ConsumerFactory and ReplyTemplate")
    void shouldCreateGenericKafkaListenerFactory() {
        ConsumerFactory<String, String> mockConsumerFactory = mock(ConsumerFactory.class);
        KafkaTemplate<Object, Object> mockReplyTemplate = mock(KafkaTemplate.class);


        ConcurrentKafkaListenerContainerFactory<String, String> listenerFactory = kafkaGenericFactory.genericKafkaListenerFactory(mockConsumerFactory, mockReplyTemplate);


        assertThat(listenerFactory).isNotNull();
        assertThat(listenerFactory.getConsumerFactory()).isSameAs(mockConsumerFactory);
    }

    @Test
    @DisplayName("should create listener container with expected group, topic, and autoStartup true")
    void shouldCreateGenericRepliesContainer() {
        ConsumerFactory<String, Response<String>> mockCf = mock(ConsumerFactory.class);
        String replyTopic = "reply-topic";
        String replyGroupId = "reply-group";

        ConcurrentMessageListenerContainer<String, Response<String>> container = kafkaGenericFactory.genericRepliesContainer(mockCf, replyTopic, replyGroupId);

        assertThat(container).isInstanceOf(ConcurrentMessageListenerContainer.class);
        assertThat(container.isAutoStartup()).isTrue();
        assertThat(container.getContainerProperties().getGroupId()).isEqualTo(replyGroupId);
        assertThat(container.getContainerProperties().getTopics()).containsExactly(replyTopic);
    }

    @Test
    @DisplayName("should create replying kafka template with provided ProducerFactory and replies container")
    void shouldCreateReplyingKafkaTemplate() {
        ProducerFactory<String, String> mockPf = mock(ProducerFactory.class);
        ConcurrentMessageListenerContainer<String, Response<String>> mockRepliesContainer = mock(ConcurrentMessageListenerContainer.class);

        ContainerProperties mockContainerProperties = mock(ContainerProperties.class);
        when(mockRepliesContainer.getContainerProperties()).thenReturn(mockContainerProperties);

        ReplyingKafkaTemplate<String, String, Response<String>> template = kafkaGenericFactory.genericReplyingKafkaTemplate(mockPf, mockRepliesContainer);

        assertThat(template).isNotNull();
    }
}