package com.github.projects.model;

import com.github.configuration.CassandraTestSetup;
import com.github.configuration.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.cassandra.core.ReactiveCassandraOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataCassandraTest
class ProjectRepositoryTest extends TestcontainersConfiguration {
    private final ProjectRepository underTest;
    private final CassandraTestSetup cassandraTestSetup;

    @Autowired
    ProjectRepositoryTest(ProjectRepository underTest, ReactiveCassandraOperations reactiveCassandraOperations) {
        this.underTest = underTest;
        this.cassandraTestSetup = new CassandraTestSetup(reactiveCassandraOperations);
    }

    @BeforeEach
    void setUp() {
        cassandraTestSetup.setup();
    }

    @Test
    void shouldThrowOptimisticLockingFailure_whenConcurrentUpdatesAttemptToModifyTheSameEntity() {

        // Given: A project entity is persisted to establish the initial state
        Mono<ProjectEntity> projectToPersist = underTest.save(
                ProjectEntity.createNewProject("Project A", BigDecimal.ONE, BigDecimal.TWO)
        );

        // When: A stale version of the project is attempted to be saved (simulating concurrency conflict)
        projectToPersist
                .flatMap(persistedProject -> {

                    // Create a stale project entity with a version conflict
                    ProjectEntity staleProjectEntity = createStaleProjectEntity(persistedProject);

                    // Attempt to save the stale project entity
                    return underTest.save(staleProjectEntity);
                })
                .as(StepVerifier::create)

                // Then: Expect an OptimisticLockingFailureException due to the version conflict
                .expectError(OptimisticLockingFailureException.class)
                .verify();
    }

    /**
     * Creates a project entity with an outdated version to trigger concurrency conflict.
     *
     * @param persistedProject The current project entity.
     * @return A new project entity with the version decremented by 1.
     */
    private ProjectEntity createStaleProjectEntity(ProjectEntity persistedProject) {
        return new ProjectEntity(
                persistedProject.id(),
                persistedProject.name(),
                persistedProject.requiredCapital(),
                persistedProject.profit(),
                persistedProject.auditMetadata(),
                persistedProject.version() - 1 // Set to an outdated version
        );
    }

    @Test
    void shouldPersistProject_whenValidDetailsProvided() {

        // Given: A new project entity with valid details
        var project = ProjectEntity.createNewProject("Project B", new BigDecimal("100"), new BigDecimal("300"));

        // When: The project is saved to the repository
        Mono<ProjectEntity> persistedProjectMono = underTest.save(project);

        // Then: The persisted project should have populated fields (ID, audit metadata, version)
        StepVerifier.create(persistedProjectMono)
                .expectNextMatches(persistedProject -> {
                    assertThat(persistedProject.id()).isNotNull();
                    assertThat(persistedProject.name()).isEqualTo("Project B");
                    assertThat(persistedProject.requiredCapital()).isEqualTo(new BigDecimal("100"));
                    assertThat(persistedProject.profit()).isEqualTo(new BigDecimal("300"));
                    assertThat(persistedProject.auditMetadata().createdAt()).isNotNull();
                    assertThat(persistedProject.auditMetadata().updatedAt()).isNotNull();
                    assertThat(persistedProject.version()).isNotNull();
                    return true;
                })
                .verifyComplete();
    }
}