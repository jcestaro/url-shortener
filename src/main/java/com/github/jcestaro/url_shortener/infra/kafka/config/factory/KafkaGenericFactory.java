package com.github.jcestaro.url_shortener.infra.kafka.config.factory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jcestaro.url_shortener.infra.kafka.config.response.Response;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KafkaGenericFactory {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final ObjectMapper objectMapper;

    @Autowired
    public KafkaGenericFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <V> ProducerFactory<String, V> genericProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        JsonSerializer<V> jsonSerializer = new JsonSerializer<>(this.objectMapper);
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), jsonSerializer);
    }

    public <V> ConsumerFactory<String, V> genericConsumerFactory(TypeReference<V> valueClass, String consumerGroupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        JsonDeserializer<V> deserializer = new JsonDeserializer<>(valueClass, this.objectMapper, false);

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

    public <K, R> ConcurrentMessageListenerContainer<K, Response<R>> genericRepliesContainer(
            ConsumerFactory<K, Response<R>> cf,
            String topic,
            String replyGroupId
    ) {
        ConcurrentKafkaListenerContainerFactory<K, Response<R>> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        factory.getContainerProperties().setGroupId(replyGroupId);

        ConcurrentMessageListenerContainer<K, Response<R>> container = factory.createContainer(topic);
        container.setAutoStartup(true);
        return container;
    }

    public <K, V, R> ReplyingKafkaTemplate<K, V, Response<R>> genericReplyingKafkaTemplate(
            ProducerFactory<K, V> pf,
            ConcurrentMessageListenerContainer<K, Response<R>> repliesContainer
    ) {
        return new ReplyingKafkaTemplate<>(pf, repliesContainer);
    }

}
