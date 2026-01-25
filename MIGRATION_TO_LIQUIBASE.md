# Migration từ Flyway sang Liquibase - Hoàn Tất ✅

## Tóm Tắt Thay Đổi

Dự án **Edu-Tool-BE** đã được chuyển đổi thành công từ **Flyway** sang **Liquibase** để quản lý database migrations.

---

## ✅ Các Thay Đổi Đã Thực Hiện

### 1. **Dependencies** ([pom.xml](pom.xml))

**Đã xóa:**
- `org.flywaydb:flyway-core`
- `org.flywaydb:flyway-database-postgresql`
- Flyway Maven Plugin

**Đã thêm:**
- `org.liquibase:liquibase-core`

### 2. **Cấu Hình** ([application.yml](src/main/resources/application.yml))

**Đã thay đổi:**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Đổi từ 'create' sang 'validate'
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    enabled: true
```

### 3. **Cấu Trúc Migration Files**

**Cũ (Flyway):**
```
src/main/resources/db/migration/
├── V1__create_schema.sql
├── V2__init_master_data.sql
├── V3__add_cascade_delete.sql
├── V4__convert_enum_to_string.sql
├── V5__update_schema_for_report_system.sql
└── V6__add_indexes_for_performance.sql
```

**Mới (Liquibase):**
```
src/main/resources/db/changelog/
├── db.changelog-master.yaml          # Master changelog
├── changes/                          # Changelog YAML files
│   ├── 01-create-schema.yaml
│   ├── 02-init-master-data.yaml
│   ├── 03-add-cascade-delete.yaml
│   ├── 04-convert-enum-to-string.yaml
│   ├── 05-update-schema-for-report-system.yaml
│   └── 06-add-indexes-for-performance.yaml
└── sql/                              # SQL files
    ├── V1__create_schema.sql
    ├── V2__init_master_data.sql
    ├── V3__add_cascade_delete.sql
    ├── V4__convert_enum_to_string.sql
    ├── V5__update_schema_for_report_system.sql
    └── V6__add_indexes_for_performance.sql
```

### 4. **Files Đã Xóa**
- ❌ `db/migration/` (thư mục)
- ❌ `FLYWAY_MIGRATION_GUIDE.md`

### 5. **Files Đã Tạo**
- ✅ `db/changelog/db.changelog-master.yaml`
- ✅ `db/changelog/changes/*.yaml` (6 files)
- ✅ `db/changelog/sql/*.sql` (6 files - copy từ Flyway)
- ✅ `LIQUIBASE_GUIDE.md` - Hướng dẫn chi tiết

---

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

## 🎯 So Sánh Flyway vs Liquibase

| Feature | Flyway | Liquibase |
|---------|--------|-----------|
| **Format** | SQL only | SQL, YAML, JSON, XML |
| **Rollback** | ❌ Không (Pro) | ✅ Có |
| **Preconditions** | ❌ | ✅ Có |
| **Diff databases** | ❌ | ✅ Có |
| **Generate changelog** | ❌ | ✅ Có |
| **Context/Labels** | ❌ | ✅ Có |
| **Database-agnostic** | ❌ | ✅ Có |

**Lý do chọn Liquibase:**
- ✅ Hỗ trợ rollback migrations
- ✅ Preconditions để kiểm tra điều kiện
- ✅ Generate changelog từ database
- ✅ Diff giữa databases
- ✅ Database-agnostic changesets
- ✅ Context cho multi-environment

---

## ✅ Checklist Hoàn Thành

- [x] Xóa Flyway dependencies
- [x] Thêm Liquibase dependency
- [x] Tạo cấu trúc thư mục `db/changelog`
- [x] Tạo master changelog file
- [x] Chuyển đổi 6 migration files sang Liquibase format
- [x] Cập nhật [application.yml](src/main/resources/application.yml)
- [x] Xóa thư mục `db/migration`
- [x] Xóa `FLYWAY_MIGRATION_GUIDE.md`
- [x] Tạo [LIQUIBASE_GUIDE.md](LIQUIBASE_GUIDE.md)
- [x] Build thành công (`./mvnw clean install`)
- [x] Tạo file README migration

---

## 🔗 Resources

- [Liquibase Official Docs](https://docs.liquibase.com/)
- [Spring Boot Liquibase](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase)
- [Liquibase Best Practices](https://www.liquibase.org/get-started/best-practices)

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề:

1. Đọc [LIQUIBASE_GUIDE.md](LIQUIBASE_GUIDE.md) phần Troubleshooting
2. Kiểm tra logs khi chạy application
3. Xem bảng `databasechangelog` trong database
4. Liên hệ team backend

---

**Status:** ✅ **Hoàn thành và sẵn sàng sử dụng**

Build successful! Dự án đã sẵn sàng với Liquibase.
