# Hướng Dẫn Sử Dụng Liquibase

## Tổng Quan

Dự án này sử dụng **Liquibase** để quản lý database schema migrations. Liquibase tự động chạy các changesets khi khởi động ứng dụng Spring Boot.

## Cấu Trúc Thư Mục

```
src/main/resources/db/changelog/
├── db.changelog-master.yaml          # Master changelog file
├── changes/                          # Các file changelog YAML
│   ├── 01-create-schema.yaml
│   ├── 02-init-master-data.yaml
│   ├── 03-add-cascade-delete.yaml
│   ├── 04-convert-enum-to-string.yaml
│   ├── 05-update-schema-for-report-system.yaml
│   └── 06-add-indexes-for-performance.yaml
└── sql/                              # Các file SQL được reference từ changelogs
    ├── V1__create_schema.sql
    ├── V2__init_master_data.sql
    ├── V3__add_cascade_delete.sql
    ├── V4__convert_enum_to_string.sql
    ├── V5__update_schema_for_report_system.sql
    └── V6__add_indexes_for_performance.sql
```

## Cấu Hình

Liquibase được cấu hình trong `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/eduTool
    username: postgres
    password: 12345
  jpa:
    hibernate:
      ddl-auto: validate  # Quan trọng: để validate để Liquibase quản lý schema
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
    enabled: true
```

## Dependencies

Trong `pom.xml`:

```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

## Cách Hoạt Động

1. Khi ứng dụng khởi động, Liquibase đọc file `db.changelog-master.yaml`
2. File master này include tất cả các changelog files theo thứ tự
3. Liquibase kiểm tra bảng `databasechangelog` để xem changeset nào đã chạy
4. Chỉ các changeset chưa chạy mới được thực thi
5. Mỗi changeset chạy thành công được ghi vào bảng `databasechangelog`

## Tạo Migration Mới

### Bước 1: Tạo File SQL

Tạo file SQL trong `src/main/resources/db/changelog/sql/`:

```sql
-- V7__add_user_avatar.sql
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500);
```

### Bước 2: Tạo Changelog YAML

Tạo file YAML trong `src/main/resources/db/changelog/changes/`:

```yaml
# 07-add-user-avatar.yaml
databaseChangeLog:
  - changeSet:
      id: 7
      author: your-name
      comment: Add avatar_url column to users table
      changes:
        - sqlFile:
            path: db/changelog/sql/V7__add_user_avatar.sql
            splitStatements: true
            stripComments: true
```

### Bước 3: Include vào Master Changelog

Thêm vào `db.changelog-master.yaml`:

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/changes/01-create-schema.yaml
  # ... các includes khác ...
  - include:
      file: db/changelog/changes/07-add-user-avatar.yaml
```

### Bước 4: Chạy Application

```bash
./mvnw spring-boot:run
```

Liquibase sẽ tự động chạy migration mới.

## Liquibase Maven Plugin (Tùy chọn)

Nếu muốn sử dụng Liquibase CLI qua Maven, thêm plugin vào `pom.xml`:

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

Tạo file `liquibase.properties`:

```properties
changeLogFile=src/main/resources/db/changelog/db.changelog-master.yaml
url=jdbc:postgresql://localhost:5432/eduTool
username=postgres
password=12345
driver=org.postgresql.Driver
```

## Các Lệnh Maven Hữu Ích

```bash
# Xem trạng thái migration
./mvnw liquibase:status

# Chạy migrations
./mvnw liquibase:update

# Rollback 1 changeset
./mvnw liquibase:rollback -Dliquibase.rollbackCount=1

# Rollback đến tag cụ thể
./mvnw liquibase:rollback -Dliquibase.rollbackTag=version-1.0

# Clear checksums (khi có warning về checksum)
./mvnw liquibase:clearCheckSums

# Tạo diff so với database khác
./mvnw liquibase:diff

# Generate changelog từ database hiện tại
./mvnw liquibase:generateChangeLog

# Xem SQL sẽ được thực thi (không chạy thật)
./mvnw liquibase:updateSQL
```

## Format Changelog

### 1. Sử dụng SQL File (Khuyên dùng cho migration phức tạp)

```yaml
databaseChangeLog:
  - changeSet:
      id: 8
      author: dev-name
      changes:
        - sqlFile:
            path: db/changelog/sql/V8__complex_migration.sql
            splitStatements: true
            stripComments: true
```

### 2. Sử dụng SQL Inline

```yaml
databaseChangeLog:
  - changeSet:
      id: 9
      author: dev-name
      changes:
        - sql:
            sql: ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

### 3. Sử dụng Liquibase Change Types (Type-safe)

```yaml
databaseChangeLog:
  - changeSet:
      id: 10
      author: dev-name
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: phone
                  type: VARCHAR(20)
        - createIndex:
            indexName: idx_user_phone
            tableName: users
            columns:
              - column:
                  name: phone
```

## Preconditions (Điều kiện tiên quyết)

Kiểm tra điều kiện trước khi chạy changeset:

```yaml
databaseChangeLog:
  - changeSet:
      id: 11
      author: dev-name
      preConditions:
        - onFail: MARK_RAN  # Đánh dấu đã chạy nếu fail
        - not:
            - columnExists:
                tableName: users
                columnName: phone
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: phone
                  type: VARCHAR(20)
```

Các giá trị `onFail`:
- `HALT`: Dừng migration
- `MARK_RAN`: Đánh dấu đã chạy và tiếp tục
- `WARN`: Cảnh báo và tiếp tục
- `CONTINUE`: Bỏ qua và tiếp tục

## Rollback

### Tự động Rollback

Liquibase tự động tạo rollback cho một số operations đơn giản như `addColumn`, `createTable`.

### Custom Rollback

```yaml
databaseChangeLog:
  - changeSet:
      id: 12
      author: dev-name
      changes:
        - sql:
            sql: ALTER TABLE users ADD COLUMN temp_field VARCHAR(100);
      rollback:
        - sql:
            sql: ALTER TABLE users DROP COLUMN temp_field;
```

## Context và Labels

Chạy migration cho môi trường cụ thể:

```yaml
databaseChangeLog:
  - changeSet:
      id: 13
      author: dev-name
      context: dev  # Chỉ chạy ở môi trường dev
      changes:
        - insert:
            tableName: users
            columns:
              - column:
                  name: username
                  value: test_user
```

Trong `application.yml`:

```yaml
spring:
  liquibase:
    contexts: dev  # Chỉ chạy changesets có context=dev
```

## Tagging

Đánh dấu version để rollback dễ dàng:

```yaml
databaseChangeLog:
  - include:
      file: db/changelog/changes/01-create-schema.yaml
  # ... các migrations ...
  - changeSet:
      id: tag-v1.0
      author: system
      changes:
        - tagDatabase:
            tag: version-1.0
```

Rollback về tag:

```bash
./mvnw liquibase:rollback -Dliquibase.rollbackTag=version-1.0
```

## Best Practices

### 1. Không Sửa Changeset Đã Deploy

❌ **Sai:**
```yaml
# KHÔNG BAO GIỜ sửa changeset đã deploy
- changeSet:
    id: 5
    changes:
      - addColumn:  # Sửa từ VARCHAR(50) thành VARCHAR(100)
          tableName: users
          columns:
            - column:
                name: name
                type: VARCHAR(100)  # ĐÃ SỬA
```

✅ **Đúng:**
```yaml
# Tạo changeset mới để thay đổi
- changeSet:
    id: 14
    changes:
      - modifyDataType:
          tableName: users
          columnName: name
          newDataType: VARCHAR(100)
```

### 2. ID Duy Nhất

Mỗi changeset phải có ID duy nhất (kết hợp với author):

```yaml
- changeSet:
    id: 15  # ID số hoặc chuỗi mô tả
    author: john.doe
```

### 3. Comment Rõ Ràng

```yaml
- changeSet:
    id: 16
    author: dev-team
    comment: Add email verification fields to support 2FA authentication
    changes:
      # ...
```

### 4. Sử dụng Rollback

Luôn cung cấp rollback cho các thay đổi quan trọng:

```yaml
- changeSet:
    id: 17
    changes:
      - dropColumn:
          tableName: users
          columnName: old_field
    rollback:
      - addColumn:
          tableName: users
          columns:
            - column:
                name: old_field
                type: VARCHAR(50)
```

### 5. Tách Changesets Nhỏ

❌ Một changeset làm nhiều việc:
```yaml
- changeSet:
    id: 18
    changes:
      - createTable: {...}
      - addColumn: {...}
      - createIndex: {...}
      - insert: {...}
```

✅ Tách thành nhiều changesets:
```yaml
- changeSet:
    id: 18
    changes:
      - createTable: {...}
      
- changeSet:
    id: 19
    changes:
      - addColumn: {...}
```

### 6. Tránh Hardcode Giá Trị

❌ Hardcode:
```sql
INSERT INTO config VALUES (1, 'app.url', 'http://localhost:8080');
```

✅ Sử dụng property:
```yaml
- changeSet:
    id: 20
    changes:
      - insert:
          tableName: config
          columns:
            - column:
                name: key
                value: app.url
            - column:
                name: value
                value: ${app.url:http://localhost:8080}
```

## Kiểm Tra Database Tables

Liquibase tạo 2 bảng tracking:

### 1. `databasechangelog`

Lưu lịch sử các changeset đã chạy:

```sql
SELECT * FROM databasechangelog ORDER BY dateexecuted DESC;
```

Columns:
- `id`: ID của changeset
- `author`: Tác giả
- `filename`: File chứa changeset
- `dateexecuted`: Thời gian chạy
- `orderexecuted`: Thứ tự chạy
- `exectype`: Loại execution (EXECUTED, RERAN, etc.)
- `md5sum`: Checksum để kiểm tra thay đổi

### 2. `databasechangeloglock`

Quản lý lock để tránh conflict:

```sql
SELECT * FROM databasechangeloglock;
```

## Troubleshooting

### 1. Lỗi Checksum Mismatch

**Nguyên nhân:** File changeset đã bị sửa sau khi deploy.

**Giải pháp:**

```bash
# Clear checksums
./mvnw liquibase:clearCheckSums
```

Hoặc:

```sql
UPDATE databasechangelog SET md5sum = NULL WHERE id = 'changeset-id';
```

### 2. Lock Không Được Release

**Triệu chứng:** Lỗi "Waiting for changelog lock..."

**Giải pháp:**

```sql
UPDATE databasechangeloglock SET locked = FALSE, lockgranted = NULL, lockedby = NULL WHERE id = 1;
```

### 3. Changeset Bị Bỏ Qua

**Nguyên nhân:** Changeset đã có trong `databasechangelog` với status khác EXECUTED.

**Kiểm tra:**

```sql
SELECT * FROM databasechangelog WHERE id = 'your-changeset-id';
```

**Giải pháp:**

```sql
-- Xóa record để chạy lại (CHỈ làm trong development)
DELETE FROM databasechangelog WHERE id = 'your-changeset-id';
```

### 4. Migration Chạy Trên Database Sai

**Giải pháp:** Kiểm tra `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/eduTool  # Kiểm tra URL
    username: postgres
    password: 12345
  liquibase:
    enabled: true  # Đảm bảo enabled = true
```

### 5. Disable Liquibase Tạm Thời

Trong `application.yml` hoặc `application-dev.yml`:

```yaml
spring:
  liquibase:
    enabled: false
```

Hoặc command line:

```bash
./mvnw spring-boot:run -Dspring.liquibase.enabled=false
```

## So Sánh Với Flyway

| Feature | Flyway | Liquibase |
|---------|--------|-----------|
| Format | SQL only | SQL, YAML, JSON, XML |
| Rollback | ❌ Không (Pro version có) | ✅ Có |
| Preconditions | ❌ | ✅ Có |
| Database-agnostic | ❌ | ✅ (type-safe changes) |
| Generate changelog | ❌ | ✅ Có |
| Diff databases | ❌ | ✅ Có |
| Context/Labels | ❌ | ✅ Có |
| Learning curve | Dễ | Trung bình |
| Phổ biến | Rất cao | Cao |

## Ví Dụ Thực Tế

### 1. Thêm Column Mới

```yaml
databaseChangeLog:
  - changeSet:
      id: add-phone-to-users
      author: backend-team
      comment: Add phone number field for user contact
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: phone_number
                  type: VARCHAR(20)
                  constraints:
                    nullable: true
        - createIndex:
            indexName: idx_user_phone
            tableName: users
            columns:
              - column:
                  name: phone_number
      rollback:
        - dropIndex:
            indexName: idx_user_phone
            tableName: users
        - dropColumn:
            tableName: users
            columnName: phone_number
```

### 2. Modify Column

```yaml
databaseChangeLog:
  - changeSet:
      id: extend-username-length
      author: backend-team
      changes:
        - modifyDataType:
            tableName: users
            columnName: username
            newDataType: VARCHAR(100)
```

### 3. Rename Column

```yaml
databaseChangeLog:
  - changeSet:
      id: rename-full-name-column
      author: backend-team
      changes:
        - renameColumn:
            tableName: users
            oldColumnName: full_name
            newColumnName: display_name
```

### 4. Create Table

```yaml
databaseChangeLog:
  - changeSet:
      id: create-notifications-table
      author: backend-team
      changes:
        - createTable:
            tableName: notifications
            columns:
              - column:
                  name: id
                  type: BIGSERIAL
                  constraints:
                    primaryKey: true
                    nullable: false
              - column:
                  name: user_id
                  type: BIGINT
                  constraints:
                    nullable: false
                    foreignKeyName: fk_notification_user
                    references: users(user_id)
              - column:
                  name: message
                  type: TEXT
              - column:
                  name: is_read
                  type: BOOLEAN
                  defaultValueBoolean: false
              - column:
                  name: created_at
                  type: TIMESTAMP
                  defaultValueComputed: CURRENT_TIMESTAMP
        - createIndex:
            indexName: idx_notification_user
            tableName: notifications
            columns:
              - column:
                  name: user_id
```

### 5. Insert Data

```yaml
databaseChangeLog:
  - changeSet:
      id: insert-default-roles
      author: backend-team
      context: dev,staging,prod
      changes:
        - insert:
            tableName: roles
            columns:
              - column:
                  name: name
                  value: ADMIN
              - column:
                  name: description
                  value: Administrator role
        - insert:
            tableName: roles
            columns:
              - column:
                  name: name
                  value: USER
              - column:
                  name: description
                  value: Regular user role
```

## Tài Liệu Tham Khảo

- [Liquibase Official Documentation](https://docs.liquibase.com/)
- [Liquibase Best Practices](https://www.liquibase.org/get-started/best-practices)
- [Spring Boot & Liquibase](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.liquibase)
- [Liquibase Change Types](https://docs.liquibase.com/change-types/home.html)
- [Liquibase Maven Plugin](https://docs.liquibase.com/tools-integrations/maven/home.html)

## Liên Hệ

Nếu có vấn đề hoặc câu hỏi về Liquibase migration, liên hệ team backend.
