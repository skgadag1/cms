#!/bin/bash
export DOMAIN_NAME='dev.dice-tools.com'
export PUBLIC_DOMAIN_NAME='dev.dice-tools.com'
export KAGS_SUBDOMAIN='dxp-kags'
export BOBCAT_PUBLIC_DOMAIN_NAME='dev.bobcat.com'
export DB_HOST='dibh-dev-magnolia-db-cluster.cluster-cfzrzmzspb3b.us-east-1.rds.amazonaws.com'
export DB_HOST_RO='dibh-dev-magnolia-db-cluster.cluster-ro-cfzrzmzspb3b.us-east-1.rds.amazonaws.com'
export EFS_DATA_HOST='fs-250393c7.efs.us-east-1.amazonaws.com'
export EFS_BACKUP_HOST='fs-4afaaaa8.efs.us-east-1.amazonaws.com'
export SSL_CERT_ARN='arn:aws:acm:us-east-1:452295442916:certificate/5ca979a3-a9b3-45a7-9c7a-6bc4172e7676'
export MAGNOLIA_PUBLIC_JAVA_HEAP='3096M'
export TOMCAT_VERSION='9.0'
export CATALINA_EXTRA_OPTIONS='-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1099 -Dcom.sun.management.jmxremote.rmi.port=1099 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.rmi.server.hostname=127.0.0.1 -Dcom.sun.management.jmxremote.local.only=false'
export ENV_NAME='dev'
export AWS_ECR_REPOSITORY='452295442916.dkr.ecr.us-east-1.amazonaws.com'
export BUILD_NUMBER='2552'

helm upgrade magnolia-cms "helm/stable" \
	--install --atomic --wait --set env="${ENV_NAME}" --set efs.data.host="${EFS_DATA_HOST}" --set efs.backup.host="${EFS_BACKUP_HOST}" --set db.magnolia_author.url="jdbc:postgresql://${DB_HOST}:${DB_PORT}" --set db.magnolia_publisher.url="jdbc:postgresql://${DB_HOST}:${DB_PORT}" --set db.magnolia_public.url="jdbc:postgresql://${DB_HOST_RO}:${DB_PORT}" --set domain_name="${DOMAIN_NAME}" --set public_domain_name="${PUBLIC_DOMAIN_NAME}" --set bobcat_public_domain_name="${BOBCAT_PUBLIC_DOMAIN_NAME}" --set ssl_cert_arn="${SSL_CERT_ARN}" --set image.apache.repository="${AWS_ECR_REPOSITORY}/dxp/apache" --set image.apache.tag='latest' --set image.magnolia.repository="${AWS_ECR_REPOSITORY}/dxp/magnolia" --set image.magnolia.tag="${BUILD_NUMBER}" --set image.runner.repository="${AWS_ECR_REPOSITORY}/dxp/runner" --set service.magnolia_public.java_heap="${MAGNOLIA_PUBLIC_JAVA_HEAP}"  --set service.magnolia_author.catalina_extra_options="${CATALINA_EXTRA_OPTIONS}" --timeout 2200s --namespace default
