#!/bin/sh

cmd="./gradlew clean coverage coveralls"
if [ -z ${@+x} ]
then
  echo "No command line params - starting default Gradle build."
else
  echo "Using $@ as the entrypoint."
  cmd=$@
fi

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
  -p 15672:15672 \
  -P \
  gonkulatorlabs/rabbitmq

# fire up our test container
docker run \
  -p 7077:7077 \
  -p 8080:8080 \
  -p 9999:9999 \
  -v $(pwd):/opt/sparkplug \
  --link sparkplug-rabbitmq:sparkplug-rabbitmq \
  -it \
  uncharted/sparkplug-test \
  ${cmd}
