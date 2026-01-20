# Flyway Migration Guide

## Giới thiệu
Dự án này đã được tích hợp Flyway để quản lý database migration một cách tự động và có tổ chức.

## Cấu trúc Migration Files

Migration files được đặt trong: `src/main/resources/db/migration/`

Quy tắc đặt tên: `V{version}__{description}.sql`
- **V1__create_schema.sql**: Tạo tất cả các bảng và indexes
- **V2__init_master_data.sql**: Thêm dữ liệu mẫu ban đầu

## Cấu hình Flyway

Trong `application.properties`:
```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.validate-on-migrate=true

# JPA Configuration  
spring.jpa.hibernate.ddl-auto=validate
```

**Lưu ý**: 
- `ddl-auto=validate` đảm bảo schema chỉ được quản lý bởi Flyway
- Flyway sẽ tự động chạy migrations khi khởi động ứng dụng

## Flyway Commands (Maven)

### 1. Chạy migrations
```bash
mvn flyway:migrate
```

### 2. Xem thông tin migrations
```bash
mvn flyway:info
```

### 3. Validate migrations
```bash
mvn flyway:validate
```

### 4. Clean database (CHỈ dùng trong development!)
```bash
mvn flyway:clean
```

### 5. Repair metadata table
```bash
mvn flyway:repair
```

## Quy trình Migration

### Tạo Migration mới

1. Tạo file mới trong `src/main/resources/db/migration/`
2. Đặt tên theo format: `V{next_version}__{description}.sql`
   - Ví dụ: `V3__add_user_preferences.sql`

3. Viết SQL migration:
```sql
-- V3__add_user_preferences.sql
ALTER TABLE users ADD COLUMN theme VARCHAR(50) DEFAULT 'light';
ALTER TABLE users ADD COLUMN language VARCHAR(10) DEFAULT 'vi';
```

4. Khởi động lại ứng dụng hoặc chạy: `mvn flyway:migrate`

### Rollback

Flyway không hỗ trợ rollback tự động. Để rollback:

1. Tạo migration mới để revert thay đổi:
```sql
-- V4__remove_user_preferences.sql
ALTER TABLE users DROP COLUMN theme;
ALTER TABLE users DROP COLUMN language;
```

## Database Setup từ đầu

### Bước 1: Drop database cũ (nếu có)
```sql
DROP DATABASE IF EXISTS eduTool;
```

### Bước 2: Tạo database mới
```sql
CREATE DATABASE eduTool;
```

### Bước 3: Khởi động ứng dụng
Flyway sẽ tự động:
1. Tạo bảng `flyway_schema_history` để track migrations
2. Chạy V1__create_schema.sql
3. Chạy V2__init_master_data.sql

## Kiểm tra Migration Status

Kết nối database và query:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

Output sẽ hiển thị:
- installed_rank: Thứ tự migration được chạy
- version: Version của migration
- description: Mô tả migration
- script: Tên file migration
- success: Trạng thái (true/false)
- installed_on: Thời gian chạy migration

## Best Practices

### ✅ NÊN:
- Luôn test migration trên database local trước
- Backup database trước khi chạy migration trên production
- Sử dụng transactions khi có thể
- Đặt tên migration rõ ràng, mô tả chính xác
- Version number phải tăng dần (V1, V2, V3...)
- Commit migration files cùng với code changes

### ❌ KHÔNG NÊN:
- Sửa đổi migration files đã chạy
- Xóa migration files đã chạy
- Sử dụng `flyway:clean` trên production
- Để `ddl-auto=create` hoặc `ddl-auto=update` khi dùng Flyway

## Troubleshooting

### Lỗi: "Migration checksum mismatch"
**Nguyên nhân**: File migration đã được sửa đổi sau khi chạy

**Giải pháp**:
```bash
mvn flyway:repair
```

### Lỗi: "Detected failed migration"
**Nguyên nhân**: Migration bị lỗi ở lần chạy trước

**Giải pháp**:
1. Sửa lỗi trong migration file
2. Chạy: `mvn flyway:repair`
3. Chạy lại: `mvn flyway:migrate`

### Lỗi: "Found non-empty schema(s)"
**Nguyên nhân**: Database đã có schema từ trước

**Giải pháp**:
- Flyway đã được cấu hình `baseline-on-migrate=true` để tự động xử lý

## Development vs Production

### Development
- Có thể dùng `flyway:clean` để reset database
- Test migrations nhiều lần
- Dùng `ddl-auto=validate` để kiểm tra

### Production
- KHÔNG BAO GIỜ dùng `flyway:clean`
- Backup trước khi migrate
- Test kỹ migrations trên staging environment
- Monitor migration logs

## Tài liệu tham khảo
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Spring Boot & Flyway](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)
