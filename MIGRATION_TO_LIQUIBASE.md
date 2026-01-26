## 🚀 Cách Sử Dụng

### 1. **Chạy Application**

```bash
./mvnw spring-boot:run
```

Liquibase sẽ tự động:
- Tạo bảng `databasechangelog` và `databasechangeloglock`
- Chạy tất cả changesets chưa execute
- Ghi lại lịch sử migration

### 2. **Tạo Migration Mới**

**Bước 1:** Tạo file SQL
```bash
# src/main/resources/db/changelog/sql/V7__your_migration.sql
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500);
```

**Bước 2:** Tạo file changelog YAML
```yaml
# src/main/resources/db/changelog/changes/07-your-migration.yaml
databaseChangeLog:
  - changeSet:
      id: 7
      author: your-name
      comment: Add avatar_url to users
      changes:
        - sqlFile:
            path: db/changelog/sql/V7__your_migration.sql
            splitStatements: true
            stripComments: true
```

**Bước 3:** Include vào master
```yaml
# db/changelog/db.changelog-master.yaml
databaseChangeLog:
  # ... các includes hiện tại ...
  - include:
      file: db/changelog/changes/07-your-migration.yaml
```

**Bước 4:** Chạy application để apply migration

### 3. **Kiểm Tra Database**

```sql
-- Xem các migrations đã chạy
SELECT * FROM databasechangelog ORDER BY dateexecuted DESC;

-- Kiểm tra lock status
SELECT * FROM databasechangeloglock;
```

---

## 📚 Tài Liệu

Xem hướng dẫn chi tiết trong [LIQUIBASE_GUIDE.md](LIQUIBASE_GUIDE.md):

- ✅ Cấu hình và cách hoạt động
- ✅ Tạo migration mới
- ✅ Rollback changesets
- ✅ Preconditions và context
- ✅ Best practices
- ✅ Troubleshooting
- ✅ Ví dụ thực tế

---

## 🔧 Lưu Ý Quan Trọng

### ⚠️ Nếu Database Đã Có Dữ Liệu

Bạn có 2 lựa chọn:

**Option 1: Xóa và tạo lại database** (Khuyên dùng cho development)
```sql
DROP DATABASE edutool;
CREATE DATABASE edutool;
```
Sau đó chạy application, Liquibase sẽ tạo toàn bộ schema mới.

**Option 2: Sync với database hiện tại** (Nếu muốn giữ dữ liệu)

1. Thêm Liquibase Maven plugin vào [pom.xml](pom.xml):
```xml
<plugin>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-maven-plugin</artifactId>
    <version>4.30.0</version>
    <configuration>
        <propertyFile>src/main/resources/liquibase.properties</propertyFile>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.4</version>
        </dependency>
    </dependencies>
</plugin>
```

2. Tạo `liquibase.properties`:
```properties
changeLogFile=src/main/resources/db/changelog/db.changelog-master.yaml
url=jdbc:postgresql://localhost:5432/eduTool
username=postgres
password=12345
driver=org.postgresql.Driver
```

3. Chạy changelogSync:
```bash
./mvnw liquibase:changelogSync
```

### ⚠️ JPA Hibernate DDL-Auto

File [application.yml](src/main/resources/application.yml) đã được cập nhật:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # ĐỔI TỪ 'create' SANG 'validate'
```

**Lý do:** 
- `validate`: Hibernate chỉ kiểm tra schema, không tự động tạo/sửa
- Liquibase hoàn toàn quản lý schema migrations
- Tránh conflict giữa Hibernate và Liquibase

---