-- PostgreSQL Database Provisioning Script
-- Based on configuration from magnolia-webapp/src/main/tomcatconf/db.properties
--
-- Run this script as a PostgreSQL superuser (e.g., postgres)
-- Example: psql -U postgres -f provision-db.sql

-- Create the database user
CREATE USER magnolia WITH PASSWORD 'your_password_here';

-- Create the database
CREATE DATABASE jcr
    WITH 
    OWNER = magnolia
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Grant all privileges on the database to the user
GRANT ALL PRIVILEGES ON DATABASE jcr TO magnolia;

-- Connect to the jcr database to set schema permissions
\c jcr

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO magnolia;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO magnolia;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO magnolia;

-- Set default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO magnolia;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO magnolia;

-- Display confirmation
\echo 'Database provisioning completed successfully!'
\echo 'Database: jcr'
\echo 'User: magnolia'
\echo 'Connection string: jdbc:postgresql://127.0.0.1:5432/jcr'



