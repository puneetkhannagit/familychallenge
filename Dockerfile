# ---------- UI BUILD ----------
FROM node:20 AS ui-build
WORKDIR /app
COPY ui/package*.json ./
RUN npm install
COPY ui/ ./
RUN npm run build

# ---------- BACKEND BUILD ----------
FROM maven:3.9.6-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY --from=ui-build /app/dist ./src/main/resources/static
RUN mvn clean package -DskipTests

# ---------- RUNTIME ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]