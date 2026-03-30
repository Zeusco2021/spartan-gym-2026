-- Training groups
CREATE TABLE training_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    created_by UUID NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Group members (join table)
CREATE TABLE training_group_members (
    group_id UUID NOT NULL REFERENCES training_groups(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    PRIMARY KEY (group_id, user_id)
);

-- Social interactions
CREATE TABLE interactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    target_id UUID,
    target_type VARCHAR(50),
    content VARCHAR(2000),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_interactions_user_id ON interactions(user_id);
CREATE INDEX idx_interactions_target ON interactions(target_id, target_type);
CREATE INDEX idx_training_group_members_user ON training_group_members(user_id);
