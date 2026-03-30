-- Servicio_Gimnasio: Tabla de gimnasios
CREATE TABLE gyms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chain_id UUID REFERENCES gym_chains(id),
    name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    operating_hours JSONB NOT NULL,
    max_capacity INT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
