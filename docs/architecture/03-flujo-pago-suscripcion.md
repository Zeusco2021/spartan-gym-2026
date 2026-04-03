# Flujo de Pago y Suscripción

## Descripción

Diagrama de secuencia que muestra el flujo completo de suscripción a un plan,
procesamiento de pago, manejo de fallos con reintentos y flujo de donaciones vía PayPal.

## Diagrama de Secuencia — Suscripción

```mermaid
sequenceDiagram
    participant C as Cliente (Web/Móvil)
    participant GW as API Gateway
    participant SP as Servicio_Pagos
    participant PG as Aurora PostgreSQL
    participant STR as Stripe/Adyen
    participant K as Kafka
    participant SNOT as Servicio_Notificaciones
    participant SU as Servicio_Usuarios

    Note over C,SU: Fase 1 — Agregar Método de Pago

    C->>STR: Tokenizar tarjeta (SDK cliente)
    STR-->>C: paymentToken
    C->>GW: POST /api/payments/methods<br/>{provider: "stripe", token}
    GW->>SP: Enrutar solicitud
    SP->>STR: Crear PaymentMethod con token
    STR-->>SP: PaymentMethod confirmado
    SP->>PG: INSERT payment_method
    SP-->>GW: 201 {methodId, last4, type}
    GW-->>C: 201 Created

    Note over C,SU: Fase 2 — Suscripción

    C->>GW: POST /api/payments/subscribe<br/>{planType: "premium", paymentProvider, paymentMethodId}
    GW->>SP: Enrutar solicitud
    SP->>PG: Verificar suscripción activa existente
    alt Suscripción activa existe
        SP-->>GW: 409 Conflict
        GW-->>C: 409 Ya tiene suscripción activa
    else Sin suscripción activa
        SP->>STR: Crear suscripción recurrente
        STR->>STR: Procesar primer cobro
        alt Pago exitoso
            STR-->>SP: Suscripción activa + transactionId
            SP->>PG: INSERT subscriptions {status: active}
            SP->>PG: INSERT transactions {status: completed}
            SP->>K: Publicar payment.completed
            K->>SNOT: Consumir payment.completed
            SNOT->>C: Push: "Membresía Premium activada"
            K->>SU: Actualizar membresía del usuario
            SP-->>GW: 201 {subscription}
            GW-->>C: 201 Suscripción creada
        else Pago fallido
            STR-->>SP: Error de pago
            SP->>PG: INSERT transactions {status: failed}
            SP-->>GW: 400 Error de pago
            GW-->>C: 400 Pago rechazado
        end
    end

    Note over C,SU: Fase 3 — Reintento de Pago Recurrente (Asíncrono)

    STR->>SP: Webhook: pago recurrente fallido
    SP->>PG: UPDATE transactions {status: failed, retry_count++}

    loop Hasta 3 reintentos (cada 24h)
        SP->>STR: Reintentar cobro
        alt Reintento exitoso
            STR-->>SP: Pago procesado
            SP->>PG: UPDATE subscription {status: active}
            SP->>K: Publicar payment.completed
            K->>SNOT: Notificar usuario: "Pago procesado"
        else Reintento fallido
            STR-->>SP: Error
            SP->>K: Publicar payment.retry.failed
            K->>SNOT: Notificar usuario: "Reintento fallido"
        end
    end

    alt 3 reintentos agotados
        SP->>PG: UPDATE subscription {status: suspended}
        SP->>K: Publicar membership.suspended
        K->>SNOT: Notificar: "Membresía suspendida"
        K->>SU: Desactivar membresía
    end
```

## Diagrama de Secuencia — Donación (PayPal)

```mermaid
sequenceDiagram
    participant C as Cliente
    participant GW as API Gateway
    participant SP as Servicio_Pagos
    participant PP as PayPal API
    participant PG as Aurora PostgreSQL
    participant K as Kafka
    participant SNOT as Servicio_Notificaciones

    C->>GW: POST /api/payments/donations<br/>{creatorId, amount, currency, message}
    GW->>SP: Enrutar solicitud
    SP->>PP: Crear pago PayPal
    PP-->>SP: paypalTransactionId
    SP->>PG: INSERT donations<br/>{donorId, creatorId, amount, paypalTransactionId}
    SP->>K: Publicar donation.completed
    K->>SNOT: Consumir donation.completed
    SNOT->>SNOT: Notificar al creador (push + email)
    SP-->>GW: 201 {donationId, amount}
    GW-->>C: 201 Donación procesada
```

## Servicios Involucrados

| Servicio | Rol |
|---|---|
| API Gateway | Validación JWT, enrutamiento |
| Servicio_Pagos | Procesamiento de pagos, suscripciones, reembolsos, donaciones |
| Aurora PostgreSQL | Persistencia de transacciones, suscripciones, donaciones |
| Stripe/Adyen | Pasarelas de pago para suscripciones |
| PayPal | Procesamiento de donaciones |
| Kafka | Eventos de pago |
| Servicio_Notificaciones | Notificaciones de estado de pago |
| Servicio_Usuarios | Actualización de estado de membresía |

## Notas

- Los tokens de tarjeta se generan en el cliente (SDK de Stripe/Adyen) para cumplir PCI DSS.
- Los reintentos de pago se ejecutan hasta 3 veces con intervalos de 24 horas.
- Tras 3 reintentos fallidos, la membresía se suspende automáticamente.
- Las donaciones se procesan exclusivamente vía PayPal.
- Todas las transacciones se registran en la tabla de auditoría.
