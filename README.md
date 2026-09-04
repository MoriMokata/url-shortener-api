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

## Database (local dev)

```bash
cp .env.example .env
docker compose up -d db
```

ค่า default ใน `.env.example` ตั้ง PostgreSQL ไว้ที่ host port `5433` (ไม่ใช่ `5432`) เผื่อเครื่อง dev มี Postgres/ container อื่นครอง port 5432 อยู่แล้ว แก้ `POSTGRES_PORT` ใน `.env` ได้ตามต้องการ

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

แอปรันที่ `http://localhost:8080` โดย default — ถ้า port 8080 ถูกใช้งานอยู่แล้ว override ได้ด้วย `SERVER_PORT` env var เช่น `SERVER_PORT=8081 ./mvnw spring-boot:run`

## Run full stack ด้วย Docker (app + db)

```bash
cp .env.example .env
docker compose up --build
```

รัน backend + PostgreSQL ครบทั้งคู่จาก clean checkout โดยไม่ต้องติดตั้ง JDK/Maven บนเครื่อง แอปจะขึ้นที่ `http://localhost:8080` (override ด้วย `APP_PORT` ใน `.env` ได้ถ้าชนกับ service อื่น)

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
