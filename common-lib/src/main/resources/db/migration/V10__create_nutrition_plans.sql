-- Servicio_Nutricion: Tabla de planes nutricionales
CREATE TABLE nutrition_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    goal VARCHAR(50) NOT NULL,
    daily_calories INT,
    protein_grams INT,
    carbs_grams INT,
    fat_grams INT,
    created_at TIMESTAMP DEFAULT NOW()
);
