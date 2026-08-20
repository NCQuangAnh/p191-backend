# P-191 Telemetry Backend

Backend telemetry cho app trợ lý lái xe P-191. Một backend, hai chiều tách biệt:

- **GHI (app khách / Guest):** `POST /device/heartbeat`, `POST /event/logEvent` — xác thực bằng `X-Api-Key`, không cần JWT. Backend biết "máy nào" nhờ `deviceId` trong payload.
- **ĐỌC (admin):** `GET /device/list`, `GET /event/listEvents` — cần JWT có role `ADMIN`.

## Stack
Java 17 · Spring Boot 3.3 · Spring Web / Data JPA / Security / Validation · jjwt 0.12 · H2 (mặc định) / PostgreSQL (profile `postgres`).

## Chạy nhanh (H2, không cần cài DB)
```bash
mvn spring-boot:run
```
App chạy ở `http://localhost:8080`. H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:telemetry`, user `sa`, không mật khẩu). Admin mặc định `admin` / `admin123` được seed lúc khởi động.

## Swagger UI
Sau khi chạy, mở `http://localhost:8080/swagger-ui.html`. OpenAPI JSON ở `http://localhost:8080/v3/api-docs`.

Cách test trên Swagger:
1. Bấm **Authorize**. Có 2 ô: `deviceApiKey` (nhập giá trị `X-Api-Key`, mặc định `dev-device-key-doi-o-production`) và `bearerAuth` (nhập JWT).
2. Endpoint ghi (`/device/heartbeat`, `/event/logEvent`) tự dùng `X-Api-Key`; endpoint đọc (`/device/list`, `/event/listEvents`) dùng Bearer.
3. Lấy JWT: gọi `POST /auth/login` (`admin`/`admin123`), copy `token` vào ô `bearerAuth`.

## Chạy với PostgreSQL
```bash
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

## Thử API
Mở `api.http` (VS Code REST Client / IntelliJ) hoặc dùng curl:
```bash
# Ghi (app khách)
curl -X POST localhost:8080/device/heartbeat \
  -H "Content-Type: application/json" -H "X-Api-Key: dev-device-key-doi-o-production" \
  -d '{"deviceId":"pixel-8-abc123","model":"Pixel 8","appVersion":"1.0.0"}'

# Đọc (admin): login lấy token rồi gọi list
TOKEN=$(curl -s -X POST localhost:8080/auth/login \
  -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')
curl localhost:8080/device/list -H "Authorization: Bearer $TOKEN"
```

## Endpoints
| Method | Path | Auth | Ghi chú |
|---|---|---|---|
| POST | `/device/heartbeat` | `X-Api-Key` | Upsert device theo `deviceId`, cập nhật `lastSeen` |
| POST | `/event/logEvent` | `X-Api-Key` | Ghi 1 sự kiện: `category`, `latencyMs`, `hasError`, `errorMessage` |
| POST | `/auth/login` | — | Trả JWT (`Bearer`, TTL 1h mặc định) |
| GET | `/device/list` | JWT ADMIN | Kèm cờ `online` theo ngưỡng `online-threshold-seconds` |
| GET | `/event/listEvents` | JWT ADMIN | Lọc `deviceId`/`category`/`hasError` + phân trang `page`/`size` |

## Cấu hình (application.yml)
- `app.security.jwt.secret` — bí mật ký JWT (>= 32 byte). **Đổi ở production.**
- `app.security.device-api-key` — khóa app khách gửi kèm `X-Api-Key`. **Đổi ở production.**
- `app.admin.username` / `app.admin.password` — admin seed. **Đổi ở production.**
- `app.device.online-threshold-seconds` — ngưỡng coi device là online (mặc định 120s).

Override bằng biến môi trường, ví dụ `APP_SECURITY_JWT_SECRET`, `APP_SECURITY_DEVICE_API_KEY`, `APP_ADMIN_PASSWORD`.

## Từ phía Flutter
Trong các service hiện có (`message_classifier_service.dart`, `stt_pipeline_service.dart`, ...), sau khi xử lý xong 1 tin nhắn thì bắn `POST /event/logEvent` kèm header `X-Api-Key`; và có 1 timer định kỳ gọi `POST /device/heartbeat`.
