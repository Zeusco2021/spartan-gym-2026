-- Servicio_Reservas: Tabla de lista de espera
CREATE TABLE waitlist (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    class_id UUID REFERENCES group_classes(id),
    user_id UUID NOT NULL,
    position INT NOT NULL,
    added_at TIMESTAMP DEFAULT NOW()
);
