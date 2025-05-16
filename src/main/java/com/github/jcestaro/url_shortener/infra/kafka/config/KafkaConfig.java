package com.github.jcestaro.url_shortener.infra.kafka.config;

import com.github.jcestaro.url_shortener.model.UrlMapping;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private static final String REPLY = "-reply";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.requestreply.request}")
    private String requestTopic;

    @Value("${kafka.topic.requestreply.reply}")
    private String replyTopic;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // -------------------- MÉTODOS GENÉRICOS --------------------
    public <K, V> ProducerFactory<K, V> genericProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    public <V> ConsumerFactory<String, V> genericConsumerFactory(Class<V> valueClass, String consumerGroupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        JsonDeserializer<V> deserializer = new JsonDeserializer<>(valueClass);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(false);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    public <K, V> KafkaTemplate<K, V> genericKafkaTemplate(ProducerFactory<K, V> pf) {
        return new KafkaTemplate<>(pf);
    }

    public <K, V> ConcurrentKafkaListenerContainerFactory<K, V> genericKafkaListenerFactory(
            ConsumerFactory<K, V> cf,
            KafkaTemplate<?, ?> replyTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        factory.setReplyTemplate(replyTemplate);
        return factory;
    }

    public <K, R> ConcurrentMessageListenerContainer<K, R> genericRepliesContainer(
            ConsumerFactory<K, R> cf,
            String topic,
            String replyGroupId
    ) {
        ConcurrentKafkaListenerContainerFactory<K, R> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        factory.getContainerProperties().setGroupId(replyGroupId);

        ConcurrentMessageListenerContainer<K, R> container = factory.createContainer(topic);
        container.setAutoStartup(false);
        return container;
    }

    public <K, V, R> ReplyingKafkaTemplate<K, V, R> genericReplyingKafkaTemplate(
            ProducerFactory<K, V> pf,
            ConcurrentMessageListenerContainer<K, R> repliesContainer
    ) {
        return new ReplyingKafkaTemplate<>(pf, repliesContainer);
    }

    // -------------------- BEANS CONCRETOS --------------------
    @Bean
    public ProducerFactory<String, String> producerFactoryString() {
        return genericProducerFactory();
    }

    @Bean
    public ProducerFactory<String, UrlMapping> producerFactoryUrlMapping() {
        return genericProducerFactory();
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplateString() {
        return genericKafkaTemplate(producerFactoryString());
    }

    @Bean
    public KafkaTemplate<String, UrlMapping> kafkaTemplateUrlMapping() {
        return genericKafkaTemplate(producerFactoryUrlMapping());
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactoryString() {
        return genericConsumerFactory(String.class, groupId);
    }

    @Bean
    public ConsumerFactory<String, UrlMapping> consumerFactoryUrlMapping() {
        return genericConsumerFactory(UrlMapping.class, groupId + REPLY);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactoryString(
            ConsumerFactory<String, String> consumerFactoryString,
            KafkaTemplate<String, UrlMapping> kafkaTemplateUrlMapping
    ) {
        return genericKafkaListenerFactory(consumerFactoryString, kafkaTemplateUrlMapping);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, UrlMapping> repliesContainerUrlMapping() {
        return genericRepliesContainer(
                consumerFactoryUrlMapping(),
                replyTopic,
                groupId + REPLY
        );
    }

    @Bean
    public ReplyingKafkaTemplate<String, String, UrlMapping> replyingKafkaTemplateUrlMapping() {
        return genericReplyingKafkaTemplate(
                producerFactoryString(),
                repliesContainerUrlMapping()
        );
    }

}
