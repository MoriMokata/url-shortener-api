# URL Shortener API

URL shortener service เขียนด้วย Java (Spring Boot) + PostgreSQL รองรับ shorten URL, redirect, และ register/login JWT โดยสามารถรันได้ด้วย Docker Compose และยังมี CI ของ Github workflow ให้ใช้งานด้วย

## 1. Setup and run instructions

### Tech stack

- Java 21 (LTS)
- Spring Boot 4.1.1 (Web, Data JPA, Security, Validation)
- PostgreSQL
- JWT (`io.jsonwebtoken:jjwt`)
- Maven (ใช้ผ่าน wrapper `./mvnw`, ไม่ต้องติดตั้ง Maven เอง)
- JUnit 5 + Mockito

### Prerequisites

- Install JDK 21+
- Install Docker (สำหรับรัน service ผ่าน Docker compose)

### Run full stack ด้วย Docker (app + db)

```bash
cp .env.example .env
docker compose up --build
```

แอปรันที่ `http://localhost:8080`

หยุด service (ลบ container + network, ใส่ `-v` ถ้าต้องการลบ volume ของ DB ด้วย):

```bash
docker compose down
```

### Build & Run with command Maven

```bash
./mvnw clean package
./mvnw spring-boot:run
```

Windows (PowerShell):

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

### Run tests

```bash
./mvnw test
```

### API documentation (Swagger UI)

เปิด `http://localhost:8080/swagger-ui/index.html` หลัง start application

## 2. Example API requests

Postman collection [`URL-Shortener-API.postman_collection.json`](URL-Shortener-API.postman_collection.json) สำหรับทดสอบ API 

### Register

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"P@ssw0rd"}'
```

Response `201`:

```json
{"id":1,"email":"user@example.com","createdAt":"2026-01-01T00:00:00Z"}
```

### Login

```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"P@ssw0rd"}'
```

Response `200`:

```json
{"token":"eyJhbGciOiJIUzUxMiJ9...","expiresIn":86400}
```

เก็บ `token` ไว้ใช้เป็น `Authorization: Bearer <token>` ใน request ถัดไป

### Shorten a URL (ต้อง login)

```bash
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"original_url":"https://example.com/some/very/long/link"}'
```

Response `201`:

```json
{"id":10,"shortCode":"abc123","shortUrl":"http://localhost:8080/abc123","originalUrl":"https://example.com/some/very/long/link","createdAt":"2026-01-01T00:00:00Z"}
```

### List my URLs (ต้อง login)

```bash
curl http://localhost:8080/api/urls \
  -H "Authorization: Bearer <token>"
```

Response `200`:

```json
[{"id":10,"shortCode":"abc123","shortUrl":"http://localhost:8080/abc123","originalUrl":"https://example.com/some/very/long/link","isActive":true,"createdAt":"2026-01-01T00:00:00Z"}]
```

### Delete / deactivate a URL (ต้อง login และเป็นเจ้าของ)

```bash
curl -X DELETE http://localhost:8080/api/urls/10 \
  -H "Authorization: Bearer <token>"
```

Response `204` (ไม่มี body)

### Redirect (public, ไม่ต้อง login)

```bash
curl -i http://localhost:8080/abc123
```

Response `302` พร้อม header `Location: https://example.com/some/very/long/link`
