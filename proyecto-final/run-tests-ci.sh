#!/bin/bash

# Script optimizado para CI/CD
set -e

echo "🧪 Iniciando tests en entorno CI..."

# Variables de entorno para CI
export COMPOSE_PROJECT_NAME="ci-tests-${GITHUB_RUN_ID:-$(date +%s)}"
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

# Función para limpiar recursos
cleanup() {
    echo "🧹 Limpiando recursos..."
    docker-compose -f docker-compose.test.yml down -v --remove-orphans || true
    docker system prune -f || true
}

# Registrar función de limpieza para cuando el script termine
trap cleanup EXIT

# Crear directorio para resultados
mkdir -p test-results

echo "🔨 Construyendo imágenes..."
docker-compose -f docker-compose.test.yml build --parallel

echo "🚀 Iniciando servicios base..."
docker-compose -f docker-compose.test.yml up -d postgres keycloak

echo "⏳ Esperando a que los servicios estén listos..."
docker-compose -f docker-compose.test.yml exec -T postgres pg_isready -U dev -d development
docker-compose -f docker-compose.test.yml exec -T keycloak sh -c 'until curl -f http://localhost:8080/health/ready; do sleep 5; done'

echo "🧪 Ejecutando tests unitarios..."
docker-compose -f docker-compose.test.yml run --rm quarkus-unit-tests

echo "🚀 Iniciando aplicación para tests de integración..."
docker-compose -f docker-compose.test.yml up -d quarkus-app frontend-app

echo "⏳ Esperando a que las aplicaciones estén listas..."
timeout 300 bash -c 'until curl -f http://localhost:8080/q/health/ready; do sleep 5; done'
timeout 300 bash -c 'until curl -f http://localhost:5173; do sleep 5; done'

echo "🧪 Ejecutando tests de aceptación..."
docker-compose -f docker-compose.test.yml run --rm quarkus-acceptance-tests

echo "🧪 Ejecutando tests E2E..."
docker-compose -f docker-compose.test.yml run --rm playwright-tests

echo "✅ Todos los tests completados exitosamente"

# Copiar reportes para artifacts
if [ -n "$GITHUB_WORKSPACE" ]; then
    echo "📊 Copiando reportes para GitHub Actions..."
    docker cp $(docker-compose -f docker-compose.test.yml ps -q quarkus-unit-tests):/app/target/surefire-reports ./test-results/ 2>/dev/null || true
    docker cp $(docker-compose -f docker-compose.test.yml ps -q playwright-tests):/app/test-results ./frontend/ 2>/dev/null || true
    docker cp $(docker-compose -f docker-compose.test.yml ps -q playwright-tests):/app/playwright-report ./frontend/ 2>/dev/null || true
fi
