package com.github.projects.model;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

public interface ProjectRepository extends ReactiveCassandraRepository<ProjectEntity, String> {

}