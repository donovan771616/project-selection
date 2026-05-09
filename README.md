# CPT202 Project Selection

CPT202 project selection system skeleton, built with Spring Boot 2.7.18, Thymeleaf, Spring Security, MyBatis, and MySQL 8.0.

## Database Initialization

Execute the following in MySQL 8.0:

```sql
source sql/schema.sql;
source sql/data.sql;
```

Or execute `sql/schema.sql` first, then `sql/data.sql` in a GUI tool.

Default database name: `cpt202_project_selection`.

## Default Accounts

| Role | Username | Password |
| --- | --- | --- |
| Administrator | `admin` | `admin123` |
| Teacher | `teacher` | `teacher123` |
| Student | `student` | `student123` |

## Local Run

Modify the MySQL username and password in `src/main/resources/application-dev.yml`, then run:

```bash
mvn spring-boot:run
```

Access:

```text
http://localhost:8080/login
```

## Current Completed Scope

- Single-module Spring Boot project structure.
- Spring Security login, logout, registration, and role-based permission skeleton.
- Thymeleaf backend layout.
- RBAC system tables, business tables, and test data SQL.
- Entities, Mappers, Services, and entry pages for topics, categories, and applications modules.

## Verification

```bash
mvn test
```

