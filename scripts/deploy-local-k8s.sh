#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 || ! $1 =~ ^[A-Za-z0-9_][A-Za-z0-9_.-]{0,127}$ ]]; then
  printf 'Usage: bash scripts/deploy-local-k8s.sh <published-image-tag>\n' >&2
  exit 2
fi

if ! command -v kubectl >/dev/null 2>&1; then
  printf 'kubectl is required. Install it and enable Kubernetes in Docker Desktop.\n' >&2
  exit 1
fi

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
secret_file="${APP_SECRETS_FILE:-${repo_root}/application-secrets.yml}"
deployment_file="${repo_root}/k8s/deployment.yaml"

if [[ ! -s "$secret_file" ]]; then
  printf 'Missing or empty %s. Add local DB credentials and required provider keys first.\n' "$secret_file" >&2
  exit 1
fi

current_context="$(kubectl config current-context)"
if [[ "$current_context" != "docker-desktop" ]]; then
  printf 'Refusing deployment: current kubectl context is %s, expected docker-desktop.\n' "$current_context" >&2
  exit 1
fi

if ! kubectl get secret ghcr-secret --namespace dev >/dev/null 2>&1; then
  printf 'Missing namespace-scoped GHCR pull secret ghcr-secret in namespace dev.\n' >&2
  printf 'Create it once using the README Docker Desktop Kubernetes instructions.\n' >&2
  exit 1
fi

kubectl apply -f "${repo_root}/k8s/00-namespace.yaml"
kubectl create secret generic news-scheduler-secrets \
  --namespace dev \
  --from-file="application-secrets.yml=${secret_file}" \
  --dry-run=client \
  --output yaml | kubectl apply -f -

kubectl apply \
  -f "${repo_root}/k8s/service.yaml" \
  -f "${repo_root}/k8s/ingress.yaml"

image="ghcr.io/omkarchorghe16/news-scheduler-svc:$1"
sed "s|ghcr.io/omkarchorghe16/news-scheduler-svc:REPLACE_WITH_GITHUB_SHA|${image}|" "$deployment_file" \
  | kubectl apply -f -

kubectl rollout restart --namespace dev deployment/news-scheduler-svc
kubectl rollout status --namespace dev deployment/news-scheduler-svc
