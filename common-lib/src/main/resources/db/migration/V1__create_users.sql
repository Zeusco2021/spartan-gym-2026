-- Servicio_Usuarios: Tabla de usuarios
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('client', 'trainer', 'admin')),
    locale VARCHAR(10) DEFAULT 'es',
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    account_locked_until TIMESTAMP,
    failed_login_attempts INT DEFAULT 0,
    onboarding_completed BOOLEAN DEFAULT FALSE,
    profile_photo_url VARCHAR(500),
    fitness_goals JSONB,
    medical_conditions JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP -- soft delete para GDPR
);
