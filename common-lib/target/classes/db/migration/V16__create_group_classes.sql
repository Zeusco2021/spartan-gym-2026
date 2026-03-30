-- Servicio_Reservas: Tabla de clases grupales
CREATE TABLE group_classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id UUID NOT NULL,
    instructor_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    room VARCHAR(100),
    max_capacity INT NOT NULL,
    current_capacity INT DEFAULT 0,
    difficulty_level VARCHAR(20),
    scheduled_at TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL
);
