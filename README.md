# URL Shortener API

Take-home assignment: URL shortener service เขียนด้วย Java (Spring Boot) + PostgreSQL รองรับ shorten URL, redirect, สมัคร/ล็อกอินด้วย JWT, และจัดการลิงก์ของตัวเอง

## Tech stack

- Java 21 (LTS)
- Spring Boot 4.1.1 (Web, Data JPA, Security, Validation)
- PostgreSQL
- JWT (`io.jsonwebtoken:jjwt`)
- Maven (ใช้ผ่าน wrapper `./mvnw`, ไม่ต้องติดตั้ง Maven เอง)
- JUnit 5 + Mockito

## Prerequisites

- JDK 21+
- Docker (สำหรับรัน PostgreSQL ใน local)

## Build & Run

```bash
./mvnw clean package
./mvnw spring-boot:run
```

Windows (PowerShell):

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

## Run tests

```bash
./mvnw test
```

## Project structure

```
com.example.urlshortener
├── controller/       # REST endpoints
├── service/          # business logic
│   └── shortcode/    # short-code generation strategy
├── repository/        # Spring Data JPA repositories
├── entity/             # JPA entities
├── dto/                # request/response DTOs
├── security/           # JWT filter, JwtService, SecurityConfig
├── exception/          # global exception handling
└── config/             # CORS, OpenAPI, bean config
```
