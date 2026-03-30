-- Servicio_Gimnasio: Tabla de cadenas de gimnasios
CREATE TABLE gym_chains (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
