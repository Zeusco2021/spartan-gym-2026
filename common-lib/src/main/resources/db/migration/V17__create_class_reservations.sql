-- Servicio_Reservas: Tabla de reservas de clases
CREATE TABLE class_reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID REFERENCES group_classes(id),
    user_id UUID NOT NULL,
    status VARCHAR(20) DEFAULT 'confirmed',
    penalty_count INT DEFAULT 0,
    reserved_at TIMESTAMP DEFAULT NOW(),
    cancelled_at TIMESTAMP
);
