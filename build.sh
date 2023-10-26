#!/bin/bash
./mvnw package -DskipTests
java -Djarmode=layertools -jar target/my-application.jar extract
#docker build -t samsoft