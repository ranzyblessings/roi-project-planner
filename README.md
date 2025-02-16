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
  are collected by Alloy, sent to Loki, and visualized in Grafana for comprehensive observability.

---

## Installation

Follow these steps to set up the **roi-project-planner** project locally.

### Prerequisites

Ensure the following tools are installed:

- [Docker v27.5.1](https://www.docker.com/get-started) (or latest)
- [Docker Compose v2.32.4](https://www.docker.com/get-started) (or latest)
- [Java 23](https://docs.aws.amazon.com/corretto/latest/corretto-23-ug/downloads-list.html) (or latest)

### Local Setup with Docker

To run the project locally using Docker, follow these steps:

1. **Clone the Repository:**
    ```bash
    git clone https://github.com/ranzyblessings/roi-project-planner.git
    cd roi-project-planner
    ```
2. **Start Dependencies with Docker Compose:**
   ```bash
      docker compose up -d
   ```

   **Note:** This command will start the following services:

    - **Kafka:** Handles event streaming for distributed communication, enabling real-time analytics on Capital
      Maximization Query events with low-latency, high-throughput processing.
    - **Cassandra:** A highly available, distributed NoSQL database that stores project data, ensuring fault tolerance,
      horizontal scalability, and low-latency access.
    - **Redis:** A high-performance, in-memory data store that functions as a caching layer, speeding up project
      lookups and optimizing overall system performance.

3. **Start the Backend Core API:**
   ```bash
    ./gradlew clean bootRun
    ```

---

## API Usage

_**Note**: For proper API usage, we are still considering whether to use OpenAPI or Spring REST Docs. Your
contribution is welcome._

### Projects

1. To **create one or more projects**, send a **POST** request:

    ```bash
    curl -X POST http://localhost:8080/api/v1/projects \
         -H "Content-Type: application/json" \
         -d '[
               {
                 "name": "Project 1",
                 "requiredCapital": 0.00,
                 "profit": 100.00
               },
               {
                 "name": "Project 2",
                 "requiredCapital": 100.00,
                 "profit": 200.00
               },
               {
                 "name": "Project 3",
                 "requiredCapital": 100.00,
                 "profit": 300.00
               }
             ]'
    ```

### Project Capital Maximization

1. To **maximize capital** by selecting up to _k_ distinct projects from a pool of available projects, send a **POST**
   request:

    ```bash
    curl -X POST http://localhost:8080/api/v1/capital/maximization/query \
         -H "Content-Type: application/json" \
         -d '{
              "maxProjects":2,
              "initialCapital":"100.00" 
            }'
    ```

For now, to **view selected projects and capital maximization**, use Grafana as outlined
in [Observability Setup for Local Development](#observability-setup-for-local-development) under the
**Log Monitoring** section, or check the console logs.

_In the future, advanced analytics and graphical representations will be added, with support for custom views that
consumers can subscribe to for tailored visualizations._

---

## Observability Setup for Local Development

_**Note:** On Mac or Windows, set the `targets` in **observability/prom-config.yaml**
to: `- targets: ['host.docker.internal:8080']`. On Linux, use the host's IP address._

1. **Start Observability Services**
    ```bash
    docker compose -f observability/compose.yaml up -d
    ```

### Distributed Tracing

To **monitor requests across services**, we use **distributed tracing** for improved observability and debugging. View
traces in **Jaeger** to analyze request flows, latency, and dependencies.

1. **Access Jaeger**
    - Open Jaeger at `http://localhost:16686`.
    - In the left panel, under **Service**, select `roi-project-planner` and click **Find Traces**.
    - Send **API requests** using the [API Usage](#api-usage) guide to visualize request flows.

### Metrics Monitoring

We use **Prometheus** to collect and monitor key application metrics, enabling performance analysis and proactive issue
detection. Metrics include **request rates**, **response times**, **error rates**, **JVM performance (memory, GC,
threads)**, and **database latency**.

1. **Access Grafana and configure Prometheus**
    - Open Grafana at `http://localhost:3000` (default login: `admin` / `admin`).
    - Navigate to **Data Sources**, click **"Add data source"** then Select **Prometheus**.
    - Set the **URL** to `http://prometheus:9090` (thanks to Docker DNS), then click **"Save & Test"** to verify
      connectivity.

2. **Create a Log Dashboard**
    - Click the **"+"** in the top right, select **"New Dashboard"**, then click **"Add Visualization"**.
    - Choose **Prometheus** as the data source.
    - Use **Label Filters** to refine logs (e.g., job:roi-project-planner-metrics).

3. **Monitor Key Metrics**
    - `http_server_requests_seconds_count` - Total HTTP requests per endpoint.
    - `http_server_requests_seconds_sum` - Request duration per endpoint.
    - `jvm_memory_used_bytes` - JVM memory usage.

- Refer to the [PromQL documentation](https://prometheus.io/docs/prometheus/latest/querying/basics) for advanced
  queries.

### Log Monitoring

We use **Loki** and **Alloy** to aggregate and analyze application logs, enabling real-time debugging and operational
insights. Logs capture **request processing**, **application events**, **errors**, and **performance metrics** for
efficient troubleshooting.

1. **Access Grafana and configure Loki**
    - Open Grafana at `http://localhost:3000` (default login: `admin` / `admin`).
    - Navigate to **Data Sources**, click **"Add data source"** then Select **Loki**.
    - Set the **URL** to `http://loki:3100` (thanks to Docker DNS), then click **"Save & Test"** to verify connectivity.

2. **Create a Log Dashboard**
    - Click the **"+"** in the top right, select **"New Dashboard"**, then click **"Add Visualization"**.
    - Choose **Loki** as the data source.
    - Use **Label Filters** to refine logs (e.g., service:roi-project-planner).
    - Enable **Table View** to see structured log entries.

3. **LogQL Queries for Analysis**
    - `rate({job="roi-project-planner-logs"} |~ "statusCode=201" | json [30m])` - rate of successful requests (status
      code 201) over the last 30 minutes.
    - `rate({job="roi-project-planner-logs"} [1m])` - Track High Log Volume (Spike Detection).
    - `rate({job="roi-project-planner-logs"} | json | level="ERROR" [5m])` - Measure Log Rate per Log Level (eg,
      `ERROR`, `INFO`, `WARN`).

- Refer to the [LogQL documentation](https://grafana.com/docs/loki/latest/query) for advanced queries.

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