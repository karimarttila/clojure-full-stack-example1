#!/usr/bin/env bash

FLYWAY=/mnt/ssd2/local/flyway-8.5.7/flyway
$FLYWAY -configFiles=flyway_docker_compose.conf migrate
