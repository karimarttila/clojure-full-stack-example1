#!/usr/bin/env bash

echo "NOTE: Remember to destroy the container if running again!"
echo "Copy test data to docker volume..."
rm -rf docker-dev-data
mkdir docker-dev-data
cp dev-data/* docker-dev-data/.
echo "Starting docker compose for postgres..."
docker-compose -p ss2_postgres -f docker-compose-setup-local-postgres.yml up -d
sleep 2
echo "Creating Simple Server schemas..."
./run-flyway.sh
sleep 1
echo "Starting following logs..."
docker logs -f ss2_postgres_postgres_1
