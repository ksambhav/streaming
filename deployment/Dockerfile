FROM eclipse-temurin:21-jdk-jammy
#USER drunken
#WORKDIR /home/drunken/app
COPY start.sh .
COPY  dependencies/ ./
COPY  snapshot-dependencies/ ./
COPY  spring-boot-loader/ ./
COPY  application/ ./
ENTRYPOINT ["./start.sh"]