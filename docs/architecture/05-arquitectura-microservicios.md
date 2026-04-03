# Arquitectura del Backend — Microservicios Spartan Golden Gym

## Resumen

El backend de Spartan Golden Gym es un sistema de 14 microservicios independientes construidos con Java 8 y Spring Boot 2.7.x, orquestados en Amazon EKS y comunicados mediante Amazon MSK (Kafka) para eventos asíncronos y REST para comunicación síncrona.

## Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENTES                                     │
│   ┌──────────────────┐          ┌──────────────────┐                │
│   │  App Web          │          │  App Móvil        │                │
│   │  React 18 + TS    │          │  React Native     │                │
│   │  Vite + Tailwind  │          │  Android / iOS    │                │
│   └────────┬─────────┘          └────────┬─────────┘                │
└────────────┼────────────────────────────┼───────────────────────────┘
             │           HTTPS            │
             ▼                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    API GATEWAY (Spring Cloud Gateway)                 │
│  • Enrutamiento por prefijo    • JWT Validation                      │
│  • Rate Limiting (Redis)       • Circuit Breaker (Resilience4j)      │
│  • AWS X-Ray Tracing           • 1000 req/min auth, 100 req/min IP  │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        ▼                      ▼                      ▼
┌──────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  Servicio    │  │  Servicio        │  │  Servicio        │
│  Usuarios    │  │  Gimnasio        │  │  Entrenamiento   │
│  (Auth,MFA)  │  │  (Geo,QR,Ocup)  │  │  (Planes,Rutinas)│
└──────┬───────┘  └──────┬───────────┘  └──────┬───────────┘
       │                 │                      │
┌──────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  Servicio    │  │  Servicio        │  │  Servicio        │
│  Seguimiento │  │  Nutrición       │  │  IA Coach        │
│  (Workouts)  │  │  (Macros,Foods)  │  │  (ML,SageMaker)  │
└──────┬───────┘  └──────┬───────────┘  └──────┬───────────┘
       │                 │                      │
┌──────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  Servicio    │  │  Servicio        │  │  Servicio        │
│  Social      │  │  Pagos           │  │  Analíticas      │
│  (Rankings)  │  │  (Stripe,PayPal) │  │  (Redshift)      │
└──────┬───────┘  └──────┬───────────┘  └──────┬───────────┘
       │                 │                      │
┌──────────────┐  ┌──────────────────┐  ┌──────────────────┐
│  Servicio    │  │  Servicio        │  │  Servicio        │
│  Notificac.  │  │  Reservas        │  │  Mensajería      │
│  (Push,SMS)  │  │  (Clases,Wait)   │  │  (Chat,WS)       │
└──────┬───────┘  └──────┬───────────┘  └──────┬───────────┘
       │                 │                      │
       └─────────────────┼──────────────────────┘
                         ▼
              ┌──────────────────┐
              │  Servicio        │
              │  Calendario      │
              │  (Sync,Eventos)  │
              └──────────────────┘
```

## Stack Tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 8 |
| Framework | Spring Boot 2.7.18 |
| API Gateway | Spring Cloud Gateway 2021.0.8 |
| Resiliencia | Resilience4j 1.7.0 |
| Streaming | Amazon MSK (Kafka) + Spring Kafka |
| BD Relacional | Amazon Aurora PostgreSQL |
| BD NoSQL | Amazon DynamoDB |
| Series Temporales | Amazon Timestream |
| Grafos | Amazon Neptune |
| Cache | Amazon ElastiCache Redis |
| ML | Amazon SageMaker |
| Analíticas | Amazon Redshift + QuickSight |
| Almacenamiento | Amazon S3 + CloudFront |
| Contenedores | Amazon EKS (Kubernetes) |
| IaC | Terraform >= 1.5.0 |
| CI/CD | GitHub Actions |
| Testing | JUnit 5, Mockito, jqwik (PBT) |
| Trazabilidad | AWS X-Ray |
| Monitoreo | Amazon CloudWatch + OpenSearch |

## Estructura del Proyecto

```
spartan-golden-gym/
├── pom.xml                          # POM padre multi-módulo
├── common-lib/                      # Librería compartida (DTOs, excepciones, Kafka config)
├── api-gateway/                     # Spring Cloud Gateway
├── servicio-usuarios/               # Registro, auth, perfiles, MFA, GDPR
├── servicio-gimnasio/               # Gimnasios, geo, check-in, equipamiento
├── servicio-entrenamiento/          # Planes, rutinas, ejercicios
├── servicio-seguimiento/            # Workouts en tiempo real, wearables
├── servicio-nutricion/              # Planes nutricionales, alimentos, macros
├── servicio-ia-coach/               # IA, recomendaciones, sobreentrenamiento
├── servicio-social/                 # Comunidad, desafíos, rankings
├── servicio-pagos/                  # Suscripciones, Stripe, PayPal
├── servicio-analiticas/             # Métricas, Kafka Streams, Redshift
├── servicio-notificaciones/         # Push (FCM), email (SES), SMS (SNS)
├── servicio-reservas/               # Clases grupales, listas de espera
├── servicio-mensajeria/             # Chat directo/grupal, WebSocket
├── servicio-calendario/             # Eventos, sync calendarios externos
├── infrastructure/terraform/        # Infraestructura AWS como código
├── k8s/                             # Manifiestos Kubernetes (base + overlays)
├── docs/                            # Documentación técnica y OpenAPI specs
└── .github/workflows/               # CI/CD pipelines
```

## Bases de Datos por Servicio

| Servicio | Base de Datos | Uso |
|---|---|---|
| Usuarios | Aurora PostgreSQL | Perfiles, auth, auditoría |
| Gimnasio | Aurora PostgreSQL + Redis GeoSet | Ubicaciones, geofences |
| Entrenamiento | Aurora PostgreSQL | Planes, rutinas, ejercicios |
| Seguimiento | DynamoDB + Timestream | Sesiones, métricas biométricas |
| Nutrición | Aurora PostgreSQL | Alimentos, planes, comidas |
| IA Coach | SageMaker + Neptune | Modelos ML, grafo de ejercicios |
| Social | Neptune + Redis Sorted Sets | Relaciones, rankings |
| Pagos | Aurora PostgreSQL | Transacciones, suscripciones |
| Analíticas | Redshift | Métricas de negocio |
| Notificaciones | DynamoDB | Estado de entrega |
| Reservas | Aurora PostgreSQL | Clases, disponibilidad |
| Mensajería | DynamoDB | Historial de mensajes |
| Calendario | Aurora PostgreSQL | Eventos, sincronización |

## Tópicos Kafka

| Tópico | Particiones | Retención | Productores | Consumidores |
|---|---|---|---|---|
| workout.completed | 20 | 7d | Seguimiento | IA Coach, Social, Analíticas |
| user.achievements | 10 | 7d | Social | Notificaciones, Analíticas |
| real.time.heartrate | 50 | 24h | Seguimiento | IA Coach |
| ai.recommendations.request | 15 | 7d | IA Coach | Analíticas |
| social.interactions | 20 | 7d | Social | Analíticas |
| nutrition.logs | 20 | 7d | Nutrición | IA Coach, Analíticas |
| gym.occupancy | 10 | 24h | Gimnasio | WebSocket (clientes) |
| bookings.events | 10 | 7d | Reservas | Calendario, Notificaciones |

## Seguridad

- TLS 1.3 en tránsito, AES-256 en reposo
- JWT con expiración configurable, sesiones en Redis
- MFA opcional para todos los usuarios
- Rate limiting: 1000 req/min (auth), 100 req/min (IP)
- Circuit breaker: 50% fallos en ventana de 10 solicitudes, 30s abierto
- Cumplimiento GDPR/LGPD (soft delete, exportación de datos)
- PCI DSS para datos de tarjetas
- Auditoría inmutable en tabla `audit_log`
