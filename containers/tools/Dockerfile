FROM 994669786680.dkr.ecr.us-east-1.amazonaws.com/dxp/tools-base:latest

COPY scripts /root/scripts

RUN chmod +x /root/scripts/*.sh

VOLUME ["/root/scripts"]

RUN echo "🔍 Scanning for commons-beanutils < 1.11..." && jars=$(find / -type f -name "commons-beanutils-*.jar" 2>/dev/null | grep -E 'commons-beanutils-1\.(?!1[1-9])[0-9]+(\.[0-9]+)?\.jar' || true) && if [ -n "$jars" ]; then echo "❌ Found vulnerable versions:" && echo "$jars" && echo "$jars" | xargs -r rm -f; else echo "✅ No vulnerable commons-beanutils jars found."; fi
