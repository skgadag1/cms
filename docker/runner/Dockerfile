ARG REPO
FROM ${REPO}/dxp/runner-base:latest

ENV DB_MASTER_USER="master" \
  DB_HOST="dibh-dev-magnolia.cluster-cfzrzmzspb3b.us-east-1.rds.amazonaws.com" \
  DB_PORT="5432" \
  DB_MASTER_PASSWORD="" \
  DB_PASSWORD="" \
  DB_USER="" \
  DB_NAME="" \
  DEBUG=""

COPY scripts /root/scripts
RUN chmod +x /root/scripts/*.sh
VOLUME ["/root/scripts"]
