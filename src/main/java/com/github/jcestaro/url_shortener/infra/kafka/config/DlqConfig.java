package com.github.jcestaro.url_shortener.infra.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DlqConfig {

    @Value("${kafka.topic.requestreply.shorturlcreator.request}")
    private String createUrlRequestTopic;

    @Value("${kafka.topic.requestreply.findurl.request}")
    private String findUrlRequestTopic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.requestreply.shorturlcreator.partitions:1}")
    private int shortUrlCreatorPartitions;

    @Value("${kafka.topic.requestreply.shorturlcreator.replicas:1}")
    private int shortUrlCreatorReplicas;

    @Value("${kafka.topic.requestreply.findurl.partitions:1}")
    private int findUrlPartitions;

    @Value("${kafka.topic.requestreply.findurl.replicas:1}")
    private int findUrlReplicas;

    @Bean
    public ProducerFactory<String, String> producerFactoryStringDlt() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> dltKafkaTemplate(@Qualifier("producerFactoryStringDlt") ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public NewTopic createUrlRequestDltTopic() {
        return TopicBuilder.name(createUrlRequestTopic + ".DLT")
                .partitions(shortUrlCreatorPartitions)
                .replicas(shortUrlCreatorReplicas)
                .build();
    }

    @Bean
    public NewTopic findUrlRequestDltTopic() {
        return TopicBuilder.name(findUrlRequestTopic + ".DLT")
                .partitions(findUrlPartitions)
                .replicas(findUrlReplicas)
                .build();
    }

    @Bean
    public ConsumerFactory<String, String> dltConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> dltKafkaListenerContainerFactory(
            @Qualifier("dltConsumerFactory") ConsumerFactory<String, String> dltConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(dltConsumerFactory);
        return factory;
    }

}
