# Use a minimal Python image
FROM python:3.9-slim-buster

# Install required packages
RUN apt-get update && apt-get install -y netcat-openbsd && \
    pip install cassandra-driver

# Set working directory
WORKDIR /scripts

# Copy init scripts into container
COPY init.cql /scripts/init.cql
COPY init_cassandra.py /scripts/init_cassandra.py

# Entrypoint command: wait for Cassandra to be ready, then execute the script
CMD sh -c "while ! nc -zv cassandra 9042; do echo 'Waiting for Cassandra...'; sleep 1; done; python3 /scripts/init_cassandra.py"