package com.github.jcestaro.url_shortener.infra.kafka.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.jcestaro.url_shortener.infra.kafka.config.factory.KafkaGenericFactory;
import com.github.jcestaro.url_shortener.infra.kafka.config.response.Response;
import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@Configuration
public class KafkaConfig {

    private static final String REPLY_SUFFIX = "-reply";

    @Value("${kafka.topic.requestreply.shorturlcreator.reply}")
    private String replyShortUrlCreatorTopic;

    @Value("${kafka.topic.requestreply.findurl.reply}")
    private String replyFindUrlTopic;

    @Value("${spring.kafka.consumer.group-id}")
    private String defaultGroupId;

    private final KafkaGenericFactory kafkaGenericFactory;
    private final CommonErrorHandler dlqCommonErrorHandler;

    @Autowired
    public KafkaConfig(KafkaGenericFactory kafkaGenericFactory, CommonErrorHandler dlqCommonErrorHandler) {
        this.kafkaGenericFactory = kafkaGenericFactory;
        this.dlqCommonErrorHandler = dlqCommonErrorHandler;
    }

    @Bean
    public ProducerFactory<String, String> producerFactoryString() {
        return kafkaGenericFactory.genericProducerFactory();
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplateString() {
        return kafkaGenericFactory.genericKafkaTemplate(producerFactoryString());
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactoryString() {
        return kafkaGenericFactory.genericConsumerFactory(new TypeReference<>() {
        }, defaultGroupId);
    }

    @Bean
    public ConsumerFactory<String, Response<UrlMapping>> consumerFactoryUrlMappingResponse() {
        return kafkaGenericFactory.genericConsumerFactory(new TypeReference<>() {
        }, defaultGroupId + REPLY_SUFFIX);
    }

    @Bean
    public ProducerFactory<String, Response<UrlMapping>> producerFactoryResponseUrlMapping() {
        return kafkaGenericFactory.genericProducerFactory();
    }

    @Bean
    public KafkaTemplate<String, Response<UrlMapping>> replyKafkaTemplateForResponseUrlMapping() {
        return new KafkaTemplate<>(producerFactoryResponseUrlMapping());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactoryString(
            ConsumerFactory<String, String> consumerFactoryString,
            KafkaTemplate<String, Response<UrlMapping>> replyKafkaTemplateForResponseUrlMapping
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                kafkaGenericFactory.genericKafkaListenerFactory(
                        consumerFactoryString,
                        replyKafkaTemplateForResponseUrlMapping
                );

        factory.setCommonErrorHandler(dlqCommonErrorHandler);
        return factory;
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, Response<UrlMapping>> repliesContainerUrlMappingCreator() {
        return kafkaGenericFactory.genericRepliesContainer(
                consumerFactoryUrlMappingResponse(),
                replyShortUrlCreatorTopic,
                defaultGroupId + REPLY_SUFFIX
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, Response<UrlMapping>> repliesContainerUrlMappingFinder() {
        return kafkaGenericFactory.genericRepliesContainer(
                consumerFactoryUrlMappingResponse(),
                replyFindUrlTopic,
                defaultGroupId + REPLY_SUFFIX
        );
    }

    @Bean
    public ReplyingKafkaTemplate<String, String, Response<UrlMapping>> replyingKafkaTemplateUrlMappingCreator() {
        return kafkaGenericFactory.genericReplyingKafkaTemplate(
                producerFactoryString(),
                repliesContainerUrlMappingCreator()
        );
    }

    @Bean
    public ReplyingKafkaTemplate<String, String, Response<UrlMapping>> replyingKafkaTemplateUrlMappingFinder() {
        return kafkaGenericFactory.genericReplyingKafkaTemplate(
                producerFactoryString(),
                repliesContainerUrlMappingFinder()
        );
    }

}