package com.github.jcestaro.url_shortener.infra.kafka.config;

import com.github.jcestaro.url_shortener.infra.kafka.config.factory.KafkaGenericFactory;
import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaConfigTest {

    @InjectMocks
    private KafkaConfig kafkaConfig;

    @Mock
    private KafkaGenericFactory kafkaGenericFactory;

    @Mock
    private ProducerFactory<String, String> producerFactoryString;

    @Mock
    private ProducerFactory<String, UrlMapping> producerFactoryUrlMapping;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplateString;

    @Mock
    private KafkaTemplate<String, UrlMapping> kafkaTemplateUrlMapping;

    @Mock
    private ConsumerFactory<String, String> consumerFactoryString;

    @Mock
    private ConsumerFactory<String, UrlMapping> consumerFactoryUrlMapping;

    @Mock
    private ConcurrentKafkaListenerContainerFactory<String, String> listenerFactory;

    @Mock
    private ConcurrentMessageListenerContainer<String, UrlMapping> repliesContainer;

    @Mock
    private ReplyingKafkaTemplate<String, String, UrlMapping> replyingKafkaTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(kafkaConfig, "requestTopic", "url-shortener-request");
        ReflectionTestUtils.setField(kafkaConfig, "replyTopic", "url-shortener-reply");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "test-group");
    }

    @Test
    @DisplayName("Should create ProducerFactory<String, String>")
    void shouldCreateProducerFactoryString() {
        when(kafkaGenericFactory.<String, String>genericProducerFactory()).thenReturn(producerFactoryString);

        ProducerFactory<String, String> result = kafkaConfig.producerFactoryString();

        assertThat(result).isSameAs(producerFactoryString);
        verify(kafkaGenericFactory).genericProducerFactory();
    }

    @Test
    @DisplayName("Should create ProducerFactory<String, UrlMapping>")
    void shouldCreateProducerFactoryUrlMapping() {
        when(kafkaGenericFactory.<String, UrlMapping>genericProducerFactory()).thenReturn(producerFactoryUrlMapping);

        ProducerFactory<String, UrlMapping> result = kafkaConfig.producerFactoryUrlMapping();

        assertThat(result).isSameAs(producerFactoryUrlMapping);
        verify(kafkaGenericFactory).genericProducerFactory();
    }

    @Test
    @DisplayName("Should create KafkaTemplate<String, String>")
    void shouldCreateKafkaTemplateString() {
        when(kafkaGenericFactory.<String, String>genericProducerFactory()).thenReturn(producerFactoryString);
        when(kafkaGenericFactory.genericKafkaTemplate(producerFactoryString)).thenReturn(kafkaTemplateString);

        KafkaTemplate<String, String> result = kafkaConfig.kafkaTemplateString();

        assertThat(result).isSameAs(kafkaTemplateString);
        verify(kafkaGenericFactory).genericKafkaTemplate(producerFactoryString);
    }

    @Test
    @DisplayName("Should create KafkaTemplate<String, UrlMapping>")
    void shouldCreateKafkaTemplateUrlMapping() {
        when(kafkaGenericFactory.<String, UrlMapping>genericProducerFactory()).thenReturn(producerFactoryUrlMapping);
        when(kafkaGenericFactory.genericKafkaTemplate(producerFactoryUrlMapping)).thenReturn(kafkaTemplateUrlMapping);

        KafkaTemplate<String, UrlMapping> result = kafkaConfig.kafkaTemplateUrlMapping();

        assertThat(result).isSameAs(kafkaTemplateUrlMapping);
        verify(kafkaGenericFactory).genericKafkaTemplate(producerFactoryUrlMapping);
    }

    @Test
    @DisplayName("Should create ConsumerFactory<String, String>")
    void shouldCreateConsumerFactoryString() {
        when(kafkaGenericFactory.genericConsumerFactory(String.class, "test-group")).thenReturn(consumerFactoryString);

        ConsumerFactory<String, String> result = kafkaConfig.consumerFactoryString();

        assertThat(result).isSameAs(consumerFactoryString);
        verify(kafkaGenericFactory).genericConsumerFactory(String.class, "test-group");
    }

    @Test
    @DisplayName("Should create ConsumerFactory<String, UrlMapping>")
    void shouldCreateConsumerFactoryUrlMapping() {
        when(kafkaGenericFactory.genericConsumerFactory(UrlMapping.class, "test-group-reply")).thenReturn(consumerFactoryUrlMapping);

        ConsumerFactory<String, UrlMapping> result = kafkaConfig.consumerFactoryUrlMapping();

        assertThat(result).isSameAs(consumerFactoryUrlMapping);
        verify(kafkaGenericFactory).genericConsumerFactory(UrlMapping.class, "test-group-reply");
    }

    @Test
    @DisplayName("Should create KafkaListenerContainerFactory<String, String>")
    void shouldCreateKafkaListenerContainerFactoryString() {
        when(kafkaGenericFactory.genericKafkaListenerFactory(consumerFactoryString, kafkaTemplateUrlMapping)).thenReturn(listenerFactory);

        ConcurrentKafkaListenerContainerFactory<String, String> result = kafkaConfig.kafkaListenerContainerFactoryString(consumerFactoryString, kafkaTemplateUrlMapping);

        assertThat(result).isSameAs(listenerFactory);
        verify(kafkaGenericFactory).genericKafkaListenerFactory(consumerFactoryString, kafkaTemplateUrlMapping);
    }

    @Test
    @DisplayName("Should create repliesContainerUrlMapping")
    void shouldCreateRepliesContainerUrlMapping() {
        when(kafkaGenericFactory.genericConsumerFactory(UrlMapping.class, "test-group-reply")).thenReturn(consumerFactoryUrlMapping);
        when(kafkaGenericFactory.genericRepliesContainer(consumerFactoryUrlMapping, "url-shortener-reply", "test-group-reply")).thenReturn(repliesContainer);

        ConcurrentMessageListenerContainer<String, UrlMapping> result = kafkaConfig.repliesContainerUrlMapping();

        assertThat(result).isSameAs(repliesContainer);
        verify(kafkaGenericFactory).genericRepliesContainer(consumerFactoryUrlMapping, "url-shortener-reply", "test-group-reply");
    }

    @Test
    @DisplayName("Should create replyingKafkaTemplateUrlMapping")
    void shouldCreateReplyingKafkaTemplateUrlMapping() {
        when(kafkaGenericFactory.<String, String>genericProducerFactory()).thenReturn(producerFactoryString);
        when(kafkaGenericFactory.genericConsumerFactory(UrlMapping.class, "test-group-reply")).thenReturn(consumerFactoryUrlMapping);
        when(kafkaGenericFactory.genericRepliesContainer(consumerFactoryUrlMapping, "url-shortener-reply", "test-group-reply")).thenReturn(repliesContainer);
        when(kafkaGenericFactory.genericReplyingKafkaTemplate(producerFactoryString, repliesContainer)).thenReturn(replyingKafkaTemplate);

        ReplyingKafkaTemplate<String, String, UrlMapping> result = kafkaConfig.replyingKafkaTemplateUrlMapping();

        assertThat(result).isSameAs(replyingKafkaTemplate);
        verify(kafkaGenericFactory).genericReplyingKafkaTemplate(producerFactoryString, repliesContainer);
    }
}
