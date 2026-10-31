#!/bin/bash

if [ ! -z "$DEBUG" ]; then
  set -x
fi

DATE=$(date +%s)
BACKUP_PATH="/dst/$DATE"
SRC="${SRC:-/src}"
INSTANCE="${INSTANCE:magnolia-nonpublic}"

function check_var(){
  if [ -z "$1" ]; then
    echo "$1 variable is empty, exiting"
    exit 1
  fi
}

check_var $ENV
check_var $DB_PASSWORD
check_var $DB_URL
check_var $DB_USER

aws eks update-kubeconfig --region us-east-1 --profile $ENV --name db-dxp-eks

if [ ! -d "$BACKUP_PATH" ]; then
  mkdir -p $BACKUP_PATH
fi

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

PUBLISHER_REV_VALUE=1
PUBLIC_REV_VALUE=2
export PGPASSWORD=$DB_PASSWORD
DB_NAME=$(echo $DB_URL | cut -d"/" -f4)
check_var $DB_NAME
DB_HOST=$(echo $DB_URL | cut -d"/" -f3 | cut -d":" -f1)
check_var $DB_HOST
while [ "$PUBLISHER_REV_VALUE" != "$PUBLIC_REV_VALUE" ]; do
echo "Compare $PUBLISHER_REV_VALUE with $PUBLIC_REV_VALUE"
PUBLISHER_REV_VALUE=$(psql -A -t -U $DB_USER -h $DB_HOST -d $DB_NAME -c "SELECT revision_id FROM public.journal_local_revisions WHERE journal_id = 'magnolia-public-cluster'")
PUBLIC_REV_VALUE=$(psql -A -t -U $DB_USER -h $DB_HOST -d $DB_NAME -c "SELECT revision_id FROM public.journal_local_revisions WHERE journal_id = 'magnolia-non-public-instance'")
sleep 5
done

rsync -rtvog --exclude=cache --exclude=tmp --exclude=logs --ignore-missing-args $SRC/public $BACKUP_PATH
rsync -rtvog --exclude=cache --exclude=tmp --exclude=logs --ignore-missing-args $SRC/public $BACKUP_PATH
echo "$PUBLIC_REV_VALUE" > $BACKUP_PATH/revision
CODE="$?"
touch $BACKUP_PATH/public/$PUBLIC_REV_VALUE

# Retry rm -rf because NFS can return a transient ENOTEMPTY when a just-emptied
# directory is removed before the server-side listing settles; a later pass clears it.
# A directory still present after all retries marks the backup status as failed.
echo "Remove backups older than ${BACKUP_RETENTION}"
while IFS= read -r OLD_BACKUP; do
  [ -z "$OLD_BACKUP" ] && continue
  for attempt in 1 2 3; do
    rm -rf "$OLD_BACKUP"
    [ ! -e "$OLD_BACKUP" ] && break
    sleep 5
  done
  if [ -e "$OLD_BACKUP" ]; then
    echo "Failed to remove old backup $OLD_BACKUP"
    CODE=1
  fi
done < <(find /dst/ -mindepth 1 -maxdepth 1 -type d -mtime +${BACKUP_RETENTION})

#"Scale up"
#kubectl scale statefulset ${INSTANCE} --replicas=1
aws cloudwatch put-metric-data --region us-east-1 --metric-name backup-status --dimensions "Type=Public"  --namespace "CMS" --value $CODE
