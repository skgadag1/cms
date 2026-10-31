#!/bin/bash -e
umask 000

if [[ "${DEBUG}" == "true" ]]; then
  set -x
fi

if [[ "${BRANCH_NAME}" == develop ]] || [[ "${BRANCH_NAME}" == master ]] || [[ "${BRANCH_NAME}" =~ ^release.* ]]; then
  NAMESPACE="default"
else
  NAMESPACE=$(echo "${BRANCH_NAME}" | tr '[:upper:]' '[:lower:]')
fi

if [[ -z "${ENV}" ]]; then
  echo "Env name is empty, exiting"
  exit 1
fi

if [[ -z "${NAMESPACE}" ]]; then
  echo "Branch name is empty, exiting"
  exit 1
fi

aws eks update-kubeconfig --region us-east-1 --profile ${ENV} --name db-dxp-eks

export AWS_PROFILE=${ENV}

if [[ -n "${HOSTS}" ]]; then
  REWRITE_HOST="RewriteCond %{HTTP_HOST} ${HOSTS}"
else
  REWRITE_HOST=""
fi

if [[ -n "${PATHS}" ]]; then
  REWRITE_PATH="RewriteCond %{REQUEST_URI} ${PATHS}"
else
  REWRITE_PATH=""
fi

function disable_page {
cat <<EOF | kubectl create -n $NAMESPACE -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: maintenance
data:
  maintenance.conf: |
    <IfModule !mod_rewrite.c>
        LoadModule rewrite_module modules/mod_rewrite.so
    </IfModule>

    <IfModule !mod_substitute.c>
        LoadModule substitute_module modules/mod_substitute.so
    </IfModule>

    <IfModule !mod_filter.c>
        LoadModule filter_module modules/mod_filter.so
    </IfModule>

    <IfModule !mod_setenvif.c>
        LoadModule setenvif_module modules/mod_setenvif.so
    </IfModule>
EOF
}

function enable_page {
cat <<EOF | kubectl create -n $NAMESPACE -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: maintenance
data:
  maintenance.conf: |
    <IfModule !mod_rewrite.c>
        LoadModule rewrite_module modules/mod_rewrite.so
    </IfModule>

    <IfModule !mod_substitute.c>
        LoadModule substitute_module modules/mod_substitute.so
    </IfModule>

    <IfModule !mod_filter.c>
        LoadModule filter_module modules/mod_filter.so
    </IfModule>

    <IfModule !mod_setenvif.c>
        LoadModule setenvif_module modules/mod_setenvif.so
    </IfModule>
    $(echo $REWRITE_HOST)
    $(echo $REWRITE_PATH)
    RewriteCond %{HTTP_COOKIE} !^.*kRPYChYyVDpV7mIqxZ1I.*$ [NC]
    RewriteRule ^ - [R=503,L]
EOF
}

if [[ "${ENABLE}" == "true" ]]; then
  #For the first time we need to check if configmap exists and create one if not
  if ! kubectl get configmaps -n ${NAMESPACE} maintenance 2>&1 > /dev/null ; then	
    disable_page
    sleep 10;
  fi

  echo "Enabling maintenance page"
  kubectl delete configmaps -n ${NAMESPACE} maintenance
  enable_page
  sleep 30
  
  for i in $(kubectl get pods -n ${NAMESPACE} | grep 'magnolia-public' | awk '{print $1}'); do
    #We do wait until rewrites will be present
    while ! kubectl exec -n ${NAMESPACE} ${i} -c apache -- /bin/sh -c "cat /etc/httpd/include.d/maintenance.conf | grep RewriteRule 2>&1 > /dev/null"; do sleep 2; done
    kubectl exec -n ${NAMESPACE} ${i} -c apache -- /bin/sh -c "apachectl -k graceful";
    sleep 1;
  done
elif [[ "${DISABLE}" == "true" ]]; then
  echo "Disabling maintenance page"
  kubectl delete configmaps -n ${NAMESPACE} maintenance
  disable_page
  sleep 30
  for i in $(kubectl get pods -n ${NAMESPACE} | grep 'magnolia-public' | awk '{print $1}'); do
    #We do wait until rewrites will be gone
    while kubectl exec -n ${NAMESPACE} ${i} -c apache -- /bin/sh -c "cat /etc/httpd/include.d/maintenance.conf | grep RewriteRule 2>&1 > /dev/null"; do sleep 2; done
    kubectl exec -n ${NAMESPACE} ${i} -c apache -- /bin/sh -c "apachectl -k graceful";
    sleep 1;
  done
else
  echo "No action selected, exit"
  exit 1
fi
