# Guía de Desarrollo Local — Spartan Golden Gym

## Prerrequisitos

| Herramienta | Versión | Instalación |
|---|---|---|
| JDK 8 | 1.8.x | [Amazon Corretto 8](https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/downloads-list.html) |
| Maven | 3.8+ | [maven.apache.org](https://maven.apache.org/download.cgi) |
| Node.js | 18+ | [nodejs.org](https://nodejs.org/) |
| Docker | 24+ | [docker.com](https://www.docker.com/get-started/) |
| Git | 2.x | [git-scm.com](https://git-scm.com/) |

## 1. Clonar el Repositorio

```bash
git clone https://github.com/tu-org/spartan-golden-gym.git
cd spartan-golden-gym
```

## 2. Backend — Microservicios

### 2.1 Compilar todo el proyecto

```bash
mvn clean install -DskipTests
```

### 2.2 Ejecutar tests

```bash
# Todos los módulos
mvn test

# Un módulo específico
mvn test -pl servicio-usuarios

# Solo tests de propiedad (jqwik)
mvn test -pl common-lib -Dtest="*Properties"
```

### 2.3 Ejecutar un microservicio individual

```bash
# Ejemplo: servicio de usuarios
mvn spring-boot:run -pl servicio-usuarios -Dspring-boot.run.profiles=desarrollo

# API Gateway (puerto 8080 por defecto)
mvn spring-boot:run -pl api-gateway -Dspring-boot.run.profiles=desarrollo
```

### 2.4 Ejecutar con Docker

```bash
# Construir imagen de un servicio
docker build --build-arg MODULE=servicio-usuarios -t spartan-gym/servicio-usuarios .

# Ejecutar
docker run -p 8081:8080 -e SPRING_PROFILES_ACTIVE=desarrollo spartan-gym/servicio-usuarios
```

## 3. Frontend Web

```bash
cd spartan-gym-frontend
npm install

# Servidor de desarrollo (http://localhost:5173)
npm run dev

# Tests
npm test

# Lint
npm run lint

# Build de producción
npm run build

# Preview del build
npm run preview
```

El proxy de desarrollo redirige `/api` a `http://localhost:8080` (API Gateway).

## 4. App Móvil

```bash
cd spartan-gym-mobile
npm install

# Tests
npm test

# Type checking
npm run typecheck

# Lint
npm run lint
```

### iOS (requiere macOS)

```bash
cd ios && pod install && cd ..
npm run ios
```

### Android

```bash
# Asegurar que un emulador está corriendo o un dispositivo conectado
npm run android
```

## 5. Estructura de Tests

### Backend (Java)

| Tipo | Convención | Framework | Ubicación |
|---|---|---|---|
| Unitarios | `*Test.java` | JUnit 5 + Mockito | `src/test/java/` |
| Propiedad | `*Properties.java` | jqwik 1.7.4 | `src/test/java/` |

### Frontend Web

| Tipo | Convención | Framework | Ubicación |
|---|---|---|---|
| Unitarios | `*.test.ts(x)` | Vitest + Testing Library | junto al archivo fuente |
| Accesibilidad | `axeHelper.ts` | axe-core | `src/test/` |
| Propiedad | `*.test.ts` con fast-check | fast-check | junto al archivo fuente |

### App Móvil

| Tipo | Convención | Framework | Ubicación |
|---|---|---|---|
| Unitarios | `*.test.ts(x)` | Jest + ts-jest | junto al archivo fuente |
| Propiedad | `*.test.ts` con fast-check | fast-check | junto al archivo fuente |

## 6. Convenciones del Proyecto

- Idiomas de código: inglés para código, español para documentación y mensajes de usuario
- Commits: convencional commits (`feat:`, `fix:`, `docs:`, `test:`)
- Branches: `main` (producción), `develop` (desarrollo), `feature/*`, `bugfix/*`
- Formato de error API: `{ error, message, timestamp, traceId }`
- Códigos HTTP estándar: 400, 401, 403, 404, 409, 429, 500, 503
