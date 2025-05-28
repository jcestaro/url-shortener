package com.github.jcestaro.url_shortener.infra.kafka.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.jcestaro.url_shortener.infra.kafka.config.factory.KafkaGenericFactory;
import com.github.jcestaro.url_shortener.infra.kafka.config.response.Response;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaConfigTest {

    private static final String SHORT_URL_REPLY = "url-shortener-short-url-reply";
    private static final String FIND_URL_REPLY = "url-shortener-find-url-reply";
    private static final String TEST_GROUP = "test-group";
    private static final String TEST_GROUP_REPLY = "test-group-reply";

    @InjectMocks
    private KafkaConfig kafkaConfig;

    @Mock
    private KafkaGenericFactory kafkaGenericFactory;

    @Mock
    private ProducerFactory<String, String> mockProducerFactoryString;

    @Mock
    private KafkaTemplate<String, String> mockKafkaTemplateString;

    @Mock
    private ConsumerFactory<String, String> mockConsumerFactoryPlainString;

    @Mock
    private ConsumerFactory<String, Response<UrlMapping>> mockConsumerFactoryUrlMappingResponse;

    @Mock
    private ProducerFactory<String, Response<UrlMapping>> mockProducerFactoryResponseUrlMapping;

    @Mock
    private KafkaTemplate<String, Response<UrlMapping>> mockReplyKafkaTemplateForResponseUrlMapping;

    @Mock
    private ConcurrentKafkaListenerContainerFactory<String, String> mockKafkaListenerContainerFactoryString;

    @Mock
    private ConcurrentMessageListenerContainer<String, Response<UrlMapping>> mockRepliesContainer;

    @Mock
    private ReplyingKafkaTemplate<String, String, Response<UrlMapping>> mockReplyingKafkaTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(kafkaConfig, "replyShortUrlCreatorTopic", SHORT_URL_REPLY);
        ReflectionTestUtils.setField(kafkaConfig, "replyFindUrlTopic", FIND_URL_REPLY);
        ReflectionTestUtils.setField(kafkaConfig, "groupId", TEST_GROUP);
    }

    @Test
    @DisplayName("Should create ProducerFactory<String, String>")
    void shouldCreateProducerFactoryString() {
        when(kafkaGenericFactory.<String>genericProducerFactory()).thenReturn(mockProducerFactoryString);

        ProducerFactory<String, String> result = kafkaConfig.producerFactoryString();

        assertThat(result).isSameAs(mockProducerFactoryString);
        verify(kafkaGenericFactory).genericProducerFactory();
    }

    @Test
    @DisplayName("Should create KafkaTemplate<String, String>")
    void shouldCreateKafkaTemplateString() {
        when(kafkaGenericFactory.<String>genericProducerFactory()).thenReturn(mockProducerFactoryString);
        when(kafkaGenericFactory.genericKafkaTemplate(mockProducerFactoryString)).thenReturn(mockKafkaTemplateString);

        KafkaTemplate<String, String> result = kafkaConfig.kafkaTemplateString();

        assertThat(result).isSameAs(mockKafkaTemplateString);
        verify(kafkaGenericFactory).genericProducerFactory();
        verify(kafkaGenericFactory).genericKafkaTemplate(mockProducerFactoryString);
    }

    @Test
    @DisplayName("Should create ConsumerFactory<String, String>")
    void shouldCreateConsumerFactoryString() {
        when(kafkaGenericFactory.genericConsumerFactory(any(TypeReference.class), eq(TEST_GROUP))).thenReturn(mockConsumerFactoryPlainString);

        ConsumerFactory<String, String> result = kafkaConfig.consumerFactoryString();

        assertThat(result).isSameAs(mockConsumerFactoryPlainString);
        verify(kafkaGenericFactory).genericConsumerFactory(any(TypeReference.class), eq(TEST_GROUP));
    }

    @Test
    @DisplayName("Should create ConsumerFactory<String, Response<UrlMapping>> for replies")
    void shouldCreateConsumerFactoryUrlMappingResponse() {
        when(kafkaGenericFactory.genericConsumerFactory(any(TypeReference.class), eq(TEST_GROUP_REPLY))).thenReturn(mockConsumerFactoryUrlMappingResponse);

        ConsumerFactory<String, Response<UrlMapping>> result = kafkaConfig.consumerFactoryUrlMappingResponse();

        assertThat(result).isSameAs(mockConsumerFactoryUrlMappingResponse);
        verify(kafkaGenericFactory).genericConsumerFactory(any(TypeReference.class), eq(TEST_GROUP_REPLY));
    }

    @Test
    @DisplayName("Should create ProducerFactory<String, Response<UrlMapping>>")
    void shouldCreateProducerFactoryResponseUrlMapping() {
        when(kafkaGenericFactory.<Response<UrlMapping>>genericProducerFactory()).thenReturn(mockProducerFactoryResponseUrlMapping);

        ProducerFactory<String, Response<UrlMapping>> result = kafkaConfig.producerFactoryResponseUrlMapping();

        assertThat(result).isSameAs(mockProducerFactoryResponseUrlMapping);
        verify(kafkaGenericFactory).genericProducerFactory();
    }

    @Test
    @DisplayName("Should create KafkaTemplate<String, Response<UrlMapping>> for replies")
    void shouldCreateReplyKafkaTemplateForResponseUrlMapping() {
        when(kafkaGenericFactory.<Response<UrlMapping>>genericProducerFactory()).thenReturn(mockProducerFactoryResponseUrlMapping);

        KafkaTemplate<String, Response<UrlMapping>> result = kafkaConfig.replyKafkaTemplateForResponseUrlMapping();

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(KafkaTemplate.class);
        verify(kafkaGenericFactory).genericProducerFactory();
    }

    @Test
    @DisplayName("Should create KafkaListenerContainerFactory<String, String>")
    void shouldCreateKafkaListenerContainerFactoryString() {
        when(kafkaGenericFactory.genericKafkaListenerFactory(mockConsumerFactoryPlainString, mockReplyKafkaTemplateForResponseUrlMapping)).thenReturn(mockKafkaListenerContainerFactoryString);

        ConcurrentKafkaListenerContainerFactory<String, String> result = kafkaConfig.kafkaListenerContainerFactoryString(mockConsumerFactoryPlainString, mockReplyKafkaTemplateForResponseUrlMapping);

        assertThat(result).isSameAs(mockKafkaListenerContainerFactoryString);
        verify(kafkaGenericFactory).genericKafkaListenerFactory(mockConsumerFactoryPlainString, mockReplyKafkaTemplateForResponseUrlMapping);
    }

    @Test
    @DisplayName("Should create repliesContainerUrlMappingCreator")
    void shouldCreateRepliesContainerUrlMappingCreator() {
        when(kafkaGenericFactory.genericConsumerFactory(any(TypeReference.class), eq(TEST_GROUP_REPLY))).thenReturn(mockConsumerFactoryUrlMappingResponse);
        when(kafkaGenericFactory.genericRepliesContainer(mockConsumerFactoryUrlMappingResponse, SHORT_URL_REPLY, TEST_GROUP_REPLY)).thenReturn(mockRepliesContainer);

        ConcurrentMessageListenerContainer<String, Response<UrlMapping>> result = kafkaConfig.repliesContainerUrlMappingCreator();

        assertThat(result).isSameAs(mockRepliesContainer);
        verify(kafkaGenericFactory).genericRepliesContainer(mockConsumerFactoryUrlMappingResponse, SHORT_URL_REPLY, TEST_GROUP_REPLY);
    }

    @Test
    @DisplayName("Should create repliesContainerUrlMappingFinder")
    void shouldCreateRepliesContainerUrlMappingFinder() {
        when(kafkaGenericFactory.genericConsumerFactory(any(TypeReference.class), eq(TEST_GROUP_REPLY))).thenReturn(mockConsumerFactoryUrlMappingResponse);

        when(kafkaGenericFactory.genericRepliesContainer(mockConsumerFactoryUrlMappingResponse, FIND_URL_REPLY, TEST_GROUP_REPLY)).thenReturn(mockRepliesContainer);

        ConcurrentMessageListenerContainer<String, Response<UrlMapping>> result =
                kafkaConfig.repliesContainerUrlMappingFinder();

        assertThat(result).isSameAs(mockRepliesContainer);
        verify(kafkaGenericFactory).genericRepliesContainer(mockConsumerFactoryUrlMappingResponse, FIND_URL_REPLY, TEST_GROUP_REPLY);
    }

    @Test
    @DisplayName("Should create replyingKafkaTemplateUrlMappingCreator")
    void shouldCreateReplyingKafkaTemplateUrlMappingCreator() {
        when(kafkaGenericFactory.<String>genericProducerFactory()).thenReturn(mockProducerFactoryString);
        when(kafkaGenericFactory.genericConsumerFactory(any(TypeReference.class), eq(TEST_GROUP_REPLY))).thenReturn(mockConsumerFactoryUrlMappingResponse);
        when(kafkaGenericFactory.genericRepliesContainer(mockConsumerFactoryUrlMappingResponse, SHORT_URL_REPLY, TEST_GROUP_REPLY)).thenReturn(mockRepliesContainer);
        when(kafkaGenericFactory.genericReplyingKafkaTemplate(mockProducerFactoryString, mockRepliesContainer)).thenReturn(mockReplyingKafkaTemplate);

        ReplyingKafkaTemplate<String, String, Response<UrlMapping>> result = kafkaConfig.replyingKafkaTemplateUrlMappingCreator();

        assertThat(result).isSameAs(mockReplyingKafkaTemplate);
        verify(kafkaGenericFactory).genericReplyingKafkaTemplate(mockProducerFactoryString, mockRepliesContainer);
    }

    @Test
    @DisplayName("Should create replyingKafkaTemplateUrlMappingFinder")
    void shouldCreateReplyingKafkaTemplateUrlMappingFinder() {
        when(kafkaGenericFactory.<String>genericProducerFactory()).thenReturn(mockProducerFactoryString);
        when(kafkaGenericFactory.genericConsumerFactory(any(TypeReference.class), eq(TEST_GROUP_REPLY))).thenReturn(mockConsumerFactoryUrlMappingResponse);
        when(kafkaGenericFactory.genericRepliesContainer(mockConsumerFactoryUrlMappingResponse, FIND_URL_REPLY, TEST_GROUP_REPLY)).thenReturn(mockRepliesContainer);
        when(kafkaGenericFactory.genericReplyingKafkaTemplate(mockProducerFactoryString, mockRepliesContainer)).thenReturn(mockReplyingKafkaTemplate);

        ReplyingKafkaTemplate<String, String, Response<UrlMapping>> result = kafkaConfig.replyingKafkaTemplateUrlMappingFinder();

        assertThat(result).isSameAs(mockReplyingKafkaTemplate);
        verify(kafkaGenericFactory).genericReplyingKafkaTemplate(mockProducerFactoryString, mockRepliesContainer);
    }
}