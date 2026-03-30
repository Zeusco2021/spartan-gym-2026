CREATE TABLE challenges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    type VARCHAR(20) NOT NULL,
    category VARCHAR(50) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    target_value DOUBLE PRECISION NOT NULL,
    badge_name VARCHAR(255),
    created_by UUID,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    challenge_id UUID REFERENCES challenges(id),
    type VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    badge_name VARCHAR(255),
    description VARCHAR(500),
    earned_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_achievements_user_id ON achievements(user_id);
CREATE INDEX idx_achievements_user_challenge ON achievements(user_id, challenge_id);
CREATE INDEX idx_challenges_type ON challenges(type);
CREATE INDEX idx_challenges_category ON challenges(category);
