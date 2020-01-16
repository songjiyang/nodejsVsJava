# our base build image
FROM maven:3-jdk-8 as maven

# copy the project files
COPY ./pom.xml ./pom.xml

# build all dependencies
RUN mvn dependency:go-offline -B

# copy your other files
COPY ./src ./src

# build for release
RUN ["mvn", "package", "-Dmaven.test.skip=true"]

# our final base image
FROM openjdk:8u171-jre-alpine

# set deployment directory
WORKDIR /stress

# copy over the built artifact from the maven image
COPY --from=maven target/stress-*.jar ./

# set the startup command to run your binary
ENTRYPOINT ["sh", "-c", "java -jar -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap stress-0.0.1-SNAPSHOT.jar"]
