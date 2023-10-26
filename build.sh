#!/bin/bash
rm -rf application
rm -rf dependencies
rm -rf snapshot-dependencies
rm -rf spring-boot-loader
./mvnw package -DskipTests
java -Djarmode=layertools -jar target/my-application.jar extract
docker build -t samsoft .