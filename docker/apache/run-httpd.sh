#!/bin/bash

# Make sure we're not confused by old, incompletely-shutdown httpd
# context after restarting the container.  httpd won't start correctly
# if it thinks it is already running.
rm -rf /run/http*

mkdir -p /srv/www/magnolia-author/current /srv/www/magnolia-publish/current /srv/www/magnolia-public/current /srv/www/magnolia-publisher
chown -R apache.apache /srv/www

exec /usr/sbin/apachectl -D FOREGROUND
