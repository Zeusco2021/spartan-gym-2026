-- Servicio_Entrenamiento: Tabla de planes de entrenamiento
CREATE TABLE training_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    trainer_id UUID,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    ai_generated BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT NOW()
);
