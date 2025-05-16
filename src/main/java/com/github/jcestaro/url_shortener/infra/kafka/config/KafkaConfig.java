package com.github.jcestaro.url_shortener.infra.kafka.config;

import com.github.jcestaro.url_shortener.infra.kafka.config.factory.KafkaGenericFactory;
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

    @Value("${kafka.topic.requestreply.request}")
    private String requestTopic;

    @Value("${kafka.topic.requestreply.reply}")
    private String replyTopic;

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
    public ProducerFactory<String, UrlMapping> producerFactoryUrlMapping() {
        return kafkaGenericFactory.genericProducerFactory();
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplateString() {
        return kafkaGenericFactory.genericKafkaTemplate(producerFactoryString());
    }

    @Bean
    public KafkaTemplate<String, UrlMapping> kafkaTemplateUrlMapping() {
        return kafkaGenericFactory.genericKafkaTemplate(producerFactoryUrlMapping());
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactoryString() {
        return kafkaGenericFactory.genericConsumerFactory(String.class, groupId);
    }

    @Bean
    public ConsumerFactory<String, UrlMapping> consumerFactoryUrlMapping() {
        return kafkaGenericFactory.genericConsumerFactory(UrlMapping.class, groupId + REPLY);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactoryString(
            ConsumerFactory<String, String> consumerFactoryString,
            KafkaTemplate<String, UrlMapping> kafkaTemplateUrlMapping
    ) {
        return kafkaGenericFactory.genericKafkaListenerFactory(consumerFactoryString, kafkaTemplateUrlMapping);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, UrlMapping> repliesContainerUrlMapping() {
        return kafkaGenericFactory.genericRepliesContainer(
                consumerFactoryUrlMapping(),
                replyTopic,
                groupId + REPLY
        );
    }

    @Bean
    public ReplyingKafkaTemplate<String, String, UrlMapping> replyingKafkaTemplateUrlMapping() {
        return kafkaGenericFactory.genericReplyingKafkaTemplate(
                producerFactoryString(),
                repliesContainerUrlMapping()
        );
    }

}
