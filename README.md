# Blogr

A modern, high-performance blog platform backend built with **Spring Boot 3**, **Java 17**, and **MongoDB**.

## 🚀 Features

- **Dual API Support**: Fully functional **RESTful API** and **GraphQL API**.
- **Content Management**: Complete CRUD operations for Posts, Comments, and User profiles.
- **Advanced Analytics**: Real-time statistics including top authors, trending tags, and engagement metrics (average reviews).
- **Security**: Robust authentication and authorization using **JWT (JSON Web Tokens)** with Role-Based Access Control (RBAC).
- **Performance**: 
  - **Caffeine Caching**: Optimized data retrieval for frequently accessed posts and pages.
  - **AOP Monitoring**: Aspect-Oriented Programming used for performance tracking and logging.
- **Documentation**: 
  - Interactive **Swagger UI** for REST endpoints.
  - **GraphiQL** interface for exploring the GraphQL schema.
- **Data Integrity**: Atomic operations ensured via Spring's `@Transactional` support for MongoDB.

## 🛠 Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.5.9
- **Database**: MongoDB (Spring Data MongoDB)
- **Security**: Spring Security + JJWT
- **API**: Spring Web (REST), Spring for GraphQL
- **Caching**: Caffeine Cache
- **Documentation**: Springdoc OpenAPI (Swagger)
- **Utilities**: Lombok, MapStruct (Mappers)
- **Build Tool**: Maven

## 📋 Prerequisites

- **JDK 17** or higher
- **Maven 3.8+**
- **MongoDB** (Local instance or Atlas URI)

## ⚙️ Configuration

Settings are located in `src/main/resources/application.yml`. Key properties:

- **MongoDB**: `spring.data.mongodb.uri`
- **JWT Secret**: `jwt.secret`
- **Server Port**: `server.port` (Default: 8080)

### Environment Variables
For production, it is recommended to use environment variables:
- `SPRING_DATA_MONGODB_URI`
- `JWT_SECRET`

## 🚀 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/EkowSackey/BlogrSpring.git
   cd BlogrSpring
   ```

2. **Build the project**:
   ```bash
   mvn clean package
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

## 📖 API Documentation

Once the app is running, you can access the documentation at:

- **Swagger UI (REST)**: `http://localhost:8080/swagger-ui.html`
- **GraphiQL (GraphQL)**: `http://localhost:8080/graphiql`

## 🧪 Testing

The project includes comprehensive unit and integration tests for Services and Controllers.

```bash
mvn test
```

## 🤝 Contributing

1. Fork the repository.
2. Create a feature branch: `git checkout -b feat/new-feature`.
3. Commit your changes: `git commit -m 'Add some feature'`.
4. Push to the branch: `git push origin feat/new-feature`.
5. Open a Pull Request.
