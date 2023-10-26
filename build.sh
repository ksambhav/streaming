#!/bin/bash
rm -rf application
rm -rf dependencies
rm -rf snapshot-dependencies
rm -rf spring-boot-loader
./mvnw package -DskipTests
java -Djarmode=layertools -jar target/bootiful-0.0.1-SNAPSHOT.jar extract
docker build -t samsoft:1.0.10 .