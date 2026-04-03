
# Plan de Implementación: Spartan Golden Gym

## Visión General

Implementación incremental de la plataforma Spartan Golden Gym, comenzando por la infraestructura base y los microservicios fundacionales, avanzando hacia funcionalidades de dominio y finalizando con integraciones, frontend y testing. Cada tarea construye sobre las anteriores, asegurando que no quede código huérfano.

## Tareas

- [x] 1. Infraestructura base con Terraform y configuración de proyecto
  - [x] 1.1 Crear estructura de proyecto multi-módulo Maven para microservicios Spring Boot 2.7.x (Java 8)
    - Crear POM padre con dependencias comunes: Spring Boot 2.7.x, Spring Cloud Gateway, Spring Kafka, Spring Data JPA, Spring Data DynamoDB, Resilience4j, jqwik, JUnit 5, Mockito
    - Crear módulos: `api-gateway`, `servicio-usuarios`, `servicio-gimnasio`, `servicio-entrenamiento`, `servicio-seguimiento`, `servicio-nutricion`, `servicio-ia-coach`, `servicio-social`, `servicio-pagos`, `servicio-analiticas`, `servicio-notificaciones`, `servicio-reservas`, `servicio-mensajeria`, `servicio-calendario`, `common-lib`
    - En `common-lib` definir DTOs de error estándar, excepciones base, configuración de Kafka, utilidades de auditoría y constantes compartidas
    - _Requisitos: 11.1, 11.2, 11.7_

  - [x] 1.2 Crear módulo `common-lib` con manejo de errores global, DTOs y utilidades compartidas
    - Implementar `GlobalExceptionHandler` (`@ControllerAdvice`) con formato de error uniforme (`error`, `message`, `timestamp`, `traceId`)
    - Definir excepciones de dominio base: `ResourceNotFoundException`, `ConflictException`, `RateLimitExceededException`, `ServiceUnavailableException`
    - Implementar DTOs de respuesta paginada, validación y auditoría
    - Definir códigos HTTP estándar: 400, 401, 403, 404, 409, 429, 500, 503
    - _Requisitos: 11.1, 13.5_

  - [x] 1.3 Crear scripts Terraform para infraestructura AWS base
    - Definir módulos Terraform para: VPC, EKS cluster, Aurora PostgreSQL, DynamoDB, Timestream, Neptune, ElastiCache Redis, Amazon MSK (Kafka), S3 + CloudFront, SageMaker, Redshift
    - Configurar Terraform workspaces para entornos: desarrollo, staging, producción
    - Configurar autoescalado EKS: CPU umbral 70%, memoria umbral 80%
    - Configurar backups automáticos diarios Aurora PostgreSQL (retención 30 días) y backups bajo demanda DynamoDB
    - Configurar replicación Aurora en 2 zonas de disponibilidad con failover automático
    - Configurar tablas globales DynamoDB en 2 regiones
    - _Requisitos: 17.1, 17.3, 17.4, 17.5, 27.1, 27.2, 27.3, 27.6_

  - [x] 1.4 Configurar tópicos Kafka en Amazon MSK
    - Crear tópicos con particiones: `workout.completed` (20), `user.achievements` (10), `real.time.heartrate` (50, retención 24h), `ai.recommendations.request` (15), `social.interactions` (20), `nutrition.logs` (20), `gym.occupancy` (10, retención 24h), `bookings.events` (10)
    - Configurar Dead Letter Queues para cada tópico
    - _Requisitos: 11.3, 11.4_

  - [x] 1.5 Crear esquemas de base de datos Aurora PostgreSQL
    - Implementar migraciones SQL (Flyway) para todas las tablas: `users`, `gyms`, `gym_chains`, `gym_equipment`, `gym_checkins`, `training_plans`, `routines`, `exercises`, `routine_exercises`, `nutrition_plans`, `foods`, `meal_logs`, `subscriptions`, `transactions`, `donations`, `group_classes`, `class_reservations`, `waitlist`, `trainer_availability`, `calendar_events`, `audit_log`
    - _Requisitos: 12.1_

  - [x] 1.6 Crear tablas DynamoDB, Timestream y configuración Neptune
    - Crear tablas DynamoDB: `workout_sessions`, `workout_sets`, `user_achievements`, `user_preferences`, `messages`, `conversations`, `notification_delivery`
    - Crear base de datos Timestream `spartan_metrics` con tablas: `heartrate_data`, `workout_metrics`, `biometric_data`, `performance_metrics`
    - Configurar Neptune con nodos (User, Exercise, MuscleGroup, Challenge, Group) y aristas (FOLLOWS, FRIEND_OF, MEMBER_OF, COMPLETED, PARTICIPATES_IN, TARGETS, ALTERNATIVE_TO)
    - _Requisitos: 12.2, 12.3, 12.4, 12.5_

- [x] 2. Punto de control — Verificar que la infraestructura base compila y las migraciones se ejecutan correctamente
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas.

- [x] 3. API Gateway y seguridad
  - [x] 3.1 Implementar API Gateway con Spring Cloud Gateway
    - Configurar enrutamiento por prefijo de ruta a cada microservicio
    - Implementar filtro de validación JWT en cada solicitud
    - Implementar rate limiting con Redis: 1000 req/min por usuario autenticado, 100 req/min por IP no autenticada
    - Implementar circuit breaker con Resilience4j (umbral 50% fallos en ventana de 10 solicitudes, 30s en estado abierto)
    - Integrar AWS X-Ray para trazabilidad distribuida
    - _Requisitos: 11.1, 11.6, 11.7, 13.6_

  - [x] 3.2 Escribir test de propiedad para rate limiting del API Gateway
    - **Propiedad 35: API Gateway aplica rate limiting**
    - **Valida: Requisitos 11.1, 13.6**

  - [ ] 3.3 Escribir test de propiedad para circuit breaker
    - **Propiedad 36: Circuit breaker activa ante fallo de microservicio**
    - **Valida: Requisito 11.6**

- [x] 4. Servicio de Usuarios — Registro, autenticación y perfiles
  - [x] 4.1 Implementar endpoints de registro y autenticación en Servicio_Usuarios
    - `POST /api/users/register`: Validar datos (nombre, email, contraseña, fecha de nacimiento), crear cuenta, cifrar contraseña con bcrypt (factor coste ≥ 12), enviar correo de verificación vía Amazon SES (máx 5s)
    - `POST /api/users/login`: Autenticar credenciales, generar JWT con expiración configurable, almacenar sesión en Redis
    - Implementar bloqueo de cuenta: 5 intentos fallidos → bloqueo 15 minutos (usando Redis `lockout:{email}`)
    - Implementar soporte para 3 roles: `client`, `trainer`, `admin`
    - _Requisitos: 1.1, 1.2, 1.3, 1.4, 1.5, 1.7_

  - [ ]* 4.2 Escribir test de propiedad para round-trip de registro y autenticación
    - **Propiedad 1: Round-trip de registro y autenticación**
    - **Valida: Requisitos 1.1, 1.4**

  - [ ]* 4.3 Escribir test de propiedad para unicidad de email
    - **Propiedad 2: Unicidad de email en registro**
    - **Valida: Requisito 1.2**

  - [ ]* 4.4 Escribir test de propiedad para bloqueo de cuenta
    - **Propiedad 3: Bloqueo de cuenta por intentos fallidos**
    - **Valida: Requisito 1.5**

  - [ ]* 4.5 Escribir test de propiedad para cifrado de contraseñas
    - **Propiedad 5: Cifrado de contraseñas con bcrypt**
    - **Valida: Requisito 1.7**

  - [x] 4.6 Implementar gestión de perfil y cumplimiento GDPR/LGPD
    - `GET/PUT /api/users/profile`: Consulta y actualización de perfil (foto, datos personales, objetivos, condiciones médicas) con persistencia en Aurora PostgreSQL (máx 2s)
    - `DELETE /api/users/profile/delete`: Solicitud de eliminación GDPR — eliminar datos personales y biométricos en máx 30 días (soft delete)
    - `POST /api/users/data-export`: Exportación de datos del usuario (portabilidad) — generar archivo con todos los datos en máx 72h
    - Registrar todas las operaciones en `audit_log`
    - _Requisitos: 1.6, 1.8, 13.3, 13.4, 13.5_

  - [ ]* 4.7 Escribir test de propiedad para round-trip de perfil
    - **Propiedad 4: Round-trip de actualización de perfil**
    - **Valida: Requisito 1.6**

  - [ ]* 4.8 Escribir test de propiedad para eliminación GDPR
    - **Propiedad 6: Eliminación de datos personales (GDPR)**
    - **Valida: Requisito 1.8**

  - [ ]* 4.9 Escribir test de propiedad para exportación de datos
    - **Propiedad 38: Exportación de datos del usuario (portabilidad)**
    - **Valida: Requisito 13.4**

  - [ ]* 4.10 Escribir test de propiedad para log de auditoría
    - **Propiedad 39: Log de auditoría inmutable para datos sensibles**
    - **Valida: Requisito 13.5**

  - [x] 4.11 Implementar MFA y onboarding
    - `POST /api/users/mfa/setup`: Configuración de autenticación multifactor
    - `GET/POST /api/users/onboarding`: Flujo de onboarding con cuestionario de evaluación inicial (nivel, objetivos, limitaciones médicas, frecuencia deseada)
    - Guardar progreso parcial de onboarding para reanudación posterior
    - Marcar perfil como activo al completar onboarding
    - _Requisitos: 13.2, 24.1, 24.2, 24.6, 24.7, 24.8_

  - [ ]* 4.12 Escribir test de propiedad para MFA
    - **Propiedad 37: MFA requerido cuando está habilitado**
    - **Valida: Requisito 13.2**

  - [ ]* 4.13 Escribir test de propiedad para onboarding completo
    - **Propiedad 53: Onboarding completo genera perfil activo y primer plan**
    - **Valida: Requisitos 24.1, 24.2, 24.3, 24.6**

  - [ ]* 4.14 Escribir test de propiedad para onboarding parcial
    - **Propiedad 54: Onboarding parcial se guarda y es reanudable**
    - **Valida: Requisitos 24.7, 24.8**

- [x] 5. Punto de control — Verificar registro, autenticación, perfiles y onboarding
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas.

- [x] 6. Servicio de Gimnasios — Ubicaciones, equipamiento y ocupación
  - [x] 6.1 Implementar CRUD de gimnasios y búsqueda por geolocalización
    - `POST/GET /api/gyms`: Crear y listar gimnasios con datos (nombre, dirección, coordenadas GPS, horarios, equipamiento)
    - `GET/PUT /api/gyms/{id}`: Detalle y actualización de gimnasio
    - `GET /api/gyms/nearby`: Búsqueda por geolocalización usando geofences en Redis GeoSet, resultados ordenados por distancia (máx 1s)
    - Soportar múltiples ubicaciones bajo una misma cadena (`gym_chains`)
    - _Requisitos: 2.1, 2.2, 2.3_

  - [ ]* 6.2 Escribir test de propiedad para round-trip de gimnasio
    - **Propiedad 7: Round-trip de registro de gimnasio**
    - **Valida: Requisitos 2.1, 2.2**

  - [ ]* 6.3 Escribir test de propiedad para ordenamiento por distancia
    - **Propiedad 8: Ordenamiento por distancia en búsqueda de gimnasios cercanos**
    - **Valida: Requisito 2.3**

  - [x] 6.4 Implementar check-in por QR, ocupación y equipamiento
    - `POST /api/gyms/{id}/checkin`: Verificar membresía activa y registrar ingreso por QR
    - `GET /api/gyms/{id}/occupancy`: Consultar ocupación actual; publicar en tópico Kafka `gym.occupancy` cada 60s
    - `GET/PUT /api/gyms/{id}/equipment`: Inventario de equipamiento con actualización reflejada en máx 5s
    - _Requisitos: 2.4, 2.5, 2.6_

  - [ ]* 6.5 Escribir test de propiedad para check-in con membresía
    - **Propiedad 9: Check-in verifica membresía activa**
    - **Valida: Requisito 2.4**

  - [ ]* 6.6 Escribir test de propiedad para round-trip de equipamiento
    - **Propiedad 10: Round-trip de inventario de equipamiento**
    - **Valida: Requisito 2.6**

- [x] 7. Servicio de Entrenamiento — Planes, rutinas y ejercicios
  - [x] 7.1 Implementar CRUD de planes de entrenamiento, rutinas y ejercicios
    - `POST/GET /api/training/plans`: Crear y listar planes de entrenamiento
    - `GET/PUT/DELETE /api/training/plans/{id}`: CRUD de plan individual
    - `POST /api/training/plans/{id}/assign`: Asignar plan a cliente (entrenador)
    - `GET /api/training/exercises`: Catálogo de ejercicios con grupos musculares, equipamiento, dificultad, video URL e instrucciones
    - `POST/GET /api/training/routines`: Gestión de rutinas con ejercicios ordenados
    - Notificar al cliente vía push cuando el entrenador modifica su plan
    - _Requisitos: 10.2, 10.5_

  - [ ]* 7.2 Escribir test de propiedad para asignación de planes por entrenador
    - **Propiedad 33: Entrenador asigna y modifica planes de clientes**
    - **Valida: Requisitos 10.2, 10.5**

- [x] 8. Servicio de Seguimiento — Entrenamientos en tiempo real y wearables
  - [x] 8.1 Implementar sesiones de entrenamiento y registro de series
    - `POST /api/workouts/start`: Crear sesión en DynamoDB
    - `POST /api/workouts/{id}/sets`: Registrar serie (peso, repeticiones, descanso)
    - `POST /api/workouts/{id}/heartrate`: Registrar frecuencia cardíaca del wearable, publicar en Kafka `real.time.heartrate` (latencia máx 2s)
    - `POST /api/workouts/{id}/complete`: Finalizar sesión, publicar en Kafka `workout.completed`
    - `GET /api/workouts/history`: Historial completo de entrenamientos
    - `GET /api/workouts/progress`: Métricas de progreso
    - Almacenar métricas en Timestream
    - _Requisitos: 4.1, 4.2, 4.3, 4.5, 4.6, 4.9, 8.4_

  - [ ]* 8.2 Escribir test de propiedad para round-trip de sesión de entrenamiento
    - **Propiedad 17: Round-trip de sesión de entrenamiento**
    - **Valida: Requisitos 4.1, 4.3, 4.6**

  - [ ]* 8.3 Escribir test de propiedad para métricas en Timestream
    - **Propiedad 18: Métricas de rendimiento almacenadas en Timestream**
    - **Valida: Requisito 4.9**

  - [x] 8.4 Implementar integración con wearables y sincronización
    - `POST /api/wearables/connect`: Conectar wearable (Apple Watch, Fitbit, Garmin)
    - `POST /api/wearables/sync`: Sincronizar datos pendientes (frecuencia cardíaca, pasos, calorías, sueño)
    - Almacenar datos biométricos en Timestream con retención configurable
    - Manejar desconexión: almacenamiento local y sincronización posterior
    - Cifrar datos biométricos en tránsito (TLS 1.3) y en reposo (AES-256)
    - _Requisitos: 8.1, 8.2, 8.3, 8.5, 8.6_

  - [ ]* 8.5 Escribir test de propiedad para round-trip de datos de wearable
    - **Propiedad 30: Round-trip de datos de wearable**
    - **Valida: Requisitos 8.2, 8.3**

  - [x] 8.6 Implementar notificación al entrenador al completar entrenamiento
    - Consumir evento `workout.completed` de Kafka
    - Si el cliente tiene entrenador asignado, enviar notificación con resumen de sesión
    - _Requisitos: 10.3_

  - [ ]* 8.7 Escribir test de propiedad para notificación al entrenador
    - **Propiedad 34: Notificación al entrenador cuando cliente completa entrenamiento**
    - **Valida: Requisito 10.3**

- [x] 9. Punto de control — Verificar gimnasios, entrenamiento y seguimiento
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas.

- [x] 10. Servicio de Nutrición — Planes nutricionales, alimentos y seguimiento de macros
  - [x] 10.1 Implementar planes nutricionales y base de datos de alimentos
    - `POST/GET /api/nutrition/plans`: Generar plan nutricional alineado con Plan_Entrenamiento activo
    - `GET /api/nutrition/foods`: Base de datos de alimentos con calorías, proteínas, carbohidratos, grasas y micronutrientes
    - `GET /api/nutrition/foods/barcode/{code}`: Búsqueda por código de barras
    - `GET /api/nutrition/recipes`: Recetas recomendadas según objetivos y preferencias alimentarias
    - `GET /api/nutrition/supplements`: Información de suplementos (pre-entrenamientos, proteínas, creatina)
    - Adaptar base de datos de alimentos a preferencias regionales
    - _Requisitos: 5.1, 5.2, 5.3, 5.5, 5.6, 14.4_

  - [ ]* 10.2 Escribir test de propiedad para información nutricional completa
    - **Propiedad 20: Alimentos en base de datos tienen información nutricional completa**
    - **Valida: Requisito 5.2**

  - [ ]* 10.3 Escribir test de propiedad para búsqueda por código de barras
    - **Propiedad 21: Round-trip de búsqueda por código de barras**
    - **Valida: Requisito 5.3**

  - [ ]* 10.4 Escribir test de propiedad para recetas según objetivos
    - **Propiedad 22: Recetas recomendadas respetan objetivos y preferencias**
    - **Valida: Requisito 5.5**

  - [x] 10.5 Implementar registro de comidas y balance diario de macronutrientes
    - `POST /api/nutrition/meals`: Registrar comida, calcular y actualizar balance diario, publicar en Kafka `nutrition.logs`
    - `GET /api/nutrition/daily-balance`: Balance diario de calorías y macronutrientes (suma de comidas del día)
    - Detectar déficit/exceso significativo de macros durante 3 días consecutivos y notificar al usuario
    - _Requisitos: 5.4, 5.7_

  - [ ]* 10.6 Escribir test de propiedad para balance diario de macronutrientes
    - **Propiedad 19: Balance diario de macronutrientes es suma de comidas**
    - **Valida: Requisito 5.4**

  - [ ]* 10.7 Escribir test de propiedad para notificación por déficit/exceso
    - **Propiedad 23: Notificación por déficit/exceso de macros tras 3 días**
    - **Valida: Requisito 5.7**

- [x] 11. Servicio IA Coach — Planes personalizados y recomendaciones con ML
  - [x] 11.1 Implementar generación de planes personalizados y recomendaciones
    - `POST /api/ai/plans/generate`: Generar Plan_Entrenamiento personalizado basado en evaluación inicial (máx 5s), adaptado a progreso, edad y condiciones médicas
    - `POST /api/ai/recommendations`: Recomendación de ejercicio (máx 500ms)
    - `POST /api/ai/alternatives`: Ejercicios alternativos cuando equipamiento no disponible (mismos grupos musculares)
    - `POST /api/ai/warmup`: Recomendación de calentamiento previo según tipo de ejercicio
    - Detectar duplicación de equipamiento en rutinas y sugerir redistribución
    - Generar rutinas sin equipamiento cuando usuario indica no tener acceso
    - Aplicar progresión automática de carga y volumen basada en historial
    - Publicar eventos en Kafka `ai.recommendations.request`
    - Integrar con SageMaker Endpoints y Neptune (grafo de ejercicios)
    - _Requisitos: 3.1, 3.2, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.11_

  - [ ]* 11.2 Escribir test de propiedad para plan respeta condiciones médicas
    - **Propiedad 11: Plan de entrenamiento respeta condiciones médicas**
    - **Valida: Requisitos 3.1, 3.2**

  - [ ]* 11.3 Escribir test de propiedad para calentamiento incluido
    - **Propiedad 12: Calentamiento incluido en rutinas**
    - **Valida: Requisito 3.5**

  - [ ]* 11.4 Escribir test de propiedad para ejercicios alternativos
    - **Propiedad 13: Ejercicios alternativos para mismo grupo muscular**
    - **Valida: Requisito 3.6**

  - [ ]* 11.5 Escribir test de propiedad para rutinas sin equipamiento
    - **Propiedad 14: Rutinas sin equipamiento cuando no hay acceso**
    - **Valida: Requisito 3.9**

  - [ ]* 11.6 Escribir test de propiedad para progresión automática de carga
    - **Propiedad 16: Progresión automática de carga**
    - **Valida: Requisito 3.8**

  - [x] 11.7 Implementar detección de sobreentrenamiento y predicción de adherencia
    - `POST /api/ai/overtraining/check`: Analizar datos biométricos (FC reposo, variabilidad cardíaca, sueño) y generar alerta de descanso
    - `POST /api/ai/adherence/predict`: Predicción de adherencia al plan (precisión > 85%)
    - Registrar recomendaciones y respuestas del usuario para retroalimentar modelo ML
    - _Requisitos: 3.3, 3.10, 18.1, 18.3, 18.6_

  - [ ]* 11.8 Escribir test de propiedad para detección de sobreentrenamiento
    - **Propiedad 15: Detección de sobreentrenamiento genera alerta**
    - **Valida: Requisitos 3.3, 18.3**

  - [ ]* 11.9 Escribir test de propiedad para recomendaciones basadas en grafo
    - **Propiedad 63: Recomendaciones de ejercicios basadas en grafo**
    - **Valida: Requisito 18.2**

  - [ ]* 11.10 Escribir test de propiedad para feedback de recomendaciones
    - **Propiedad 64: Feedback de recomendaciones registrado**
    - **Valida: Requisito 18.6**

- [x] 12. Punto de control — Verificar nutrición e IA Coach
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas.

- [x] 13. Servicio Social — Comunidad, desafíos, rankings y gamificación
  - [x] 13.1 Implementar desafíos, logros y rankings
    - `POST/GET /api/social/challenges`: Crear y listar desafíos semanales/mensuales con objetivos medibles
    - `GET /api/social/achievements`: Logros del usuario; otorgar insignia al completar desafío, publicar en Kafka `user.achievements`
    - `GET /api/social/rankings`: Rankings por categoría (fuerza, resistencia, consistencia, nutrición) almacenados en Redis Sorted Sets
    - `POST /api/social/share`: Generar imagen/tarjeta compartible en redes sociales
    - _Requisitos: 6.1, 6.2, 6.3, 6.4_

  - [ ]* 13.2 Escribir test de propiedad para rankings ordenados
    - **Propiedad 24: Rankings ordenados correctamente por categoría**
    - **Valida: Requisito 6.3**

  - [ ]* 13.3 Escribir test de propiedad para insignia al completar desafío
    - **Propiedad 25: Insignia otorgada al completar desafío**
    - **Valida: Requisito 6.2**

  - [x] 13.4 Implementar grupos, relaciones sociales y sincronización en vivo
    - `POST/GET /api/social/groups`: Grupos de entrenamiento con chat grupal y planificación conjunta
    - `POST /api/social/interactions`: Registrar interacción (comentario, reacción, compartir), publicar en Kafka `social.interactions`
    - Almacenar relaciones (seguidos, amigos, grupos) en Neptune
    - Sincronizar progreso en tiempo real vía WebSockets cuando 2+ usuarios entrenan simultáneamente
    - _Requisitos: 6.5, 6.6, 6.7, 6.8_

  - [ ]* 13.5 Escribir test de propiedad para relaciones sociales en grafo
    - **Propiedad 26: Relaciones sociales persistidas en grafo**
    - **Valida: Requisito 6.7**

- [x] 14. Servicio de Pagos — Suscripciones, membresías y donaciones
  - [x] 14.1 Implementar suscripciones y procesamiento de pagos
    - `POST /api/payments/subscribe`: Procesar pago con Stripe/Adyen y activar membresía (máx 10s)
    - `GET /api/payments/transactions`: Historial de transacciones con auditoría completa
    - `GET/POST/DELETE /api/payments/methods`: Gestión de métodos de pago
    - Implementar reintentos de pago fallido: 3 intentos en intervalos de 24h, notificar en cada intento
    - Si 3 reintentos fallan: suspender membresía y notificar con instrucciones
    - `POST /api/payments/refund`: Procesar reembolso dentro del período de garantía
    - Cumplir PCI DSS para manejo de datos de tarjetas
    - _Requisitos: 7.1, 7.2, 7.3, 7.4, 7.5, 7.7, 7.8_

  - [ ]* 14.2 Escribir test de propiedad para suscripción tras pago exitoso
    - **Propiedad 27: Suscripción activada tras pago exitoso**
    - **Valida: Requisitos 7.2, 7.5**

  - [ ]* 14.3 Escribir test de propiedad para reintento y suspensión
    - **Propiedad 28: Reintento de pago fallido y suspensión**
    - **Valida: Requisitos 7.3, 7.4**

  - [ ]* 14.4 Escribir test de propiedad para reembolso
    - **Propiedad 29: Reembolso dentro del período de garantía**
    - **Valida: Requisito 7.7**

  - [x] 14.5 Implementar sistema de donaciones con PayPal
    - `POST /api/payments/donations`: Procesar donación a creador de contenido vía PayPal
    - Registrar donación (donante, creador, monto, mensaje) en Aurora PostgreSQL
    - Notificar al creador vía push con monto y mensaje del donante
    - _Requisitos: 7.6, 19.1, 19.2, 19.4_

  - [ ]* 14.6 Escribir test de propiedad para round-trip de donación
    - **Propiedad 62: Round-trip de donación a creador**
    - **Valida: Requisitos 19.2, 19.4**

- [x] 15. Punto de control — Verificar social, pagos y donaciones
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas.

- [x] 16. Servicio de Notificaciones — Push, email y SMS multicanal
  - [x] 16.1 Implementar envío multicanal y preferencias de notificación
    - `GET/PUT /api/notifications/preferences`: Preferencias por categoría (entrenamientos, social, pagos, nutrición) y por canal (push, email, SMS)
    - `GET /api/notifications/history`: Historial de notificaciones con estado de entrega
    - `POST /api/notifications/schedule`: Programar notificación (recordatorios 30 min antes de sesión)
    - Consumir eventos de múltiples tópicos Kafka y aplicar reglas de preferencia antes de enviar
    - Integrar Firebase Cloud Messaging (push), Amazon SES (email), Amazon SNS (SMS)
    - Implementar horas silenciosas: retener notificaciones no urgentes y entregar al finalizar período
    - Registrar estado de entrega (enviada, recibida, leída, fallida) en DynamoDB
    - Reintentar push fallido hasta 3 veces con intervalos exponenciales; enviar a DLQ si persiste
    - _Requisitos: 22.1, 22.2, 22.3, 22.4, 22.5, 22.6, 22.7, 22.8_

  - [ ]* 16.2 Escribir test de propiedad para notificaciones respetan preferencias
    - **Propiedad 43: Notificaciones respetan preferencias del usuario**
    - **Valida: Requisitos 22.1, 22.3, 22.5**

  - [ ]* 16.3 Escribir test de propiedad para retención en horas silenciosas
    - **Propiedad 44: Retención de notificaciones durante horas silenciosas**
    - **Valida: Requisito 22.4**

  - [ ]* 16.4 Escribir test de propiedad para estado de entrega
    - **Propiedad 45: Round-trip de estado de entrega de notificaciones**
    - **Valida: Requisito 22.6**

  - [ ]* 16.5 Escribir test de propiedad para reintento de push fallido
    - **Propiedad 46: Reintento de notificación push fallida**
    - **Valida: Requisito 22.7**

- [x] 17. Servicio de Reservas — Clases grupales y listas de espera
  - [x] 17.1 Implementar reservas de clases grupales y listas de espera
    - `POST /api/bookings/classes`: Crear clase grupal (admin) con nombre, instructor, horario, sala, capacidad, dificultad
    - `GET /api/bookings/classes`: Listar clases disponibles con filtros (tipo, instructor, horario, dificultad, ubicación)
    - `POST /api/bookings/classes/{id}/reserve`: Reservar clase (máx 2s), decrementar capacidad; si llena → agregar a lista de espera
    - `POST /api/bookings/classes/{id}/cancel`: Cancelar reserva; si ≥ 2h anticipación → liberar cupo al primer usuario en lista de espera; si < 2h → registrar penalización
    - `GET /api/bookings/waitlist/{classId}`: Estado de lista de espera
    - `GET/PUT /api/bookings/trainers/{id}/availability`: Disponibilidad horaria del entrenador
    - Publicar eventos en Kafka `bookings.events`
    - Alertar al admin si clase tiene < 50% ocupación a 24h de inicio
    - _Requisitos: 23.1, 23.2, 23.3, 23.4, 23.5, 23.6, 23.7, 23.8, 23.9_

  - [ ]* 17.2 Escribir test de propiedad para reserva decrementa capacidad
    - **Propiedad 47: Reserva de clase decrementa capacidad**
    - **Valida: Requisito 23.2**

  - [ ]* 17.3 Escribir test de propiedad para lista de espera
    - **Propiedad 48: Lista de espera cuando clase está llena**
    - **Valida: Requisito 23.3**

  - [ ]* 17.4 Escribir test de propiedad para cancelación con anticipación
    - **Propiedad 49: Cancelación con anticipación libera cupo para lista de espera**
    - **Valida: Requisito 23.4**

  - [ ]* 17.5 Escribir test de propiedad para penalización por cancelación tardía
    - **Propiedad 50: Penalización por cancelación tardía**
    - **Valida: Requisito 23.5**

  - [ ]* 17.6 Escribir test de propiedad para filtrado de clases
    - **Propiedad 51: Filtrado de clases respeta todos los criterios**
    - **Valida: Requisito 23.7**

  - [ ]* 17.7 Escribir test de propiedad para alerta de baja ocupación
    - **Propiedad 52: Alerta de baja ocupación 24h antes de clase**
    - **Valida: Requisito 23.9**

- [x] 18. Servicio de Mensajería — Chat directo y grupal
  - [x] 18.1 Implementar mensajería directa y chat grupal en tiempo real
    - `GET /api/messages/conversations`: Listar conversaciones del usuario
    - `GET /api/messages/conversations/{id}`: Historial de conversación almacenado en DynamoDB
    - `POST /api/messages/send`: Enviar mensaje (texto, imágenes, video ≤60s, voz ≤120s)
    - `ws/chat`: Canal WebSocket para entrega en tiempo real (máx 1s)
    - Soportar chats grupales hasta 100 participantes
    - Actualizar estado a "leído" cuando destinatario lee mensaje, notificar al remitente vía WebSocket
    - Si destinatario offline: almacenar mensaje y enviar push vía Servicio_Notificaciones
    - Cifrar mensajes en tránsito (TLS 1.3) y en reposo (AES-256)
    - _Requisitos: 25.1, 25.2, 25.3, 25.4, 25.5, 25.6, 25.7, 25.8_

  - [ ]* 18.2 Escribir test de propiedad para round-trip de mensajería
    - **Propiedad 55: Round-trip de mensajería directa**
    - **Valida: Requisitos 25.1, 25.4, 25.5**

  - [ ]* 18.3 Escribir test de propiedad para límite de participantes en chat grupal
    - **Propiedad 56: Límite de participantes en chat grupal**
    - **Valida: Requisito 25.2**

  - [ ]* 18.4 Escribir test de propiedad para acuse de lectura
    - **Propiedad 57: Acuse de lectura actualiza estado del mensaje**
    - **Valida: Requisito 25.6**

  - [ ]* 18.5 Escribir test de propiedad para mensaje a usuario offline
    - **Propiedad 58: Mensaje a usuario offline se almacena y genera push**
    - **Valida: Requisito 25.7**

- [x] 19. Servicio de Calendario — Horarios y sincronización
  - [x] 19.1 Implementar calendario unificado y sincronización externa
    - `GET/POST /api/calendar/events`: Eventos del usuario (entrenamientos, clases, sesiones, nutrición)
    - `PUT/DELETE /api/calendar/events/{id}`: Modificar/eliminar evento
    - `POST /api/calendar/sync`: Sincronización bidireccional con Google Calendar, Apple Calendar, Outlook Calendar
    - `GET /api/calendar/conflicts`: Detectar conflictos de solapamiento y alertar al usuario
    - Generar recordatorios configurables (15, 30, 60 min antes) y enviar al Servicio_Notificaciones
    - _Requisitos: 26.1, 26.2, 26.4, 26.5, 26.6_

  - [ ]* 19.2 Escribir test de propiedad para calendario unificado
    - **Propiedad 59: Calendario unificado consolida todas las actividades**
    - **Valida: Requisito 26.1**

  - [ ]* 19.3 Escribir test de propiedad para detección de conflictos
    - **Propiedad 60: Detección de conflictos en calendario**
    - **Valida: Requisito 26.2**

  - [ ]* 19.4 Escribir test de propiedad para recordatorios configurables
    - **Propiedad 61: Recordatorios configurables generados correctamente**
    - **Valida: Requisito 26.6**

- [x] 20. Servicio de Analíticas — Métricas y reportes
  - [x] 20.1 Implementar recopilación de métricas y generación de reportes
    - `GET /api/analytics/dashboard`: Datos del dashboard (retención, frecuencia, ingresos, ocupación)
    - `GET /api/analytics/reports`: Reportes generados semanales/mensuales con KPIs
    - `GET /api/analytics/metrics`: Métricas en tiempo real
    - Consumir eventos de múltiples tópicos Kafka para agregaciones
    - Almacenar datos analíticos en Amazon Redshift
    - Generar reportes automáticos y enviar por email a administradores
    - Monitorear salud de microservicios con CloudWatch y OpenSearch
    - _Requisitos: 16.1, 16.2, 16.3, 16.4, 16.5_

- [ ] 21. Punto de control — Verificar notificaciones, reservas, mensajería, calendario y analíticas
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas.

- [x] 22. Publicación de eventos Kafka — Verificación transversal
  - [ ]* 22.1 Escribir test de propiedad para publicación de eventos en Kafka
    - **Propiedad 67: Publicación de eventos en Kafka**
    - Verificar que cada acción de dominio (entrenamiento completado, logro obtenido, interacción social, comida registrada, reserva realizada, recomendación generada) publica el evento en el tópico Kafka correspondiente con datos completos
    - **Valida: Requisitos 3.11, 4.5, 5.4, 6.2, 6.8, 23.8**

- [x] 23. Frontend Web — React 18 + TypeScript + Vite
  - [x] 23.1 Crear proyecto React 18 + TypeScript + Vite con estructura de módulos
    - Configurar proyecto con Material-UI, Tailwind CSS, Redux Toolkit, RTK Query, Socket.io-client, react-i18next
    - Crear estructura de módulos: `auth/`, `dashboard/`, `training/`, `nutrition/`, `social/`, `gym/`, `payments/`, `calendar/`, `messages/`, `trainer/`, `analytics/`, `settings/`
    - Configurar RTK Query para comunicación con API Gateway
    - Configurar Socket.io-client para WebSocket (rankings, entrenamientos en vivo, notificaciones)
    - _Requisitos: 21.1, 21.2, 21.3, 21.4_

  - [x] 23.2 Implementar módulo de autenticación (login, registro, MFA)
    - Formulario de registro con validación (nombre, email, contraseña, fecha de nacimiento)
    - Formulario de login con soporte MFA
    - Flujo de onboarding guiado con cuestionario de evaluación inicial
    - Registro de medidas corporales iniciales y fotos de progreso
    - _Requisitos: 1.1, 1.4, 13.2, 24.1, 24.2, 24.4, 24.5_

  - [x] 23.3 Implementar dashboard personalizado y módulo de entrenamiento
    - Dashboard con resumen de entrenamiento, progreso hacia objetivos, próximas sesiones, actividad de comunidad
    - Gráficos de progreso por día/mes/año con Recharts
    - Visualización de planes, rutinas y ejercicios con videos tutoriales (Video.js + HLS)
    - _Requisitos: 21.5, 4.7, 15.1, 15.2_

  - [x] 23.4 Implementar módulo de nutrición y seguimiento de macros
    - Registro de comidas con balance diario de macronutrientes
    - Explorador de recetas y suplementos
    - _Requisitos: 5.1, 5.4, 5.5, 5.6_

  - [x] 23.5 Implementar módulo social, mensajería y calendario
    - Desafíos, rankings, logros con tarjetas compartibles
    - Chat directo y grupal con WebSocket
    - Calendario unificado con vistas diaria/semanal/mensual, drag-and-drop para reprogramar (máx 2s)
    - _Requisitos: 6.1, 6.3, 6.4, 25.1, 25.3, 26.1, 26.3, 26.7_

  - [x] 23.6 Implementar módulo de gimnasios, pagos y panel de entrenador
    - Mapa interactivo de gimnasios con Mapbox GL JS, marcadores de ubicación y ocupación
    - Suscripciones, métodos de pago, historial de transacciones, botón de donación en perfil de creador
    - Panel de entrenador: lista de clientes, planes activos, métricas de progreso con Victory Charts
    - _Requisitos: 2.7, 7.2, 19.3, 10.1, 10.4_

  - [x] 23.7 Implementar módulo de reservas de clases y panel de analíticas
    - Listado de clases filtrable, reserva, cancelación, lista de espera
    - Panel de analíticas para administradores con dashboards interactivos
    - _Requisitos: 23.7, 16.3_

  - [x] 23.8 Implementar internacionalización (i18n) y configuración
    - Soporte para 5 idiomas: inglés, español, francés, alemán, japonés con react-i18next
    - Formatos de fecha, hora, moneda y unidades según locale del usuario
    - Configuración de preferencias de notificación, idioma, unidades
    - _Requisitos: 14.1, 14.2, 14.3_

  - [ ]* 23.9 Escribir test de propiedad para traducciones completas (fast-check)
    - **Propiedad 40: Traducciones completas para todos los idiomas soportados**
    - **Valida: Requisitos 14.1, 14.2**

  - [ ]* 23.10 Escribir test de propiedad para formato según locale (fast-check)
    - **Propiedad 41: Formato de fecha, hora, moneda y unidades según locale**
    - **Valida: Requisito 14.3**

  - [ ]* 23.11 Escribir test de propiedad para videos vinculados a planes (fast-check)
    - **Propiedad 42: Videos de ejercicios vinculados a planes**
    - **Valida: Requisito 15.2**

  - [x] 23.12 Implementar accesibilidad WCAG 2.1 AA en componentes web
    - Contraste de color mínimo 4.5:1 (texto normal) y 3:1 (texto grande)
    - Etiquetas descriptivas (aria-label) en todos los elementos interactivos
    - Integrar axe-core en pipeline CI/CD
    - _Requisitos: 21.6, 29.3, 29.7, 29.8_

  - [ ]* 23.13 Escribir test de propiedad para contraste de color (fast-check)
    - **Propiedad 65: Contraste de color cumple ratio mínimo**
    - **Valida: Requisito 29.3**

  - [ ]* 23.14 Escribir test de propiedad para etiquetas descriptivas (fast-check)
    - **Propiedad 66: Etiquetas descriptivas en elementos interactivos**
    - **Valida: Requisito 29.7**

- [x] 24. Punto de control — Verificar frontend web completo
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas.

- [x] 25. Frontend Móvil — React Native con soporte offline
  - [x] 25.1 Crear proyecto React Native con estructura de módulos y soporte offline
    - Configurar proyecto con AsyncStorage/SQLite, react-native-health, react-native-camera, react-native-maps (Mapbox), Firebase Cloud Messaging
    - Crear módulos: `auth/`, `dashboard/`, `training/`, `nutrition/`, `social/`, `gym/`, `payments/`, `calendar/`, `messages/`, `settings/`, `offline/`, `wearables/`, `camera/`, `barcode/`
    - Implementar almacenamiento local del Plan_Entrenamiento activo, rutinas programadas e historial de últimos 7 días
    - Soportar operación offline hasta 72 horas sin pérdida de datos
    - _Requisitos: 9.1, 9.2, 9.3_

  - [x] 25.2 Implementar sincronización offline y resolución de conflictos
    - Sincronizar datos pendientes al recuperar conexión en orden cronológico
    - Resolver conflictos con estrategia last-write-wins
    - _Requisitos: 9.4_

  - [ ]* 25.3 Escribir test de propiedad para sincronización offline (fast-check)
    - **Propiedad 31: Sincronización offline preserva datos**
    - **Valida: Requisitos 9.1, 9.2, 9.4**

  - [ ]* 25.4 Escribir test de propiedad para resolución de conflictos (fast-check)
    - **Propiedad 32: Resolución de conflictos last-write-wins**
    - **Valida: Requisito 9.4**

  - [x] 25.5 Implementar integración con wearables y funcionalidades móviles
    - Integrar HealthKit (iOS), Google Fit (Android), Huawei Health para lectura/escritura de datos biométricos
    - Escaneo de código QR para check-in en gimnasio (máx 3s)
    - Escaneo de código de barras de alimentos
    - Notificaciones push vía Firebase Cloud Messaging
    - Mapa interactivo de gimnasios con Mapbox GL y marcadores de ocupación
    - Descarga de videos para visualización offline
    - Comparativas de objetivos semanales/diarios/mensuales con indicadores visuales
    - _Requisitos: 8.1, 9.5, 9.6, 2.7, 4.8, 5.3, 15.4_

  - [x] 25.6 Implementar accesibilidad móvil
    - Soporte para VoiceOver (iOS) y TalkBack (Android)
    - Soporte para Dynamic Type (iOS) y escalado de fuente (Android)
    - Navegación por control de voz (Voice Control iOS, Voice Access Android)
    - Modo de alto contraste respetando configuración del sistema operativo
    - Etiquetas descriptivas (contentDescription) en todos los elementos interactivos
    - Consistencia visual y funcional con la App_Web en flujos principales
    - _Requisitos: 29.1, 29.2, 29.4, 29.5, 29.6, 29.7_

- [x] 26. Punto de control — Verificar frontend móvil y sincronización offline
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas.

- [x] 27. Integración final y documentación técnica
  - [x] 27.1 Configurar pipelines CI/CD para despliegue automatizado
    - Pipeline de CI/CD para despliegue de microservicios en EKS
    - Integrar auditorías de accesibilidad automatizadas (axe-core web, Accessibility Scanner Android, Accessibility Inspector iOS)
    - _Requisitos: 17.2, 29.8_

  - [x] 27.2 Crear especificaciones OpenAPI 3.0 (Swagger) para cada microservicio
    - Documentar todos los endpoints de los 13 microservicios
    - Incluir diagramas de secuencia para flujos principales (registro, entrenamiento, pago, recomendación IA)
    - _Requisitos: 20.2, 20.1, 20.4_

  - [x] 27.3 Implementar almacenamiento multimedia en S3 + CloudFront
    - Videos de ejercicios en S3 con distribución CloudFront (latencia inicio < 2s)
    - Fotos de progreso cifradas en S3
    - _Requisitos: 12.6, 15.3, 24.5_

  - [x] 27.4 Implementar Kafka Streams para agregaciones en tiempo real
    - Agregaciones de datos de entrenamiento y métricas de rendimiento
    - _Requisitos: 11.4_

- [x] 28. Punto de control final — Verificar integración completa
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas.

## Notas

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada tarea referencia requisitos específicos para trazabilidad
- Los puntos de control aseguran validación incremental
- Los tests de propiedad (jqwik para Java, fast-check para TypeScript) validan las 67 propiedades de corrección del diseño
- Los tests unitarios (JUnit 5 + Mockito para backend, Jest + React Testing Library para frontend) validan casos específicos y edge cases
- La infraestructura Terraform debe desplegarse antes de los microservicios
