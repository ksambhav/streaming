#!/bin/bash

JVM_OPTS="
-server
-XX:+UseZGC
-XX:MaxRAMPercentage=65
-XX:InitialRAMPercentage=50
-XX:+HeapDumpOnOutOfMemoryError
-Xss256k
"

echo "$JVM_OPTS"

# shellcheck disable=SC2086
java ${JVM_OPTS} org.springframework.boot.loader.JarLauncher