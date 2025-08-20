#!/bin/bash

# Script para ejecutar todos los tests
set -e

echo "🧪 Iniciando suite completa de tests..."

# Crear directorio para resultados
mkdir -p test-results

# Limpiar contenedores anteriores
echo "🧹 Limpiando contenedores anteriores..."
docker-compose -f Testing.yml down -v --remove-orphans

# Construir imágenes
echo "🔨 Construyendo imágenes de testing..."
docker-compose -f Testing.yml build

# Ejecutar tests
echo "🚀 Ejecutando tests..."

# Ejecutar en orden: unitarios -> aceptación -> e2e
docker-compose -f Testing.yml up \
  postgres keycloak quarkus-app frontend-app \
  quarkus-unit-tests quarkus-acceptance-tests playwright-tests \
  test-runner \
  --abort-on-container-exit \
  --exit-code-from test-runner

# Obtener códigos de salida
UNIT_EXIT_CODE=$(docker-compose -f Testing.yml ps -q quarkus-unit-tests | xargs docker inspect -f '{{.State.ExitCode}}')
ACCEPTANCE_EXIT_CODE=$(docker-compose -f Testing.yml ps -q quarkus-acceptance-tests | xargs docker inspect -f '{{.State.ExitCode}}')
PLAYWRIGHT_EXIT_CODE=$(docker-compose -f Testing.yml ps -q playwright-tests | xargs docker inspect -f '{{.State.ExitCode}}')

echo "📊 Resultados de los tests:"
echo "   - Tests unitarios: $([ $UNIT_EXIT_CODE -eq 0 ] && echo '✅ PASSED' || echo '❌ FAILED')"
echo "   - Tests de aceptación: $([ $ACCEPTANCE_EXIT_CODE -eq 0 ] && echo '✅ PASSED' || echo '❌ FAILED')"
echo "   - Tests E2E: $([ $PLAYWRIGHT_EXIT_CODE -eq 0 ] && echo '✅ PASSED' || echo '❌ FAILED')"

# Limpiar
echo "🧹 Limpiando recursos..."
docker-compose -f Testing.yml down -v

# Mostrar ubicación de reportes
echo "📁 Reportes disponibles en:"
echo "   - test-results/surefire-reports/ (Quarkus)"
echo "   - frontend/playwright-report/ (E2E)"

# Salir con error si algún test falló
if [ $UNIT_EXIT_CODE -ne 0 ] || [ $ACCEPTANCE_EXIT_CODE -ne 0 ] || [ $PLAYWRIGHT_EXIT_CODE -ne 0 ]; then
  echo "❌ Algunos tests fallaron"
  exit 1
else
  echo "✅ Todos los tests pasaron exitosamente"
  exit 0
fi
