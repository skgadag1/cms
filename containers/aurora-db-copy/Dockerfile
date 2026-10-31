FROM 994669786680.dkr.ecr.us-east-1.amazonaws.com/dxp/tools-base:latest

ENV DB_USER="master" \
  DB_HOST="dibh-dev-magnolia.cluster-cfzrzmzspb3b.us-east-1.rds.amazonaws.com" \
  DB_PORT="5432" \
  DB_PASSWORD="" \
  DB_OWNER="" \
  SOURCE_DB="" \
  DEST_SUFFIX="" \
  DEBUG=""

COPY scripts /

CMD [ "/copy.sh" ]
