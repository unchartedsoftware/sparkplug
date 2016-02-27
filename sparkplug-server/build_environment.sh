#!/bin/sh

# build our test container first; don't waste time with other stuff if this fails
docker build -t uncharted/sparkplug-test .

# create a docker network for sharing connections across containers
docker network create sparkplug-test

# nuke any running rabbitmq containers, just in case
docker kill sparkplug-rabbitmq
docker rm -f sparkplug-rabbitmq

# create the rabbitmq container
docker run -d \
  --name sparkplug-rabbitmq \
  --net sparkplug-test \
  -P \
  gonkulatorlabs/rabbitmq

# fire up our test container
docker run \
  --name sparkplug-test \
  --net sparkplug-test \
  -p 8080:8080 \
  -p 9999:9999 \
  -v $(pwd):/opt/sparkplug \
  -it \
  --workdir="//opt/sparkplug" \
  uncharted/sparkplug-test \
  /bin/bash
