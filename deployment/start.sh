#!/bin/bash

JVM_OPTS="
-server
-XX:+UseZGC
-Xms128m
-Xmx128m
-XX:+HeapDumpOnOutOfMemoryError
-Xss256k
"

echo "$JVM_OPTS"

# shellcheck disable=SC2086
java ${JVM_OPTS} org.springframework.boot.loader.JarLauncher