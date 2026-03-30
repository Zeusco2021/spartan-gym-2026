-- Servicio_Calendario: Tabla de eventos de calendario
CREATE TABLE calendar_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    reference_id UUID,
    title VARCHAR(255) NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    reminder_minutes INT DEFAULT 30,
    external_calendar_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);
