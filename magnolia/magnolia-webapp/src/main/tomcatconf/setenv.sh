#!/bin/bash
#
# JVM settings
#
export CATALINA_OPTS="$CATALINA_OPTS \
	-server \
	-Dfile.encoding=UTF-8 \
	-XX:+UseG1GC \
	-XX:+PrintGC"
#
# Magnolia runtime parameters
#
export CATALINA_OPTS="$CATALINA_OPTS \
	-Denvironment=$ENVIRONMENT \
	-DinstanceType=$INSTANCE_TYPE \
	-Dmagnolia.feature.environment.id=$FEATURE_ID \
	-Dmagnolia.home=$MAGNOLIA_DATA_DIR \
	-Dmagnolia.docker.version.file=/var/docker_version_file \
	-Dmagnolia.resources.dir=$MAGNOLIA_RESOURCES_DIR \
	-Dmagnolia.develop=$MAGNOLIA_DEV_MODE \
	-Dmagnolia.allow.custom.rule=true \
	-DdbType=$DB_TYPE \
	-DdbUrl=$DB_URL \
	-DdbUser=$DB_USER \
	-DdbPassword=$DB_PASSWORD \
	-Djcr.cloudinary.api.secret=$JCR_CLOUDINARY_API_SECRET \
	-Djcr.auth.magnolia.author.okta.secret=$JCR_MAGNOLIA_AUTHOR_OKTA_SECRET \
	-Djcr.auth.magnolia.pmd.okta.secret=$JCR_MAGNOLIA_PMD_OKTA_SECRET \
	-Djcr.auth.magnolia.enigma.okta.secret=$JCR_MAGNOLIA_ENIGMA_OKTA_SECRET \
	-Djcr.auth.magnolia.kags.okta.secret=$JCR_MAGNOLIA_KAGS_OKTA_SECRET \
	-Djcr.auth.magnolia.kags.key.account.okta.secret=$JCR_MAGNOLIA_KAGS_KEY_ACCOUNT_OKTA_SECRET \
	-Djcr.auth.magnolia.kags.smartequip.secret=$JCR_MAGNOLIA_KAGS_SMARTEQUIP_SECRET \
	-Djcr.auth.magnolia.pimpp.okta.secret=$JCR_MAGNOLIA_PIMPP_OCI_SECRET \
	-Djcr.auth.magnolia.license=$JCR_MAGNOLIA_LICENSE \
	-Djcr.google.translation.api.key=$JCR_GOOGLE_TRANSLATION_API_KEY \
	-Djcr.auth.magnolia.gpim.secret=$JCR_MAGNOLIA_GPIM_SECRET"
