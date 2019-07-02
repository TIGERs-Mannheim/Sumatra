#!/usr/bin/env bash

# Start containers, rebuilding if necessary and close all when simulation finished
docker-compose up --build --abort-on-container-exit