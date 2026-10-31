#!/bin/bash

if [ ! -z "$DEBUG" ]; then
  set -x
fi

if [ -z "${SOURCE_DB}" ];then
 echo "SOURCE_DB variable doesn't exists, exiting"
fi

if [ -z "${DEST_SUFFIX}" ];then
 echo "DEST_SUFFIX variable doesn't exists, exiting"
fi

if [ -z "${DB_HOST}" ]; then
  echo "DB_HOST variable doesn't exists, exiting"
fi

if [ -z "${DB_PORT}" ]; then
  echo "DB_PORT variable doesn't exists, exiting"
fi

if [ -z "${DB_USER}" ]; then
  echo "DB_USER variable doesn't exists, exiting"
fi

if [ -z "${DB_PASSWORD}" ]; then
  echo "DB_PASSWORD variable doesn't exists, exiting"
fi

if [ -z "${DB_OWNER}" ]; then
  echo "DB_OWNER variable doesn't exists, exiting"
fi

function db_create() {
  export PGPASSWORD="${DB_PASSWORD}"
  echo "Create new ${SOURCE_DB}-${DEST_SUFFIX} DB"
  set -e
  pg_dump -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" "${SOURCE_DB}" --file /tmp/dump.sql
  du -hs /tmp/dump.sql

  echo "Creating DB - ${SOURCE_DB}-${DEST_SUFFIX}"
  createdb -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -T template0 -O "${DB_OWNER}" "${SOURCE_DB}-${DEST_SUFFIX}"

  echo "Importing /tmp/dump.sql file into ${SOURCE_DB}-${DEST_SUFFIX} DB"
  psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" "${SOURCE_DB}-${DEST_SUFFIX}" -f /tmp/dump.sql 2>&1 > /dev/null
  echo "Removing /tmp/dump.sql"
  rm -f /tmp/dump.sql
}

function db_remove() {
  export PGPASSWORD="${DB_PASSWORD}"
  echo "Remove ${SOURCE_DB}-${DEST_SUFFIX} DB"
  set -e
  psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d postgres -c "ALTER DATABASE \"${SOURCE_DB}-${DEST_SUFFIX}\" OWNER TO \"${DB_USER}\""
  psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d postgres -c "DROP DATABASE \"${SOURCE_DB}-${DEST_SUFFIX}\""
}

export PGPASSWORD="${DB_PASSWORD}"
psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${SOURCE_DB}" -c "SELECT" 2>&1 > /dev/null
if [ "$?" -ne 0 ]; then
  echo "${SOURCE_DB} DB doesn't exists, exiting"
  exit 1
fi

psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${SOURCE_DB}-${DEST_SUFFIX}" -c "SELECT" 2>&1 > /dev/null
if [ "$?" -eq 2 ] ; then
   db_create
else
  echo "DB already exists"
fi
