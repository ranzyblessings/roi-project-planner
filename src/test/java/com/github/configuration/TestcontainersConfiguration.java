package com.github.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@Import(CassandraConfiguration.class)
@EnableAutoConfiguration(exclude = {
        KafkaAutoConfiguration.class
})
public abstract class TestcontainersConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TestcontainersConfiguration.class.getName());
    private static final int REDIS_PORT = 6379;

    @Container
    @ServiceConnection
    private static final CassandraContainer<?> CASSANDRA_CONTAINER =
            new CassandraContainer<>(DockerImageName.parse("cassandra:5.0.3"))
                    .waitingFor(Wait.forListeningPort())
                    .withExposedPorts(9042);

    @Container
    private static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:7.4.2"))
                    .waitingFor(Wait.forListeningPort())
                    .withExposedPorts(REDIS_PORT);

    @DynamicPropertySource
    static void dynamicPropertySource(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(REDIS_PORT));
    }

    @BeforeAll
    static void beforeAll() {
        try {
            CASSANDRA_CONTAINER.execInContainer(
                    "cqlsh", "-e",
                    "CREATE KEYSPACE IF NOT EXISTS roi_project_planner WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};"
            );
        } catch (Exception e) {
            logger.error("{}", e.getMessage());
            Assertions.fail(String.format("Keyspace creation failed: %s", e.getMessage()));
        }
    }
}