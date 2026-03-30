package com.spartangoldengym.common.config;

/**
 * Amazon Neptune graph schema constants: vertex labels, edge labels, and property keys.
 * Used by microservices that interact with the Neptune graph (Servicio_Social, Servicio_IA_Coach).
 *
 * Validates: Requirements 12.4, 12.5
 */
public final class NeptuneSchema {

    private NeptuneSchema() {
    }

    // --- Vertex labels ---

    public static final String VERTEX_USER = "User";
    public static final String VERTEX_EXERCISE = "Exercise";
    public static final String VERTEX_MUSCLE_GROUP = "MuscleGroup";
    public static final String VERTEX_CHALLENGE = "Challenge";
    public static final String VERTEX_GROUP = "Group";

    // --- Edge labels ---

    public static final String EDGE_FOLLOWS = "FOLLOWS";
    public static final String EDGE_FRIEND_OF = "FRIEND_OF";
    public static final String EDGE_MEMBER_OF = "MEMBER_OF";
    public static final String EDGE_COMPLETED = "COMPLETED";
    public static final String EDGE_PARTICIPATES_IN = "PARTICIPATES_IN";
    public static final String EDGE_TARGETS = "TARGETS";
    public static final String EDGE_ALTERNATIVE_TO = "ALTERNATIVE_TO";

    // --- Common property keys ---

    public static final String PROP_ID = "id";
    public static final String PROP_NAME = "name";
    public static final String PROP_MUSCLE_GROUPS = "muscleGroups";
    public static final String PROP_WEIGHT = "weight";
    public static final String PROP_REPS = "reps";
    public static final String PROP_DATE = "date";
}
