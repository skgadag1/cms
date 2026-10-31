FROM alpine:3.19

ARG VERSION

ENV version="${VERSION}"

RUN apk update \
    && apk add --no-cache --update curl nfs-utils rsync bash tar postgresql15-client \
    && rm -rf /var/cache/apk/* \
    && echo "${version}" > /version.txt

COPY scripts /root/scripts
RUN chmod +x /root/scripts/*
CMD [ "/bin/bash" ]
