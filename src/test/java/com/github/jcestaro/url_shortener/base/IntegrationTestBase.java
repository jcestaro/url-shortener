package com.github.jcestaro.url_shortener.base;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTestBase {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:6.0").withExposedPorts(27017);

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // MongoDB properties
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);

        // Kafka properties
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("kafka.topic.requestreply.request", () -> "url.shortener.request");
        registry.add("kafka.topic.requestreply.reply", () -> "url.shortener.reply");
        registry.add("spring.kafka.consumer.group-id", () -> "url-shortener-group");
    }

}
