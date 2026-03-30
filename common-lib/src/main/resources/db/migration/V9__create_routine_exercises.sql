-- Servicio_Entrenamiento: Tabla de ejercicios por rutina
CREATE TABLE routine_exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    routine_id UUID REFERENCES routines(id),
    exercise_id UUID REFERENCES exercises(id),
    sets INT NOT NULL,
    reps VARCHAR(50),
    rest_seconds INT,
    sort_order INT
);
