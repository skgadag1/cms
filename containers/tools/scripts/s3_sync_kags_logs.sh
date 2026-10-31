#!/bin/bash
set -e

if [ ! -z "$DEBUG" ]; then
  echo "Enable debugging"
  set -x
fi

function check_var(){
  if [ -z "$1" ]; then
    echo "\$1 variable is empty, exiting"
    exit 1
  fi
}

SRC="${SRC:-/src}"

check_var $S3_BUCKET

for f in $(ls $SRC/); do
  if [ ! -s "${SRC}/$f" ]; then
    echo "Checking is file ${SRC}/$f is not open and can be removed"
    fuser -s "${SRC}/$f" || rm -f "${SRC}/$f"
  fi 
done

aws s3 sync $SRC s3://${S3_BUCKET}/logs
