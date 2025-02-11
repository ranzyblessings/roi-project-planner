CREATE KEYSPACE IF NOT EXISTS roi_project_planner WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE roi_project_planner;

CREATE TYPE IF NOT EXISTS audit_metadata (created_at timestamp, updated_at timestamp);

CREATE TABLE IF NOT EXISTS projects (
  id UUID PRIMARY KEY,
  name text,
  required_capital decimal,
  profit decimal,
  auditMetadata frozen<audit_metadata>,
  version bigint
);
