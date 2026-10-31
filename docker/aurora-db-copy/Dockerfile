FROM public.ecr.aws/debian/debian:latest

ENV DB_USER="master" \
  DB_HOST="dibh-dev-magnolia.cluster-cfzrzmzspb3b.us-east-1.rds.amazonaws.com" \
  DB_PORT="5432" \
  DB_PASSWORD="" \
  DB_OWNER="" \
  SOURCE_DB="" \
  DEST_SUFFIX="" \
  DEBUG=""

RUN apt-get update \
  && apt-get install postgresql-client -y \
  && rm -f /etc/apt/sources.list.d/sid.list \
  && apt-get clean

COPY scripts /

CMD [ "/copy.sh" ]
