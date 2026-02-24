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

## 🔒 Security Architecture

### Authentication & Authorization
The application uses **Stateless JWT Authentication**.
- **Login**: Users authenticate via `/api/v1/users/auth/login` and receive a signed JWT.
- **Access**: The JWT must be included in the `Authorization` header as `Bearer <token>` for all protected requests.
- **Roles**: Access control is enforced based on user roles (`ADMIN`, `AUTHOR`, `READER`) embedded in the JWT claims.

### CSRF (Cross-Site Request Forgery)
**Status: Disabled**
- **Reason**: The application is stateless and uses JWTs stored in client-side storage (e.g., localStorage) rather than cookies. CSRF attacks rely on the browser automatically sending session cookies with requests. Since we do not use session cookies, the API is immune to standard CSRF attacks.
- **Enabling for Stateful Sessions**: If the application were to switch to server-side sessions or cookie-based authentication, CSRF protection should be enabled in `SecurityConfig.java`:
  ```java
  http.csrf(csrf -> csrf
      .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
  );
  ```

### CORS (Cross-Origin Resource Sharing)
**Status: Enabled**
- **Configuration**: The API is configured to allow requests from trusted frontend origins (e.g., `http://localhost:3000`, `http://localhost:4200`).
- **Difference from CSRF**:
  - **CORS**: A browser security feature that prevents a malicious site from reading data from your API. It controls *access* to resources.
  - **CSRF**: An attack that tricks a user into performing an unwanted *action* on a trusted site where they are authenticated. It exploits the trust the site has in the user's browser.
- **Interaction**: Our strict CORS policy complements the stateless architecture by ensuring only authorized domains can interact with the API, while the lack of cookie-based auth negates the need for CSRF tokens.

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
