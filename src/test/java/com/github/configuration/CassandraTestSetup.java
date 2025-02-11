package com.github.configuration;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.cassandra.core.ReactiveCassandraOperations;
import reactor.util.retry.Retry;

import java.time.Duration;

public class CassandraTestSetup {
    private static final Logger logger = LoggerFactory.getLogger(CassandraTestSetup.class);

    private final ReactiveCassandraOperations reactiveCassandraOperations;

    public CassandraTestSetup(ReactiveCassandraOperations reactiveCassandraOperations) {
        if (reactiveCassandraOperations == null) {
            throw new IllegalArgumentException("ReactiveCassandraOperations must not be null");
        }
        this.reactiveCassandraOperations = reactiveCassandraOperations;
    }

    // Executes a CQL command with retry logic in case of failure.
    private void executeAndAwait(final String cql) {
        reactiveCassandraOperations.getReactiveCqlOperations()
                .execute(SimpleStatement.newInstance(cql))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(500))) // Retry up to 3 times with 500 ms delay
                .doOnError(e -> logger.error("CQL execution failed: {}", e.getMessage())) // Log error on failure
                .block(); // Wait for completion
    }

    // Initializes the schema by executing the necessary CQL statements.
    public void setup() {
        executeAndAwait("DROP TABLE IF EXISTS projects;");
        executeAndAwait("CREATE TYPE IF NOT EXISTS roi_project_planner.audit_metadata (created_at timestamp, updated_at timestamp);");
        executeAndAwait("CREATE TABLE IF NOT EXISTS projects (id UUID PRIMARY KEY, name text, required_capital decimal, profit decimal, auditMetadata frozen<audit_metadata>, version bigint);");
    }
}