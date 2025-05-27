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
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@Configuration
public class KafkaConfig {

    private static final String REPLY = "-reply";

    @Value("${kafka.topic.requestreply.shorturlcreator.reply}")
    private String replyShortUrlCreatorTopic;

    @Value("${kafka.topic.requestreply.findurl.reply}")
    private String replyFindUrlTopic;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private final KafkaGenericFactory kafkaGenericFactory;

    @Autowired
    public KafkaConfig(KafkaGenericFactory kafkaGenericFactory) {
        this.kafkaGenericFactory = kafkaGenericFactory;
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
        }, groupId);
    }

    @Bean
    public ConsumerFactory<String, Response<UrlMapping>> consumerFactoryUrlMappingResponse() {
        return kafkaGenericFactory.genericConsumerFactory(new TypeReference<>() {
        }, groupId + REPLY);
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
            ConsumerFactory<String, String> plainStringConsumerFactory,
            KafkaTemplate<String, Response<UrlMapping>> replyKafkaTemplateForResponseUrlMapping
    ) {
        return kafkaGenericFactory.genericKafkaListenerFactory(
                plainStringConsumerFactory,
                replyKafkaTemplateForResponseUrlMapping
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, Response<UrlMapping>> repliesContainerUrlMappingCreator() {
        return kafkaGenericFactory.genericRepliesContainer(
                consumerFactoryUrlMappingResponse(),
                replyShortUrlCreatorTopic,
                groupId + REPLY
        );
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, Response<UrlMapping>> repliesContainerUrlMappingFinder() {
        return kafkaGenericFactory.genericRepliesContainer(
                consumerFactoryUrlMappingResponse(),
                replyFindUrlTopic,
                groupId + REPLY
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