# ---- build stage ----
# JDK17 khop voi <java.version>17</java.version> trong pom.xml - ban truoc
# dung image 21 gay lech, JDK21 van compile duoc code target 17 nhung
# runtime image nen khop dung version de tranh sai khac hanh vi tinh vi.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
# Nếu dùng Gradle, đổi dòng trên thành:
# RUN ./gradlew clean bootJar -x test

# ---- run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Gradle: COPY --from=build /app/build/libs/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70.0"
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]