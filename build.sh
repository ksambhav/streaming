#!/bin/bash
./mvnw package -DskipTests
cp start.sh target/extracted/start.sh
java -Djarmode=layertools -jar target/my-application.jar extract --destination target/extracted
docker build -f Dockerfile -t samsoft target/extracted