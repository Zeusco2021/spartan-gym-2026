-- Servicio_Nutricion: Tabla de registro de comidas
CREATE TABLE meal_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    food_id UUID REFERENCES foods(id),
    quantity_grams DECIMAL(8,2) NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    logged_at TIMESTAMP DEFAULT NOW()
);
