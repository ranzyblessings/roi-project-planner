# Return On Investment Project Planner

## Overview

**ROI Project Planner** is a tool designed to optimize capital allocation by selecting the most impactful projects from
a pool of options. The project highlights expertise in advanced data structures, algorithms, SOLID principles, and
software engineering best practices. It also integrates modern cloud-native patterns, including reactive programming,
fault tolerance, and event-driven architectures, showcasing a holistic approach to scalable and resilient system design.

[![Build and Test Application](https://github.com/ranzyblessings/roi-project-planner/actions/workflows/build-and-test.yaml/badge.svg)](https://github.com/ranzyblessings/roi-project-planner/actions/workflows/build-and-test.yaml)
[![Deploy Docker Image](https://github.com/ranzyblessings/roi-project-planner/actions/workflows/deploy-docker-image.yaml/badge.svg)](https://github.com/ranzyblessings/roi-project-planner/actions/workflows/deploy-docker-image.yaml)

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Deployment](#deployment)
- [API Usage](#api-usage)
- [Observability](#observability-setup-for-local-development)
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
  are collected by Alloy, sent to Loki for indexing, and visualized in Grafana for comprehensive observability.

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
      docker compose up --build -d
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

## Deployment

To deploy the **ROI Project Planner** in production, we use **Terraform** to provision a secure **EKS cluster** with
managed dependencies, including Kafka, Cassandra, and Redis. The setup includes a dedicated VPC, high-availability
subnets, security groups, and persistent storage with Amazon EBS volumes. Argo CD enables GitOps for CI/CD, while
Prometheus, Grafana, and Jaeger handle metrics, monitoring, and distributed tracing. We enforce IAM roles for access
control, implement SSL/TLS encryption, and configure auto-scaling for resilience. Additionally, log files are stored in
Amazon S3 for long-term retention and easy access.

### Local Deployment

For local development and testing, you can set up a **multi-node Kubernetes cluster** using **Multipass** and
**MicroK8s**. Follow the detailed instructions in [K8s-MultiNode-Dev-Setup.md](./K8s-MultiNode-Dev-Setup.md) to
configure your environment.

```bash
# Deploy Kubernetes Resources for Cassandra, Redis, and Kafka Clusters

# 1. Create the namespace
kubectl apply -f kubernetes/namespace.yaml

# 2. Configure MicroK8s storage (replace with preferred storage class for non-MicroK8s environments)
kubectl apply -f kubernetes/microk8s-storage.yaml

# Deploy Cassandra Cluster and Its Secret

# 1. Generate Cassandra credentials (example):
echo -n "admin" | base64              # Outputs: YWRtaW4=
echo -n "adminSecureSecret" | base64  # Outputs: YWRtaW5TZWN1cmVTZWNyZXQ=

# 2. Create and apply cassandra-secrets.yaml:
echo "apiVersion: v1
kind: Secret
metadata:
  name: cassandra-secrets
  namespace: roi-project-planner-dev
type: Opaque
data:
  username: YWRtaW4=
  password: YWRtaW5TZWN1cmVTZWNyZXQ=" > kubernetes/cassandra-secrets.yaml
kubectl apply -f kubernetes/cassandra-secrets.yaml

# 3. Deploy the Cassandra cluster:
kubectl apply -f kubernetes/cassandra-statefulset.yaml

# Deploy Redis Cluster and Its Secret
# 1. Generate the Redis password (example):
echo -n "adminSecureSecret" | base64  # Outputs: YWRtaW5TZWN1cmVTZWNyZXQ=

# 2. Create and apply redis-secrets.yaml:
echo "apiVersion: v1
kind: Secret
metadata:
  name: redis-secrets
  namespace: roi-project-planner-dev
type: Opaque
data:
  password: c2VjcmV0" > kubernetes/redis-secrets.yaml
kubectl apply -f kubernetes/redis-secrets.yaml

# 3. Deploy the Redis cluster:
kubectl apply -f kubernetes/redis-deployment.yaml

# Deploy Kafka Cluster and Its Secret
kubectl apply -f kubernetes/kafka-statefulset.yaml

# Deploy the roi-project-planner and its secrets

# 1. Generate roi-project-planner database connection details:

echo "apiVersion: v1
kind: Secret
metadata:
  name: roi-project-planner-secrets
  namespace: roi-project-planner-dev
type: Opaque
data:
  cassandra-username: YWRtaW4=
  cassandra-password: YWRtaW5TZWN1cmVTZWNyZXQ=
  redis-password: YWRtaW5TZWN1cmVTZWNyZXQ=
  kafka-username: YWRtaW4=
  kafka-password: YWRtaW5TZWN1cmVTZWNyZXQ=" > kubernetes/roi-project-planner-secrets.yaml
kubectl apply -f kubernetes/roi-project-planner-secrets.yaml 

# 2. # Deploy the roi-project-planner

kubectl apply -f kubernetes/roi-project-planner-deployment.yaml

```

### AWS EKS Deployment

_AWS EKS deployment via Terraform is coming soon. The current setup is optimized for Kubernetes deployments, ensuring
seamless scalability and production readiness._

---

## API Usage

_**Note**: For proper API usage, we are still considering whether to use OpenAPI or Spring REST Docs. Your
contribution is welcome._

### Projects

To **create one or more projects**, send a **POST** request:

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

To **maximize capital** by selecting up to _k_ distinct projects from a pool of available projects, send a **POST**
request:

 ```bash
 curl -X POST http://localhost:8080/api/v1/capital/maximization \
      -H "Content-Type: application/json" \
      -d '{
           "maxProjects":2,
           "initialCapital":"100.00" 
         }'
 ```

To **view the selected projects and capital maximization results** after receiving a Kafka event, refer to **Grafana**
as described in the [Setup for Local Development (Metrics Monitoring)](#observability-setup-for-local-development)
section. Select the **metric** `roi_final_capital_with_projects`, and for **label filters**, choose `selected_projects`.

### List all projects

To **retrieve all projects**, send a **GET** request:

```bash
curl http://localhost:8080/api/v1/projects
```

### List project by ID

To **retrieve a project** by its unique identifier, send a **GET** request with the project **ID**. Subsequent requests
are faster due to **caching**.

```bash
curl http://localhost:8080/api/v1/projects/{ID}
```

---

## Observability Setup for Local Development

**Note:** On Mac or Windows, set the `targets` in _observability/prom-config.yaml_ to
`- targets: ['host.docker.internal:8080']`. On Linux, use the host's IP address instead.

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

_**Note:** You can also visualize traces in Grafana by adding Jaeger as a data source._

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

Refer to the [PromQL documentation](https://prometheus.io/docs/prometheus/latest/querying/basics) for advanced
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
    - `rate({job="roi-project-planner-logs"} |~ "Final capital" | json [30m])` - rate of successful Capital maximization
      query events.
    - `rate({job="roi-project-planner-logs"} [1m])` - Track High Log Volume (Spike Detection).
    - `rate({job="roi-project-planner-logs"} | json | level="ERROR" [5m])` - Measure Log Rate per Log Level (eg,
      `ERROR`, `INFO`, `WARN`).

Refer to the [LogQL documentation](https://grafana.com/docs/loki/latest/query) for advanced queries.

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