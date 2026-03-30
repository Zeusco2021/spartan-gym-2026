-- Servicio_Gimnasio: Tabla de check-ins de gimnasios
CREATE TABLE gym_checkins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id UUID REFERENCES gyms(id),
    user_id UUID NOT NULL,
    checked_in_at TIMESTAMP DEFAULT NOW(),
    checked_out_at TIMESTAMP
);
