#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER $BET_DB_USER WITH PASSWORD '$BET_DB_PASSWORD';
    CREATE DATABASE $BET_DB_NAME;
    GRANT ALL PRIVILEGES ON DATABASE $BET_DB_NAME TO $BET_DB_USER;
    ALTER DATABASE $BET_DB_NAME SET TIMEZONE='Europe/Amsterdam';
    \c $BET_DB_NAME
    ALTER SCHEMA public OWNER TO $BET_DB_USER;
    GRANT ALL PRIVILEGES ON SCHEMA public TO $BET_DB_USER;
EOSQL