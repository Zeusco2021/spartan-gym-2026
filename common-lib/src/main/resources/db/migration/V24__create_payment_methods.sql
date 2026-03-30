-- Servicio_Pagos: Tabla de métodos de pago (PCI DSS: solo tokens, no datos de tarjeta)
CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    payment_provider VARCHAR(20) NOT NULL,
    external_method_id VARCHAR(255) NOT NULL,
    card_last_four VARCHAR(4),
    card_brand VARCHAR(20),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);
