#!/bin/sh

docker rm --force rest-proxy
docker-compose up -d

docker logs -f rest-proxy