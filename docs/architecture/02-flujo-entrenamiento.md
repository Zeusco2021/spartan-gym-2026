# Flujo de Entrenamiento

## Descripción

Diagrama de secuencia que muestra el flujo completo de una sesión de entrenamiento,
desde el inicio hasta la finalización, incluyendo registro de series, datos biométricos
de wearables y análisis post-entrenamiento por IA.

## Diagrama de Secuencia

```mermaid
sequenceDiagram
    participant U as Usuario
    participant APP as App Móvil
    participant GW as API Gateway
    participant SS as Servicio_Seguimiento
    participant DDB as DynamoDB
    participant TS as Timestream
    participant RD as Cache_Redis
    participant K as Kafka
    participant SIA as Servicio_IA_Coach
    participant SM as SageMaker
    participant W as Wearable
    participant SNOT as Servicio_Notificaciones

    Note over U,SNOT: Fase 1 — Inicio de Entrenamiento

    U->>APP: Seleccionar rutina e iniciar
    APP->>GW: POST /api/workouts/start<br/>{planId, routineId}
    GW->>GW: Validar JWT
    GW->>SS: Enrutar solicitud
    SS->>DDB: PutItem workout_sessions<br/>{userId, sessionId, status: active}
    SS->>RD: SET active_session:{userId} (TTL 4h)
    DDB-->>SS: Sesión creada
    SS-->>GW: 201 {sessionId, startedAt}
    GW-->>APP: 201 Created
    APP-->>U: Mostrar pantalla de entrenamiento activo

    Note over U,SNOT: Fase 2 — Durante el Entrenamiento

    loop Cada serie completada
        U->>APP: Registrar serie (peso, reps)
        APP->>GW: POST /api/workouts/{id}/sets<br/>{exerciseId, weight, reps, restSeconds}
        GW->>SS: Enrutar solicitud
        SS->>DDB: PutItem workout_sets<br/>{sessionId, setId, ...}
        SS-->>APP: 201 Serie registrada
    end

    loop Datos biométricos en tiempo real
        W->>APP: Datos de frecuencia cardíaca
        APP->>GW: POST /api/workouts/{id}/heartrate<br/>{bpm, deviceType}
        GW->>SS: Enrutar solicitud
        SS->>TS: Write heartrate_data<br/>{userId, sessionId, bpm}
        SS->>K: Publicar en real.time.heartrate
        K->>SIA: Consumir real.time.heartrate
        SIA->>SM: Verificar sobreentrenamiento
        alt Sobreentrenamiento detectado
            SM-->>SIA: Alerta de sobreentrenamiento
            SIA->>K: Publicar alerta
            K->>SNOT: Consumir alerta
            SNOT->>APP: Push notification: "Descanso recomendado"
        end
    end

    Note over U,SNOT: Fase 3 — Finalización

    U->>APP: Finalizar entrenamiento
    APP->>GW: POST /api/workouts/{id}/complete
    GW->>SS: Enrutar solicitud
    SS->>DDB: UpdateItem workout_sessions<br/>{status: completed, completedAt, summary}
    SS->>TS: Write workout_metrics<br/>{totalVolume, duration, calories}
    SS->>RD: DEL active_session:{userId}
    SS->>K: Publicar en workout.completed<br/>{userId, sessionId, summary}
    SS-->>GW: 200 {summary: duration, calories, sets, volume}
    GW-->>APP: 200 OK
    APP-->>U: Mostrar resumen del entrenamiento

    Note over U,SNOT: Fase 4 — Post-Entrenamiento (Asíncrono)

    K->>SIA: Consumir workout.completed
    SIA->>SM: Actualizar modelo de progreso
    SIA->>SIA: Ajustar plan de entrenamiento
    SIA->>K: Publicar en ai.recommendations.request

    K->>SNOT: Consumir workout.completed
    SNOT->>SNOT: Notificar al entrenador asignado
```

## Servicios Involucrados

| Servicio | Rol |
|---|---|
| API Gateway | Validación JWT, enrutamiento |
| Servicio_Seguimiento | Gestión de sesiones, registro de series y biométricos |
| DynamoDB | Almacenamiento de sesiones y series (alta velocidad) |
| Timestream | Almacenamiento de series temporales (frecuencia cardíaca, métricas) |
| Cache_Redis | Sesiones activas en caché |
| Kafka | Eventos: real.time.heartrate, workout.completed |
| Servicio_IA_Coach | Detección de sobreentrenamiento, actualización de modelo |
| SageMaker | Inferencia ML para sobreentrenamiento |
| Servicio_Notificaciones | Alertas push al usuario y entrenador |

## Tópicos Kafka

| Tópico | Productor | Consumidores |
|---|---|---|
| real.time.heartrate | Servicio_Seguimiento | Servicio_IA_Coach |
| workout.completed | Servicio_Seguimiento | Servicio_IA_Coach, Servicio_Social, Servicio_Analiticas |
| ai.recommendations.request | Servicio_IA_Coach | Servicio_Analiticas |

## Notas

- La sesión activa se almacena en Redis con TTL de 4 horas como protección contra sesiones huérfanas.
- Los datos biométricos se publican en Kafka con latencia máxima de 2 segundos.
- Si la conexión con el wearable se interrumpe, la app almacena datos localmente y sincroniza al reconectar.
- El resumen post-entrenamiento incluye: duración, calorías, series totales y volumen total.
