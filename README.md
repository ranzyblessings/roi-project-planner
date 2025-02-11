# Return On Investment (ROI) Project Planner

## Overview

**ROI Project Planner** is an open-source tool designed to maximize capital by selecting up to *k* distinct projects
from a pool of available projects. Leveraging greedy algorithms, advanced data structures, and best practices in
software engineering, this solution is inspired by real-world venture capital and investment strategies. It not only
demonstrates expertise in data structures, algorithms, design patterns, and SOLID principles but also integrates modern
cloud-native patterns including reactive programming, fault tolerance, and event-driven architectures.

[![Build, Test, Dockerize, and Deploy to Docker Hub](https://github.com/ranzyblessings/roi-project-planner/actions/workflows/build-test-dockerize-deploy.yml/badge.svg)](https://github.com/ranzyblessings/roi-project-planner/actions/workflows/build-test-dockerize-deploy.yml)

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [API Usage](#api-usage)
- [Observability](#observability-setup-for-local-development)
- [Deployment](#deployment)
- [How To Contribute](#how-to-contribute)
- [License](#license)

---

## Features

- **Optimized Capital Selection:** Uses greedy algorithms and max-heaps to select projects that maximize final capital.
- **Reactive & Asynchronous Processing:** Built on Java 23 and Spring WebFlux for non-blocking, asynchronous operations.
- **Cloud-Native Design:** Seamlessly deployable to Kubernetes with Docker, supporting horizontal scaling and
  resilience.
- **Fault Tolerance:** Integrated with Resilience4J to provide circuit breaker patterns and fallback mechanisms.
- **Event-Driven Architecture:** Utilizes Apache Kafka for robust, asynchronous event processing.
- **CI/CD Integration:** Automated builds, tests, and deployments via GitHub Actions.
- **Extensive Testing:** Comprehensive tests with JUnit 5, AssertJ, Mockito, and Testcontainers for realistic
  integration testing.
- **Cache Integration:** Leverages caching (via Redis) to optimize performance for frequently accessed data.
- **Observability:** Equipped with Prometheus, Grafana, Jaeger, and Argo CD for metrics, monitoring, distributed
  tracing, and GitOps.
- **Logging Strategy:** Employs SLF4J with Logback and logstash-logback-encoder to produce structured JSON logs. Logs
  are collected by Promtail, sent to Loki, and visualized in Grafana for comprehensive observability.

---

## Installation

Follow these steps to set up the **roi-project-planner** project locally.

### Prerequisites

Ensure the following tools are installed:

- [Docker v27.5.1](https://www.docker.com/get-started) (or latest)
- [Docker Compose v2.32.4](https://www.docker.com/get-started) (or latest)
- [Java 23](https://docs.aws.amazon.com/corretto/latest/corretto-23-ug/downloads-list.html) (or latest)

### Local Setup with Docker

To run the project locally using Docker:

1. **Clone the Repository:**
    ```bash
    git clone https://github.com/ranzyblessings/roi-project-planner.git
    cd roi-project-planner
    ```

2. **Start the Backend (API) Core and Its Dependencies:**
    - ***Note:** Initial application startup may fail with "Invalid keyspace roi_project_planner". On the very first
      run (when Docker images are not cached), you might encounter this error due to a timing issue: the application
      connects before schema initialization completes. Subsequent runs will not have this problem. Simply re-run the
      application.*
   ```bash
    ./gradlew clean bootRun
    ```

   **Note:** This command automatically runs the `compose.yaml`, offering an alternative to
   `docker compose up --build -d`. It will start the following services:

    - **Kafka:** Handles event streaming for distributed communication, enabling real-time analytics on Capital
      Maximization Query events with low-latency, high-throughput processing.
    - **Cassandra:** A highly available, distributed NoSQL database that stores project data, ensuring fault tolerance,
      horizontal scalability, and low-latency access.
    - **Redis:** A high-performance, in-memory data store that functions as a caching layer, speeding up project
      lookups and optimizing overall system performance.

---

## API Usage

_The API usage commands will be available soon._

---

## Observability Setup for Local Development

_(Setup instructions will be available soon.)_

---

## Deployment

To deploy the ROI Project Planner, we use **Terraform** to provision the necessary infrastructure for a Kubernetes
cluster along with its dependencies, such as a Kafka cluster, Cassandra cluster, and Redis. Additionally, Argo CD is
utilized for GitOps, while Prometheus, Grafana, and Jaeger provide metrics, monitoring, and distributed tracing.

_(Terraform project link will be available soon.)_

---

## How To Contribute

We welcome contributions from developers of all skill levels! Here’s how you can get started:

1. **Fork the Repository:** Create a personal copy of the repo.
2. **Explore Issues:** Check the [issue tracker](https://github.com/ranzyblessings/roi-project-planner/issues) for open
   issues or feature requests.
3. **Create a Branch:** Work on your feature or bug fix in a separate branch.
4. **Submit a Pull Request:** Once **ready and tests are passing**, submit a PR for review.

### Areas to Contribute

- **Feature Development:** Implement new features such as advanced project querying, analytics, or enhanced reporting.
- **Bug Fixes:** Identify and resolve issues.
- **Documentation:** Improve or expand the existing documentation.
- **Testing:** Write unit and integration tests to ensure reliability.

---

## License

This project is open-source software released under the [MIT License](https://opensource.org/licenses/MIT).
