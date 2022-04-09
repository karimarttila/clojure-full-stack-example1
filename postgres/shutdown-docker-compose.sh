#!/usr/bin/env bash

echo "Shutting down postgres..."
docker-compose -p ss2_postgres -f docker-compose-setup-local-postgres.yml down
