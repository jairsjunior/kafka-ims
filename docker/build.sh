#!/bin/sh

mkdir -p cp-kafka-oauth/libs
mkdir -p rest-proxy/libs

mvn -f ../pom.xml clean package

rm rest-proxy/libs/kafka*
rm cp-kafka-oauth/libs/kafka*

cp ../kafka-ims-rest/target/*-jar-with-dependencies.jar ./rest-proxy/libs/
cp ../kafka-ims-java/target/*-jar-with-dependencies.jar ./cp-kafka-oauth/libs/

docker-compose build --no-cache
