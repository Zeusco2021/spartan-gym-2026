-- Servicio_Entrenamiento: Tabla de ejercicios
CREATE TABLE exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    muscle_groups JSONB NOT NULL,
    equipment_required JSONB,
    difficulty VARCHAR(20),
    video_url VARCHAR(500),
    instructions TEXT
);
