#!/bin/bash -x
DATE=$(date +%s)
BACKUP_PATH="/dst/public/$DATE"
SRC="${SRC:-/src}"

if [ ! -d "$BACKUP_PATH" ]; then
mkdir -p $BACKUP_PATH
fi

PUBLISHER_REV_VALUE=1
PUBLIC_REV_VALUE=2
export PGPASSWORD=$DB_PASS
DB_NAME=$(echo $DB_URL | cut -d"/" -f4)
DB_HOST=$(echo $DB_URL | cut -d"/" -f3 | cut -d":" -f1)
while [ "$PUBLISHER_REV_VALUE" != "$PUBLIC_REV_VALUE" ]; do
echo "Compare $PUBLISHER_REV_VALUE with $PUBLIC_REV_VALUE"
PUBLISHER_REV_VALUE=$(psql -A -t -U $DB_USER -h $DB_HOST -d $DB_NAME -c "SELECT revision_id FROM public.journal_local_revisions WHERE journal_id = 'magnolia-public-cluster'")
PUBLIC_REV_VALUE=$(psql -A -t -U $DB_USER -h $DB_HOST -d $DB_NAME -c "SELECT revision_id FROM public.journal_local_revisions WHERE journal_id = 'magnolia-non-public-instance'")
sleep 5
done

rsync -rtvog --exclude=cache --exclude=tmp --exclude=logs $SRC $BACKUP_PATH
echo "$PUBLIC_REV_VALUE" > $BACKUP_PATH/revision
touch $BACKUP_PATH/public/$PUBLIC_REV_VALUE
