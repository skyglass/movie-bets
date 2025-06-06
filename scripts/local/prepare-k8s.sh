#!/bin/bash

if [ -f ./env/.env.local ]; then
  set -o allexport
  source ./env/.env.local
  set +o allexport
else
  echo ".env.local file not found"
  exit 1
fi

set -u # or set -o nounset
: "$BASE_URL"
: "$NEXT_PUBLIC_BASE_URL"
: "$KEYCLOAK_BASE_URL"
: "$OMDB_API_KEY"

kubectl delete configmap base-url-config
kubectl delete secret moviebets-secrets
kubectl create configmap base-url-config --from-literal=BASE_URL=$BASE_URL --from-literal=NEXT_PUBLIC_BASE_URL=$NEXT_PUBLIC_BASE_URL --from-literal=KEYCLOAK_BASE_URL=$KEYCLOAK_BASE_URL
kubectl create secret generic moviebets-secrets --from-literal=OMDB_API_KEY=$OMDB_API_KEY

kubectl apply -f 'https://strimzi.io/install/latest?namespace=default'

helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
kubectl apply -f 'https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.4.0/deploy/static/provider/cloud/deploy.yaml'

# Temporary directory for the processed manifests
GENERATED_DIR=./k8s/generated
rm -rf $GENERATED_DIR
mkdir $GENERATED_DIR

# Process each manifest
for file in ./k8s/* ./k8s/local/*; do
  if [ -d "$file" ]; then
    continue
  fi
  envsubst < "$file" > "$GENERATED_DIR/$(basename "$file")"
done

envsubst < "./skaffold-template.yaml" > "./skaffold.yaml"