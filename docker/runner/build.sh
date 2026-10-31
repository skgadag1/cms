#!/bin/sh

VERSION="$1"

if [ -z ${VERSION} ];then
 echo "Version is missing, exit"
 exit 1
fi

IMAGE="129363003223.dkr.ecr.us-east-1.amazonaws.com/dxp/runner"

aws ecr get-login --no-include-email --region us-east-1 | /bin/bash
if [ "$?"  -eq 0 ]; then
  buildah bud -t "${IMAGE}:${VERSION}" .
  buildah push "${IMAGE}:${VERSION}"
  if [ "$?"  -eq 0 ]; then
    VER="latest"
    TAG_LATEST="${IMAGE}:${VER}"
    buildah tag ${IMAGE}:${VERSION} $TAG_LATEST
    buildah push $TAG_LATEST
  fi
else
  echo -n "Login failed, exiting"
  exit 1
fi

