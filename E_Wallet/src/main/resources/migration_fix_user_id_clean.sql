-- Clean migration script for development (drops and recreates tables)
-- WARNING: This will delete all existing data!
-- Use this only if you're okay with losing all data

-- Drop tables in correct order (drop dependent tables first)
DROP TABLE IF EXISTS wallets;
DROP TABLE IF EXISTS users;

-- Tables will be recreated automatically by Hibernate on next startup
-- with the correct UUID column types

