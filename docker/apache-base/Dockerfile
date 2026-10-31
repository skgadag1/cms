FROM public.ecr.aws/amazonlinux/amazonlinux:2

ENV APACHE_HOME='/etc/httpd' \
    APACHE_VERSION='2.4.49' \
    APR_VERSION='1.7.4' \
    APR_UTIL_VERSION='1.6.3' \
    OPENSSL_VERSION='1_1_1g' \
    PCRE_VERSION='8.44' \
    NGHTTP2_VERSION='1.42.0'

    # Simple startup script to avoid some issues observed with container restart
    COPY run-httpd.sh /run-httpd.sh

    RUN yum update -y \
      && yum group -y install 'Development Tools' \
      && yum install -y jansson-devel expat-devel perl-core pcre pcre-devel zlib-devel procps python-devel libmcrypt openssl11-devel which libnghttp2-devel openldap-devel \
      && curl -L https://dlcdn.apache.org/apr/apr-${APR_VERSION}.tar.gz -o /tmp/apr-${APR_VERSION}.tar.gz \
      && curl -L https://dlcdn.apache.org/apr/apr-util-${APR_UTIL_VERSION}.tar.gz -o /tmp/apr-util-${APR_UTIL_VERSION}.tar.gz \
      && curl -L http://archive.apache.org/dist/httpd/httpd-${APACHE_VERSION}.tar.gz -o /tmp/httpd-${APACHE_VERSION}.tar.gz \
      && curl -L https://github.com/nghttp2/nghttp2/releases/download/v${NGHTTP2_VERSION}/nghttp2-${NGHTTP2_VERSION}.tar.gz -o /tmp/nghttp2-${NGHTTP2_VERSION}.tar.gz \
      && tar -xvf /tmp/nghttp2-${NGHTTP2_VERSION}.tar.gz -C /opt \
      && cd /opt/nghttp2-${NGHTTP2_VERSION} && ./configure --prefix=/usr/local/nghttp2 && make clean && make && make install \
      && tar -xvf /tmp/apr-${APR_VERSION}.tar.gz -C /opt/ \
      && cd /opt/apr-${APR_VERSION} && touch libtoolT && ./configure --prefix=/usr/local/apr && make && make install \
      && tar -xvf /tmp/apr-util-${APR_UTIL_VERSION}.tar.gz -C /opt/ \
      && cd /opt/apr-util-${APR_UTIL_VERSION} && ./configure --prefix=/usr/local/apr-util --with-apr=/usr/local/apr \
              --with-crypto --with-ldap=ldap --with-openssl --with-mysql --with-apr=/usr/local/apr --enable-so \
      && tar -xvf /tmp/httpd-${APACHE_VERSION}.tar.gz -C /opt/ && cd /opt/httpd-${APACHE_VERSION} \
      && cp -r /opt/apr-${APR_VERSION} /opt/httpd-${APACHE_VERSION}/srclib/apr \
      && cp -r /opt/apr-util-${APR_UTIL_VERSION} /opt/httpd-${APACHE_VERSION}/srclib/apr-util \
      && groupadd apache -g 1000 && useradd -u 1000 apache -g apache --no-create-home --shell /sbin/nologin \
      && ./buildconf \
      && ./configure --enable-layout=RedHat --prefix=/etc/httpd --localstatedir=/var --exec-prefix=/usr/ --sbindir=/usr/sbin --mandir=/usr/share/man \
                --enable-suexec --enable-so --enable-ssl --enable-cgi --enable-http2 \
                --enable-proxy-http2 --enable-pie --enable-ldap --enable-authnz-ldap \
                --enable-rewrite --enable-modules=all --enable-proxy-ajp --enable-session \
                --enable-mpms-shared=all --enable-mods-shared=all --enable-deflate --enable-log-forensic \
                --enable-nonportable-atmoics-yes --with-crypto --with-gdbm --with-included-apr --with-ldap --enable-authn-socache  \
                --with-suexec --with-mpm=prefork --with-zlib --with-ssl=/usr/local/openssl --with-nghttp2=/usr/local/nghttp2 \
                --with-suexec-caller=apache --with-suexec-docroot=/home --with-suexec-logfile=/var/log/httpd/suexec.log \
                --with-suexec-bin=/usr/sbin/suexec --with-suexec-uidmin=1000 --with-suexec-gidmin=1000 \
      && make && make install \
      && yum group remove -y 'Development Tools' \
      && yum remove -y openldap-devel which pcre-devel zlib-devel libmcrypt perl perl-core jansson-devel \
      && yum clean all \
      && echo "Apache HTTPD" >> /var/www/html/index.html \
      && ln -sf /proc/self/fd/1 /var/log/httpd/access_log \
      && ln -sf /proc/self/fd/2 /var/log/httpd/error_log \
      && ln -sf /var/log/httpd /etc/httpd/logs \
      && ln -sf /etc/httpd/lib/apache /etc/httpd/modules \
      && chown -R apache:apache /etc/httpd \
      && rm -f /etc/httpd/conf/extra/*.conf \
      && rm -rf /var/cache/yum /opt/* /tmp/*

STOPSIGNAL SIGTERM

EXPOSE 80

CMD ["/run-httpd.sh"]
