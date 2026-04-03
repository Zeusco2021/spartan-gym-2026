# Flujo de Recomendación IA

## Descripción

Diagrama de secuencia que muestra el flujo de generación de recomendaciones personalizadas
por el Servicio_IA_Coach, incluyendo generación de planes, ejercicios alternativos,
detección de sobreentrenamiento y predicción de adherencia.

## Diagrama de Secuencia — Generación de Plan Personalizado

```mermaid
sequenceDiagram
    participant C as Cliente (Web/Móvil)
    participant GW as API Gateway
    participant SIA as Servicio_IA_Coach
    participant SM as SageMaker
    participant NEP as Neptune
    participant TS as Timestream
    participant PG as Aurora PostgreSQL
    participant K as Kafka
    participant SE as Servicio_Entrenamiento
    participant SNOT as Servicio_Notificaciones

    Note over C,SNOT: Fase 1 — Solicitud de Plan Personalizado

    C->>GW: POST /api/ai/plans/generate<br/>{userId, fitnessLevel, goals, medicalConditions, equipment}
    GW->>GW: Validar JWT
    GW->>SIA: Enrutar solicitud

    SIA->>TS: Consultar historial biométrico del usuario
    TS-->>SIA: Datos de rendimiento y progreso

    SIA->>NEP: Consultar grafo de ejercicios<br/>MATCH (e:Exercise)-[:TARGETS]->(mg:MuscleGroup)
    NEP-->>SIA: Ejercicios por grupo muscular

    SIA->>SIA: Filtrar ejercicios contraindicados<br/>por condiciones médicas

    SIA->>SM: Invocar endpoint de inferencia<br/>{userData, exerciseGraph, history}
    SM->>SM: Modelo ML genera plan óptimo
    SM-->>SIA: Plan personalizado con rutinas

    SIA->>SIA: Agregar calentamiento a cada rutina
    SIA->>SIA: Verificar no duplicación de equipamiento
    SIA->>SIA: Aplicar progresión de carga

    SIA->>K: Publicar en ai.recommendations.request
    SIA-->>GW: 200 {planId, routines[], warnings[]}
    GW-->>C: 200 Plan generado

    Note over C,SNOT: Fase 2 — Ejercicios Alternativos

    C->>GW: POST /api/ai/alternatives<br/>{exerciseId, reason: "equipment_unavailable"}
    GW->>SIA: Enrutar solicitud

    SIA->>NEP: Consultar ejercicio original<br/>MATCH (e:Exercise {id})-[:TARGETS]->(mg)
    NEP-->>SIA: Grupos musculares objetivo

    SIA->>NEP: Buscar alternativas<br/>MATCH (alt:Exercise)-[:TARGETS]->(mg)<br/>WHERE alt.equipment = null OR alt.equipment IN available
    NEP-->>SIA: Ejercicios alternativos

    SIA->>SM: Rankear alternativas por efectividad
    SM-->>SIA: Alternativas rankeadas

    SIA-->>GW: 200 {originalExercise, targetMuscleGroups, alternatives[]}
    GW-->>C: 200 Alternativas

    Note over C,SNOT: Fase 3 — Detección de Sobreentrenamiento

    C->>GW: POST /api/ai/overtraining/check<br/>{userId, restingHeartRate, sleepQuality, performanceTrend}
    GW->>SIA: Enrutar solicitud

    SIA->>TS: Consultar tendencia de frecuencia cardíaca (últimos 14 días)
    TS-->>SIA: Serie temporal de HR

    SIA->>SM: Invocar modelo de detección de sobreentrenamiento<br/>{biometrics, performanceTrend, trainingLoad}
    SM-->>SIA: {overtraining: true/false, riskLevel, indicators}

    alt Sobreentrenamiento detectado
        SIA->>K: Publicar alerta de sobreentrenamiento
        K->>SNOT: Consumir alerta
        SNOT->>C: Push: "Se recomienda descanso de X días"
        SIA-->>GW: 200 {overtrainingDetected: true, suggestedRestDays: 3}
    else Sin sobreentrenamiento
        SIA-->>GW: 200 {overtrainingDetected: false, riskLevel: "low"}
    end
    GW-->>C: 200 Resultado

    Note over C,SNOT: Fase 4 — Predicción de Adherencia

    C->>GW: POST /api/ai/adherence/predict<br/>{userId, planId}
    GW->>SIA: Enrutar solicitud

    SIA->>TS: Consultar historial de entrenamientos completados
    TS-->>SIA: Datos de adherencia histórica

    SIA->>SM: Invocar modelo de predicción de adherencia<br/>{completionRate, frequency, consistency}
    SM-->>SIA: {probability: 0.78, riskFactors, suggestions}

    SIA->>K: Publicar en ai.recommendations.request
    SIA-->>GW: 200 {adherenceProbability, riskFactors, suggestions}
    GW-->>C: 200 Predicción
```

## Servicios Involucrados

| Servicio | Rol |
|---|---|
| API Gateway | Validación JWT, enrutamiento |
| Servicio_IA_Coach | Orquestación de recomendaciones, filtrado por condiciones médicas |
| SageMaker | Endpoints de inferencia ML (plan, sobreentrenamiento, adherencia) |
| Neptune | Grafo de ejercicios, grupos musculares, alternativas |
| Timestream | Datos biométricos históricos, métricas de rendimiento |
| Kafka | Eventos: ai.recommendations.request |
| Servicio_Notificaciones | Alertas de sobreentrenamiento |

## Modelos ML en SageMaker

| Modelo | Función | SLA |
|---|---|---|
| Plan Generator | Genera planes personalizados | < 5s |
| Exercise Recommender | Recomienda ejercicios basado en grafo | < 500ms |
| Overtraining Detector | Detecta patrones de sobreentrenamiento | < 500ms |
| Adherence Predictor | Predice probabilidad de completar plan (>85% precisión) | < 500ms |

## Reglas de Negocio

1. Los ejercicios contraindicados por condiciones médicas se filtran antes de la inferencia ML.
2. Cada rutina generada incluye calentamiento apropiado al tipo de ejercicio.
3. Las alternativas sugeridas deben trabajar los mismos grupos musculares que el ejercicio original.
4. Si el usuario no tiene acceso a equipamiento, todos los ejercicios deben ser sin equipamiento.
5. La progresión de carga se basa en el historial de rendimiento del usuario.
6. Todas las recomendaciones se publican en Kafka para retroalimentar los modelos.
