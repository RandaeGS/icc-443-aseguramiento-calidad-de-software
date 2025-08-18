## Tecnologías

### Backend
- **Quarkus 3.23.3** - Framework Java supersónico
- **Java 21** - Lenguaje de programación
- **PostgreSQL 16** - Base de datos
- **Hibernate Panache** - ORM simplificado
- **Flyway** - Migración de base de datos
- **Hibernate Envers** - Auditoría automática

### Frontend
- **Vue.js 3** - Framework progresivo
- **PrimeVue 4** - Librería de componentes UI
- **Tailwind CSS** - Framework de estilos
- **Axios** - Cliente HTTP

### Seguridad
- **Keycloak 26.3** - Servidor de identidad
- **OAuth2/OIDC** - Autenticación y autorización
- **JWT** - Tokens de sesión

### DevOps
- **Docker & Docker Compose** - Containerización
- **Maven** - Gestión de dependencias

## Funcionalidades

- **Gestión de Productos**: CRUD completo con validaciones
- **Control de Stock**: Actualizaciones y alertas de stock mínimo
- **Historial de Movimientos**: Trazabilidad completa con auditoría
- **Roles y Permisos**: Administrador, Empleado, Invitado
- **API RESTful**: Endpoints documentados
- **Interfaz Responsiva**: Dashboard intuitivo

## Configuración Rápida

### Prerrequisitos
- Docker y Docker Compose
- Java 21 (para desarrollo)
- Node.js 18+ (para frontend)

### 1. Levantar Infraestructura
```bash
# Iniciar PostgreSQL y Keycloak
docker-compose -f Development.yml up -d
```

### 2. Ejecutar Backend
```bash
# En el directorio del backend
./mvnw quarkus:dev
```
Backend disponible en: http://localhost:8080

### 3. Ejecutar Frontend
```bash
# En el directorio del frontend
npm install
npm run dev
```
Frontend disponible en: http://localhost:5173

### 4. Acceder al Sistema
- **Aplicación**: http://localhost:5173
- **Keycloak Admin**: http://localhost:7080 (admin/admin)

## Modelo de Datos

### Producto
```java
public class Producto {
    public String name;           // Nombre único del producto
    public String description;    // Descripción
    public ProductCategory category; // Electronics, Fitness, Clothing, Accessories
    public Double price;          // Precio de venta
    public Double cost;           // Costo
    public Double profit;         // Ganancia
    public Long quantity;         // Cantidad en stock
    public Long minimumStock;     // Stock mínimo
    public Boolean isActive;      // Estado activo/inactivo
}
```

### StockMovement
```java
public class StockMovement {
    public Producto producto;     // Producto relacionado
    public String username;       // Usuario que realizó el movimiento
    public LocalDateTime date;    // Fecha del movimiento
    public Long actualQuantity;   // Cantidad actual después del movimiento
    public Long quantityChange;   // Cambio en la cantidad (+/-)
}
```

## Autenticación

### Roles del Sistema
| Rol | Productos | Stock | API |
|-----|-----------|-------|-----|
| **Administrador** | CRUD | CRUD | ✅ |
| **Empleado** | CRU | CRUD | ❌ |
| **Invitado** | R | ❌ | ❌ |

### Configuración Keycloak
1. Crear realm: `project`
2. Crear cliente: `quarkus`
3. Configurar roles: `administrador`, `empleado`, `invitado`
4. Crear usuarios y asignar roles

## 🔧 API Endpoints

### Productos
```
GET    /api/productos              # Listar productos (paginado)
POST   /api/productos              # Crear producto
GET    /api/productos/{id}         # Obtener producto
PUT    /api/productos/{id}         # Actualizar producto
DELETE /api/productos/{id}         # Eliminar producto
```

### Stock
```
PUT    /api/productos/{id}/update-quantity   # Actualizar stock
GET    /api/productos/{id}/history           # Historial de movimientos
```

## Testing

### Backend
```bash
./mvnw test
```

### Frontend
```bash
npx playwright test
```

## Características de Calidad

- ✅ **Auditoría**: Todos los cambios son registrados automáticamente
- ✅ **Validación**: Validaciones tanto en frontend como backend
- ✅ **Seguridad**: Autenticación OAuth2 y autorización basada en roles
- ✅ **Performance**: Consultas optimizadas y paginación
- ✅ **Escalabilidad**: Arquitectura de microservicios containerizada
- ✅ **Mantenibilidad**: Código limpio y bien documentado

## 📚 Documentación Adicional

- [Quarkus Guides](https://quarkus.io/guides/)
- [Vue.js Documentation](https://vuejs.org/)
- [PrimeVue Components](https://primevue.org/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
