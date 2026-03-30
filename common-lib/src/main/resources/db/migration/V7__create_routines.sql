-- Servicio_Entrenamiento: Tabla de rutinas
CREATE TABLE routines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id UUID REFERENCES training_plans(id),
    name VARCHAR(255) NOT NULL,
    day_of_week INT,
    sort_order INT
);
