FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY backend/.mvn/ .mvn/
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:resolve -B -q

COPY backend/src/ src/
RUN ./mvnw -DskipTests clean package -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
