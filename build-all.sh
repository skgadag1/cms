#!/bin/bash

VERSION=${1}
if [ -z "$VERSION" ]; then
  echo "Version parameter is missing, exit"
  echo "Run \"$0 <version number>\""
  exit 1
fi
DIR=$(pwd)

cd ${DIR}/magnolia
#mvn clean package
cd magnolia-webapp
./build.sh ${VERSION}
cd ${DIR}/docker/apache
./build.sh ${VERSION}
cd ${DIR}/docker/aurora-db-copy
./build.sh ${VERSION}
cd ${DIR}/docker/runner
./build.sh ${VERSION}
cd $DIR
