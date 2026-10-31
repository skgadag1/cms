ARG TOMCAT_VERSION="9.0.95"
ARG VERSION
FROM tomcat:${TOMCAT_VERSION}-jre17-temurin-jammy AS base_image
USER root
RUN apt-get update \
  && apt-get install -y --no-install-recommends vim curl postgresql-client brotli libbrotli1\
  && curl -L https://github.com/stedolan/jq/releases/download/jq-1.6/jq-linux64 --output /bin/jq \
  && chmod +x /bin/jq \
  && apt-get purge -y --auto-remove -o APT::AutoRemove::RecommendsImportant=false $buildDeps \
  && rm -rf /var/lib/apt/lists/* \
  && rm -rf /usr/local/tomcat/webapps/* \
  && chmod 777 /usr/local/tomcat/webapps

FROM base_image

# We are using tomcat base image with deleted webapps folder
# as this is a problem if you build as non-root user
# magnolia development mode switch
ARG BACKUP_MODULE_VERSION=2.5.1

ENV CATALINA_BASE=${CATALINA_BASE:-$CATALINA_HOME}

ENV MAGNOLIA_DEV_MODE="false" \
# possible values: local, dev, qa, prod
  ENVIRONMENT="local" \
# magnolia instance type
# possible values: author, publisher, public
  INSTANCE_TYPE="author" \
# name of the feature id, used only for feature environment setup
  FEATURE_ID="" \
# Database type to use as a JCR storage backend, by default magnolia uses filesystem-based h2 DB
# value can be set to 'postgres', then it requires valid values in DB_URL, DB_USER and DB_PASSWORD
  DB_TYPE="" \
# JDBC compatible database url, example: jdbc:postgresql://hostname:5432/magnolia
  DB_URL="" \
  # JDBC compatible database url for RW, example: jdbc:postgresql://hostname:5432/magnolia
  DB_URL_RW="" \
# Database user
  DB_USER="" \
# Database password
  DB_PASSWORD="" \
# Java settings
  CATALINA_OPTS="-Xms4096M -Xmx4096M -Xss10M" \
# Magnolia home directory, where all the files are written, should not be changed
  MAGNOLIA_DATA_DIR="/var/magnolia" \
# Directory containing light modules, only for local environment, ignored on dev, qa & prod
  MAGNOLIA_RESOURCES_DIR="${CATALINA_BASE}/webapps/ROOT/modules" \
# DXP-350 We need unique but more stable clusterIDs for public instances to be able to ID nodes and do the cleanup
  CUSTOM_JCR_CLUSTERIDS="false"

RUN  mkdir -p ${MAGNOLIA_DATA_DIR}/logs \
  && mkdir -p ${MAGNOLIA_DATA_DIR}/dumps \
  && chown -R 5000.5000 ${MAGNOLIA_DATA_DIR} \
  && chown -R 5000.5000 /usr/local/tomcat/conf \
  && chown -R 5000.5000 /usr/local/tomcat/temp \
  && useradd -U -d /usr/local/tomcat -M -s /bin/false tomcat -u 5000 \
  && echo ${VERSION} > /var/docker_version_file

# Copy tomcat environment configuration file
# COPY target/tomcatconf/setenv.sh ${CATALINA_BASE}/bin/setenv.sh
# gh actions
COPY --from=artifacts tomcatconf/setenv.sh ${CATALINA_BASE}/bin/setenv.sh

# Install magnolia webapp in root context
# COPY target/magnolia-webapp-*.war ${CATALINA_BASE}/webapps/ROOT.war
# gh actions
COPY --from=artifacts magnolia-webapp-*.war ${CATALINA_BASE}/webapps/ROOT.war

#Override startup script
COPY usr/local/tomcat/bin/catalina.sh /usr/local/tomcat/bin/catalina.sh
COPY usr/local/tomcat/conf/server.xml /usr/local/tomcat/conf/server.xml
#Data volume
VOLUME ${MAGNOLIA_DATA_DIR}

RUN echo {\"version\":\"$VERSION\"} > /version

RUN --mount=type=secret,id=DOOSAN_ARTIFACTORY_TOKEN \
  curl -H "Authorization: Bearer $(cat /run/secrets/DOOSAN_ARTIFACTORY_TOKEN)" -L https://dbartifactory.jfrog.io/artifactory/all/info/magnolia/magnolia-module-backup/${BACKUP_MODULE_VERSION}/magnolia-module-backup-${BACKUP_MODULE_VERSION}-bundle.tar.gz > /tmp/backup.tar.gz && \
  tar xzf /tmp/backup.tar.gz -C / && \
  /bin/mv /magnolia-module-backup-${BACKUP_MODULE_VERSION} /magnolia-module-backup && \
  rm -f /tmp/backup.tar.gz && \
  chmod +x /magnolia-module-backup/bin/* && \
  echo "⬆️ Upgrading to commons-beanutils-1.11.0..." && \
  curl -L https://repo1.maven.org/maven2/commons-beanutils/commons-beanutils/1.11.0/commons-beanutils-1.11.0.jar \
    -o /magnolia-module-backup/lib/commons-beanutils-1.11.0.jar && \
  if [ -f "/magnolia-module-backup/lib/commons-beanutils-1.11.0.jar" ]; then \
    echo "✅ commons-beanutils-1.11.0 successfully downloaded and copied."; \
  else \
    echo "❌ Failed to download commons-beanutils-1.11.0.jar. Aborting." && exit 1; \
  fi && \
  echo "🔍 Scanning /magnolia-module-backup/lib for vulnerable commons-beanutils JARs..." && \
  jars=$(find /magnolia-module-backup/lib -type f -name "commons-beanutils-1.*.jar" 2>/dev/null | grep -v '1\.1[1-9]' || true) && \
  if [ -n "$jars" ]; then \
    echo "❌ Found vulnerable versions:" && echo "$jars" && echo "$jars" | xargs -r rm -f; \
  else \
    echo "✅ No vulnerable commons-beanutils jars found."; \
  fi && \
  echo "🔒 Verifying cleanup..." && \
  if find /magnolia-module-backup/lib -name "commons-beanutils-1.9.4.jar" | grep -q .; then \
    echo "❌ commons-beanutils-1.9.4.jar still present. Aborting." && exit 1; \
  else \
    echo "✅ commons-beanutils-1.9.4.jar successfully removed."; \
  fi

COPY backup/jackrabbit-db-postgres.xml /magnolia-module-backup/jackrabbit-db-postgres.xml
RUN  chown -R tomcat.tomcat /magnolia-module-backup /usr/local/tomcat/webapps

#Switch user to tomcat
USER tomcat
# Run tomcat container
CMD ["catalina.sh", "run"]

EXPOSE 8080 8009 1099
