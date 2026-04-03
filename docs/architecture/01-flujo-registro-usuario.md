# Flujo de Registro de Usuario

## Descripción

Diagrama de secuencia que muestra el flujo completo de registro de un nuevo usuario,
incluyendo validación, creación de cuenta, envío de correo de verificación y flujo de onboarding.

## Diagrama de Secuencia

```mermaid
sequenceDiagram
    participant C as Cliente (Web/Móvil)
    participant GW as API Gateway
    participant SU as Servicio_Usuarios
    participant PG as Aurora PostgreSQL
    participant RD as Cache_Redis
    participant SES as Amazon SES
    participant K as Kafka
    participant SIA as Servicio_IA_Coach
    participant SM as SageMaker

    Note over C,SM: Fase 1 — Registro

    C->>GW: POST /api/users/register<br/>{name, email, password, dateOfBirth}
    GW->>GW: Rate limiting check (Redis)
    GW->>SU: Enrutar solicitud

    SU->>PG: SELECT * FROM users WHERE email = ?
    alt Email ya existe
        PG-->>SU: Usuario encontrado
        SU-->>GW: 409 Conflict (email duplicado)
        GW-->>C: 409 Conflict
    else Email disponible
        PG-->>SU: No encontrado
        SU->>SU: Cifrar contraseña (bcrypt, coste 12)
        SU->>PG: INSERT INTO users (...)
        PG-->>SU: Usuario creado (UUID)
        SU->>SES: Enviar correo de verificación
        SU->>K: Publicar evento user.registered
        SU-->>GW: 201 Created {userId, name, email}
        GW-->>C: 201 Created
    end

    Note over C,SM: Fase 2 — Login

    C->>GW: POST /api/users/login<br/>{email, password}
    GW->>SU: Enrutar solicitud

    SU->>RD: GET lockout:{email}
    alt Cuenta bloqueada
        RD-->>SU: Intentos >= 5
        SU-->>GW: 403 Forbidden (cuenta bloqueada 15 min)
        GW-->>C: 403 Forbidden
    else Cuenta no bloqueada
        RD-->>SU: Intentos < 5
        SU->>PG: SELECT * FROM users WHERE email = ?
        SU->>SU: Verificar bcrypt hash
        alt Credenciales válidas
            SU->>SU: Generar JWT (access + refresh)
            SU->>RD: SET session:{userId} (TTL configurable)
            SU->>RD: DEL lockout:{email}
            SU-->>GW: 200 OK {accessToken, refreshToken, user}
            GW-->>C: 200 OK
        else Credenciales inválidas
            SU->>RD: INCR lockout:{email} (TTL 15 min)
            SU-->>GW: 401 Unauthorized
            GW-->>C: 401 Unauthorized
        end
    end

    Note over C,SM: Fase 3 — Onboarding

    C->>GW: POST /api/users/onboarding<br/>{fitnessLevel, goals, medicalConditions, ...}
    GW->>GW: Validar JWT
    GW->>SU: Enrutar solicitud

    SU->>PG: UPDATE users SET fitness_goals = ?, medical_conditions = ?
    SU->>SIA: POST /api/ai/plans/generate<br/>{userId, fitnessLevel, goals, medicalConditions}
    SIA->>SM: Invocar endpoint de inferencia
    SM-->>SIA: Plan personalizado generado
    SIA->>K: Publicar en ai.recommendations.request
    SIA-->>SU: Plan generado
    SU->>PG: UPDATE users SET onboarding_completed = true
    SU-->>GW: 200 OK {onboarding: completed}
    GW-->>C: 200 OK
```

## Servicios Involucrados

| Servicio | Rol |
|---|---|
| API Gateway | Rate limiting, enrutamiento, validación JWT |
| Servicio_Usuarios | Registro, autenticación, gestión de perfil |
| Aurora PostgreSQL | Persistencia de datos de usuario |
| Cache_Redis | Sesiones JWT, bloqueo de cuentas |
| Amazon SES | Envío de correo de verificación |
| Kafka | Eventos de usuario (user.registered) |
| Servicio_IA_Coach | Generación del primer plan personalizado |
| SageMaker | Inferencia ML para plan de entrenamiento |

## Notas

- Las contraseñas se cifran con bcrypt (factor de coste 12) antes de almacenarse.
- Después de 5 intentos fallidos de login, la cuenta se bloquea 15 minutos.
- El onboarding es opcional; el usuario puede omitir pasos sin afectar la generación del plan inicial.
- El correo de verificación debe enviarse en máximo 5 segundos tras el registro.
