# Spartan Golden Gym — Documentación Técnica

## Visión General

Spartan Golden Gym es una plataforma integral de fitness basada en microservicios (Java 8 + Spring Boot 2.7.x),
desplegada en AWS EKS con comunicación asíncrona vía Kafka (Amazon MSK).

## Especificaciones de API (OpenAPI 3.0)

Cada microservicio cuenta con su especificación OpenAPI 3.0 completa, incluyendo endpoints,
esquemas de request/response, seguridad JWT y códigos de error estándar.

| # | Microservicio | Especificación | Descripción |
|---|---|---|---|
| 1 | Servicio_Usuarios | [servicio-usuarios.yaml](api/servicio-usuarios.yaml) | Registro, autenticación, perfiles, MFA, GDPR |
| 2 | Servicio_Gimnasio | [servicio-gimnasio.yaml](api/servicio-gimnasio.yaml) | Gimnasios, geolocalización, check-in, equipamiento |
| 3 | Servicio_Entrenamiento | [servicio-entrenamiento.yaml](api/servicio-entrenamiento.yaml) | Planes, rutinas, ejercicios, asignación |
| 4 | Servicio_Seguimiento | [servicio-seguimiento.yaml](api/servicio-seguimiento.yaml) | Entrenamientos en tiempo real, wearables, biométricos |
| 5 | Servicio_Nutricion | [servicio-nutricion.yaml](api/servicio-nutricion.yaml) | Planes nutricionales, alimentos, macros, recetas |
| 6 | Servicio_IA_Coach | [servicio-ia-coach.yaml](api/servicio-ia-coach.yaml) | Planes IA, recomendaciones, sobreentrenamiento |
| 7 | Servicio_Social | [servicio-social.yaml](api/servicio-social.yaml) | Desafíos, logros, rankings, grupos |
| 8 | Servicio_Pagos | [servicio-pagos.yaml](api/servicio-pagos.yaml) | Suscripciones, pagos, reembolsos, donaciones |
| 9 | Servicio_Notificaciones | [servicio-notificaciones.yaml](api/servicio-notificaciones.yaml) | Push, email, SMS, preferencias |
| 10 | Servicio_Reservas | [servicio-reservas.yaml](api/servicio-reservas.yaml) | Clases grupales, lista de espera, disponibilidad |
| 11 | Servicio_Mensajeria | [servicio-mensajeria.yaml](api/servicio-mensajeria.yaml) | Chat directo, grupal, WebSocket |
| 12 | Servicio_Calendario | [servicio-calendario.yaml](api/servicio-calendario.yaml) | Eventos, sincronización, conflictos |
| 13 | Servicio_Analiticas | [servicio-analiticas.yaml](api/servicio-analiticas.yaml) | Dashboard, reportes, métricas |

Esquemas compartidos: [schemas/common.yaml](api/schemas/common.yaml)

## Documentos de Arquitectura

| Documento | Descripción |
|---|---|
| [05-arquitectura-microservicios.md](architecture/05-arquitectura-microservicios.md) | Arquitectura completa del backend (14 microservicios, bases de datos, Kafka, seguridad) |
| [06-arquitectura-frontend-web.md](architecture/06-arquitectura-frontend-web.md) | Arquitectura del frontend web (React 18, Vite, MUI, Redux Toolkit) |
| [07-arquitectura-app-movil.md](architecture/07-arquitectura-app-movil.md) | Arquitectura de la app móvil (React Native, offline, wearables, accesibilidad) |

### Diagramas de Secuencia — Flujos Principales

| Flujo | Documento | Servicios Clave |
|---|---|---|
| Registro de usuario | [01-flujo-registro-usuario.md](architecture/01-flujo-registro-usuario.md) | Usuarios, Redis, SES, IA Coach |
| Entrenamiento | [02-flujo-entrenamiento.md](architecture/02-flujo-entrenamiento.md) | Seguimiento, DynamoDB, Timestream, IA Coach |
| Pago y suscripción | [03-flujo-pago-suscripcion.md](architecture/03-flujo-pago-suscripcion.md) | Pagos, Stripe/Adyen, PayPal |
| Recomendación IA | [04-flujo-recomendacion-ia.md](architecture/04-flujo-recomendacion-ia.md) | IA Coach, SageMaker, Neptune |

## Guías

| Documento | Audiencia | Descripción |
|---|---|---|
| [guia-desarrollo-local.md](guia-desarrollo-local.md) | Desarrolladores | Configuración del entorno local, ejecución de tests, convenciones |
| [guia-despliegue.md](guia-despliegue.md) | DevOps / Desarrolladores | Despliegue de infraestructura (Terraform), backend (EKS), frontend (S3/CloudFront), móvil (stores) |
| [guia-usuario-final.md](guia-usuario-final.md) | Usuarios finales | Funcionalidades de la plataforma, primeros pasos, roles, privacidad |

## Seguridad

Todos los endpoints (excepto registro y login) requieren autenticación JWT Bearer.
Los códigos de error estándar incluyen: 400, 401, 403, 404, 409, 429, 500, 503.

Rate limiting:
- 1000 req/min por usuario autenticado
- 100 req/min por IP no autenticada

## Stack Tecnológico

- Backend: Java 8 + Spring Boot 2.7.x
- API Gateway: Spring Cloud Gateway
- Bases de datos: Aurora PostgreSQL, DynamoDB, Timestream, Neptune
- Cache: ElastiCache Redis
- Streaming: Amazon MSK (Kafka)
- ML: Amazon SageMaker
- Infraestructura: Terraform + AWS EKS
