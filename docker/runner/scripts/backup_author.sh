#!/bin/bash

if [ -n "${DEBUG}" ]; then
  set -x
fi

DATE=$(date +%s)
BACKUP_PATH="/dst/${DATE}"
SRC="${SRC:-/src}"
INSTANCE="${INSTANCE:magnolia-author}"

function check_var(){
  if [ -z "$1" ]; then
    echo "$1 variable is empty, exiting"
    exit 1
  fi
}

check_var "${ENV}"
#check_var $DB_PASSWORD
#check_var $DB_URL
#check_var $DB_USER

aws eks update-kubeconfig --region us-east-1 --profile "${ENV}" --name db-dxp-eks

if [ ! -d "${BACKUP_PATH}" ]; then
  mkdir -p "${BACKUP_PATH}"
fi

if [ -z "${SOURCE_DB}" ];then
 echo "SOURCE_DB variable doesn't exists, exiting"
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

function backup_database() {
  export PGPASSWORD="${DB_PASSWORD}"
  echo "Backup Magnolia Author DB"
  set -e
  if [[ ! -d "${BACKUP_PATH}/${SOURCE_DB}" ]]; then
    mkdir -p "${BACKUP_PATH}/${SOURCE_DB}"
  fi

  time pg_dump -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" "${SOURCE_DB}" --file "${BACKUP_PATH}/${SOURCE_DB}/${SOURCE_DB}_${DATE}_dump.sql"
  du -hs "${BACKUP_PATH}/${SOURCE_DB}/${SOURCE_DB}_${DATE}_dump.sql"

  echo "${DATE}" > /dst/latest-backup.txt

  LATEST_BACKUP=$(cat /dst/latest-backup.txt)
  echo "The latest backup ID - date: ${LATEST_BACKUP}"

}

#kubectl scale statefulset ${INSTANCE} --replicas=1

#while ! kubectl logs ${INSTANCE}-0 2>&1 | grep "Server startup in"; do
#  echo "Checking if ${INSTANCE}-0 is up"
#  sleep 5
#done

#"Scale down, scale down" - we need to shutdown the instance to get clean backup

#kubectl scale statefulset ${INSTANCE} --replicas=0

#while kubectl describe pod ${INSTANCE}-0 2>&1 > /dev/null; do
#  sleep 2
#  echo "Checking if ${INSTANCE}-0 is down"
#done


#export PGPASSWORD=$DB_PASSWORD
#DB_NAME=$(echo $DB_URL | cut -d"/" -f4)
#check_var $DB_NAME
#DB_HOST=$(echo $DB_URL | cut -d"/" -f3 | cut -d":" -f1)
#check_var $DB_HOST
#AUTHOR_REV_VALUE=$(psql -A -t -U $DB_USER -h $DB_HOST -d $DB_NAME -c "SELECT revision_id FROM public.journal_local_revisions WHERE journal_id = 'magnolia-author-instance'")

rsync -rtvog --exclude=cache --exclude=tmp --exclude=logs --ignore-missing-args ${SRC}/author "${BACKUP_PATH}"
rsync -rtvog --exclude=cache --exclude=tmp --exclude=logs --ignore-missing-args ${SRC}/author "${BACKUP_PATH}"
CODE="$?"
#echo "$AUTHOR_REV_VALUE" > $BACKUP_PATH/revision
#touch $BACKUP_PATH/author/$AUTHOR_REV_VALUE

echo "Remove backups older than ${BACKUP_RETENTION}"
find /dst/ -maxdepth 1 -type d -mtime +${BACKUP_RETENTION} -exec rm -rf {} \;

#"Scale up"
#kubectl scale statefulset ${INSTANCE} --replicas=1
aws cloudwatch put-metric-data --region us-east-1 --metric-name backup-status --dimensions "Type=Author"  --namespace "CMS" --value "${CODE}"

# Currently test in develop
if [[ ${ENV} == 'dev' ]]; then
  backup_database
fi
