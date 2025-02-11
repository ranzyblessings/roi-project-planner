from cassandra.cluster import Cluster

def execute_cql_script(host, script_path):
    try:
        cluster = Cluster([host])
        session = cluster.connect()

        with open(script_path, 'r') as f:
            cql_statements = f.read().split(';')
            for statement in cql_statements:
                if statement.strip():  # Ignore empty statements
                    session.execute(statement.strip())

        cluster.shutdown()
        print("Cassandra schema initialized successfully.")

    except Exception as e:
        print(f"Error initializing Cassandra schema: {e}")

if __name__ == "__main__":
    host = "cassandra"  # The Cassandra container name
    script_path = "/scripts/init.cql"
    execute_cql_script(host, script_path)