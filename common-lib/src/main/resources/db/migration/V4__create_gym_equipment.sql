-- Servicio_Gimnasio: Tabla de equipamiento de gimnasios
CREATE TABLE gym_equipment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id UUID REFERENCES gyms(id),
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(20) DEFAULT 'available'
);
