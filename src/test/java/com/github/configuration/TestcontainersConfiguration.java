package com.github.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@Import(CassandraConfiguration.class)
@EmbeddedKafka(
        ports = 9092,
        kraft = true
)
public abstract class TestcontainersConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TestcontainersConfiguration.class.getName());
    private static final int CASSANDRA_PORT = 9042;

    @Container
    @ServiceConnection
    private static final CassandraContainer CASSANDRA_CONTAINER =
            new CassandraContainer(DockerImageName.parse("cassandra:5.0.3"))
                    .waitingFor(Wait.forListeningPort())
                    .withExposedPorts(CASSANDRA_PORT);

    @BeforeAll
    static void beforeAll() {
        try {
            CASSANDRA_CONTAINER.execInContainer(
                    "cqlsh", "-e",
                    "CREATE KEYSPACE IF NOT EXISTS roi_project_planner WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};"
            );
        } catch (Exception e) {
            logger.error("Encountered an error while creating keyspace in cassandra", e);
            Assertions.fail(String.format("Keyspace creation failed: %s", e.getMessage()));
        }
    }
}