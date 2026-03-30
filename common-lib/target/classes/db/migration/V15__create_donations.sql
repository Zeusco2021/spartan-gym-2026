-- Servicio_Pagos: Tabla de donaciones
CREATE TABLE donations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    donor_id UUID NOT NULL,
    creator_id UUID NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    message TEXT,
    paypal_transaction_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);
