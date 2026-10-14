-- AcxiomCRM - MySQL setup (Phase 1)
--
-- Run this ONCE as a MySQL administrator. The application never creates its own
-- database or user, and never needs the root password afterwards.
--
--   mysql -u root -p < docs/mysql-setup.sql
--
-- Then start the backend with the same username and the password you choose:
--
--   export DB_NAME=acxiomcrm
--   export DB_USER=acxiom
--   export DB_PASSWORD='<the password you set below>'
--   ./mvnw spring-boot:run
--
-- The password is deliberately NOT stored here. application.properties reads
-- DB_USER / DB_PASSWORD / DB_NAME from the environment so no credential is ever
-- committed to source control (specification §14).

CREATE DATABASE IF NOT EXISTS acxiomcrm
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Replace 'CHANGE_ME_STRONG_PASSWORD' before running.
-- Use a password that satisfies the application policy: at least 8 characters
-- with a mix of character classes.
CREATE USER IF NOT EXISTS 'acxiom'@'localhost'
    IDENTIFIED BY 'CHANGE_ME_STRONG_PASSWORD';

-- Least privilege: the application needs to read and write data, not to alter
-- the server. It DOES need DDL rights because Phase 1 lets Hibernate create the
-- schema on first start (spring.jpa.hibernate.ddl-auto=update). Once the schema
-- exists and is stable, this can be narrowed to SELECT, INSERT, UPDATE, DELETE.
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
    ON acxiomcrm.* TO 'acxiom'@'localhost';

FLUSH PRIVILEGES;

-- Verify:
--   SHOW DATABASES LIKE 'acxiomcrm';
--   SHOW GRANTS FOR 'acxiom'@'localhost';
--
-- The application creates these tables on first start:
--   roles, users, customers, leads, opportunities, audit_logs
-- "users" is the identity table mapped by the AppUser entity. It is not a
-- reserved word in MySQL 8; the reserved one is USER (the function).
--
-- Seed accounts created by DataSeeder on every start (idempotent):
--   admin@acxiomcrm.local   / Admin@123     (ADMIN)
--   manager@acxiomcrm.local / Manager@123   (MANAGER)
--   sales@acxiomcrm.local   / Sales@123     (SALES_EXECUTIVE)
-- Change these before any real deployment.
