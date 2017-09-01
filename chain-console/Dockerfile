FROM openjdk:8-jdk-alpine
ADD target/chain-console-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT [ "sh", "-c", "java -jar /app.jar" ]
