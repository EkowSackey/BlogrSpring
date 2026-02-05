# BlogrSpring

A simple blog platform built with Spring (Spring Boot) and Java.

## Features

- Create, read, update, delete (CRUD) operations for blog posts
- Commenting system
- User registration and authentication (JWT based)
- RESTful API endpoints for client apps
- Pagination and basic filtering for lists
- Input validation and basic error handling
- API documentation via Swagger/OpenAPI

## Tech stack

- Java (100%)
- Spring Boot (Web, Data JPA, Security)
- JPA / Hibernate
- Mongodb database
- Build: Maven 

## Prerequisites

- JDK 17+ (or as required by your project) installed
- Maven 3.6+ or Gradle 6+ if building locally
- A running DB instance

## Getting started

1. Clone the repository:
   ```
   git clone https://github.com/EkowSackey/BlogrSpring.git
   cd BlogrSpring
   ```

2. Check whether the project uses Maven or Gradle:
    - Maven: `pom.xml` present
    - Gradle: `build.gradle` or `build.gradle.kts` present

### Build tools

If using Maven:

```
mvn -v
mvn clean package
```

If using Gradle:

```
./gradlew -v
./gradlew build
```

### Run (Maven)

```
mvn spring-boot:run
# or
java -jar target/<artifact>-<version>.jar
```

### Run (Gradle)

```
./gradlew bootRun
# or
java -jar build/libs/<artifact>-<version>.jar
```

The app will typically start on port 8080. Visit http://localhost:8080/ (or the configured port).



Adjust environment variables to match your configuration.

## Configuration

Application settings live in `src/main/resources/application.yml` Common properties to configure:

- Server port: `server.port`
- Datasource: `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`
- JPA / Hibernate: `spring.jpa.*`
- JWT / security secrets: `app.security.jwtSecret` (or similar)
- Logging level: `logging.level.*`

Use environment variables or Spring profiles for sensitive values (secrets, DB credentials). Example environment variables:

- SPRING_DATASOURCE_URL
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- JWT_SECRET
- SPRING_PROFILES_ACTIVE


## Testing

Run unit and integration tests:

Maven:

```
mvn test
```

Gradle:

```
./gradlew test
```


## Contributing

Contributions are welcome! Suggested workflow:

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/my-feature`
3. Implement changes, add tests
4. Run tests locally
5. Create a pull request describing the change

Please follow the existing code style and add tests for new behavior.

