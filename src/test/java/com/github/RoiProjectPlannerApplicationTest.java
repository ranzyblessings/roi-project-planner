package com.github;

import com.github.configuration.CassandraTestSetup;
import com.github.configuration.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.ReactiveCassandraOperations;

@SpringBootTest
class RoiProjectPlannerApplicationTest extends TestcontainersConfiguration {
    private final CassandraTestSetup cassandraTestSetup;

    @Autowired
    RoiProjectPlannerApplicationTest(ReactiveCassandraOperations reactiveCassandraOperations) {
        this.cassandraTestSetup = new CassandraTestSetup(reactiveCassandraOperations);
    }

    @BeforeEach
    void setUp() {
        cassandraTestSetup.setup();
    }

    @Test
    void contextLoads() {
    }
}