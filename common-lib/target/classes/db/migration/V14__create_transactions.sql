-- Servicio_Pagos: Tabla de transacciones
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    subscription_id UUID REFERENCES subscriptions(id),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_provider VARCHAR(20) NOT NULL,
    external_transaction_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);
