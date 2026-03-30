-- Servicio_Pagos: Tabla de suscripciones
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    plan_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_provider VARCHAR(20) NOT NULL,
    external_subscription_id VARCHAR(255),
    started_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    retry_count INT DEFAULT 0
);
