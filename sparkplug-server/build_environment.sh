#!/bin/sh

docker build -t uncharted/sparkplug-test .

docker run \
-p 8080:8080 \
-p 9999:9999 \
-v $(pwd)/src/test/resources/log4j.properties:/opt/spark-1.6.0-bin-hadoop2.6/conf/log4j.properties \
-v $(pwd):/opt/sparkplug \
-it \
--workdir="//opt/sparkplug" \
uncharted/sparkplug-test \
/bin/bash
