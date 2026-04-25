# Local PostgreSQL Setup

This project expects the following local database credentials:

```text
Database: ai_tutor
Username: ai_tutor
Password: ai_tutor
Host: localhost
Port: 5432
```

## 1. Open psql As Your Local Admin User

```bash
psql -d postgres
```

## 2. Create Or Update The App User

Paste this at the `postgres=#` prompt:

```sql
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'ai_tutor') THEN
    CREATE ROLE ai_tutor LOGIN PASSWORD 'ai_tutor';
  ELSE
    ALTER ROLE ai_tutor LOGIN PASSWORD 'ai_tutor';
  END IF;
END
$$;
```

## 3. Create The Database If Missing

Paste this at the `postgres=#` prompt:

```sql
SELECT 'CREATE DATABASE ai_tutor OWNER ai_tutor'
WHERE NOT EXISTS (
  SELECT 1 FROM pg_database WHERE datname = 'ai_tutor'
)
\gexec
```

## 4. Grant Database Permissions

Paste this at the `postgres=#` prompt:

```sql
ALTER DATABASE ai_tutor OWNER TO ai_tutor;
GRANT ALL PRIVILEGES ON DATABASE ai_tutor TO ai_tutor;
```

## 5. Connect To The App Database As Your Admin User

Run this command by itself:

```sql
\c ai_tutor
```

Your prompt should change to:

```text
ai_tutor=#
```

## 6. Grant Schema Permissions

Paste this only after you are connected to `ai_tutor`:

```sql
GRANT USAGE, CREATE ON SCHEMA public TO ai_tutor;
ALTER SCHEMA public OWNER TO ai_tutor;
```

## 7. Test The App User Login

Run this command by itself:

```sql
\connect ai_tutor ai_tutor localhost 5432
```

When prompted for the password, enter:

```text
ai_tutor
```

Then run:

```sql
SELECT current_database(), current_user;
```

Expected result:

```text
 current_database | current_user
------------------+--------------
 ai_tutor         | ai_tutor
```

## 8. Backend Connection Settings

The Spring Boot backend defaults already match this setup:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/ai_tutor
DATABASE_USERNAME=ai_tutor
DATABASE_PASSWORD=ai_tutor
```

To run the backend:

```bash
cd backend
mvn spring-boot:run
```

