FROM 994669786680.dkr.ecr.us-east-1.amazonaws.com/dxp/apache-base:latest

ARG VERSION

ENV AUTHOR_DOMAIN="author.dev" \
  PUBLISHER_DOMAIN="publisher.dev" \
  PUBLIC_DOMAIN="public.dev" \
  KAGS_DOMAIN="kags.dev" \
  BOBCAT_DOMAIN="bobcat.dev" \
  POC_DOMAIN="poc.dev" \
  #Magnolia API domain
  API_DOMAIN="api.dev" \
  MAGNOLIA_PUBLIC_HOST="http://localhost:8080/" \
  MAGNOLIA_AUTHOR_HOST="http://magnolia-author:8080/" \
  MAGNOLIA_PUBLISHER_HOST="http://magnolia-publisher:8080/"

# Simple startup script to avoid some issues observed with container restart
COPY configs /etc/httpd
COPY run-httpd.sh /run-httpd.sh

RUN echo {\"version\":\"$VERSION\"} > /var/www/html/monitor.html \
    && echo "${version}" > /version.txt
