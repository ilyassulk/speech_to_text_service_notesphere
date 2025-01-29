package ru.noteshphere.speech_to_text_manager.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.topic.name}")
    private String topicName;

    @Value("${spring.kafka.topic.partitions}")
    private int partitions;

    @Value("${spring.kafka.topic.replication-factor}")
    private short replicationFactor;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        return new KafkaAdmin(Collections.singletonMap(
                "bootstrap.servers", bootstrapServers
        ));
    }

    @Bean
    public CommandLineRunner createKafkaTopic(KafkaAdmin kafkaAdmin) {
        return args -> {
            try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
                if (!adminClient.listTopics().names().get().contains(topicName)) {
                    NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
                    adminClient.createTopics(Collections.singleton(newTopic)).all().get();
                    System.out.println("Kafka topic created: " + topicName);
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error creating Kafka topic: " + e.getMessage());
                // Можно пробросить исключение, если топик обязателен
                // throw new RuntimeException("Failed to initialize Kafka topic", e);
            }
        };
    }
}