package com.spartangoldengym.common.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NeptuneSchemaTest {

    @Test
    void allFiveVertexLabelsAreDefined() {
        Set<String> labels = new HashSet<>(Arrays.asList(
                NeptuneSchema.VERTEX_USER,
                NeptuneSchema.VERTEX_EXERCISE,
                NeptuneSchema.VERTEX_MUSCLE_GROUP,
                NeptuneSchema.VERTEX_CHALLENGE,
                NeptuneSchema.VERTEX_GROUP
        ));
        assertEquals(5, labels.size(), "Should define exactly 5 unique vertex labels");
        assertTrue(labels.contains("User"));
        assertTrue(labels.contains("Exercise"));
        assertTrue(labels.contains("MuscleGroup"));
        assertTrue(labels.contains("Challenge"));
        assertTrue(labels.contains("Group"));
    }

    @Test
    void allSevenEdgeLabelsAreDefined() {
        Set<String> edges = new HashSet<>(Arrays.asList(
                NeptuneSchema.EDGE_FOLLOWS,
                NeptuneSchema.EDGE_FRIEND_OF,
                NeptuneSchema.EDGE_MEMBER_OF,
                NeptuneSchema.EDGE_COMPLETED,
                NeptuneSchema.EDGE_PARTICIPATES_IN,
                NeptuneSchema.EDGE_TARGETS,
                NeptuneSchema.EDGE_ALTERNATIVE_TO
        ));
        assertEquals(7, edges.size(), "Should define exactly 7 unique edge labels");
        assertTrue(edges.contains("FOLLOWS"));
        assertTrue(edges.contains("FRIEND_OF"));
        assertTrue(edges.contains("MEMBER_OF"));
        assertTrue(edges.contains("COMPLETED"));
        assertTrue(edges.contains("PARTICIPATES_IN"));
        assertTrue(edges.contains("TARGETS"));
        assertTrue(edges.contains("ALTERNATIVE_TO"));
    }

    @Test
    void completedEdgePropertyKeysAreDefined() {
        // COMPLETED edge carries weight, reps, date per design spec
        assertEquals("weight", NeptuneSchema.PROP_WEIGHT);
        assertEquals("reps", NeptuneSchema.PROP_REPS);
        assertEquals("date", NeptuneSchema.PROP_DATE);
    }

    @Test
    void commonPropertyKeysAreDefined() {
        assertEquals("id", NeptuneSchema.PROP_ID);
        assertEquals("name", NeptuneSchema.PROP_NAME);
        assertEquals("muscleGroups", NeptuneSchema.PROP_MUSCLE_GROUPS);
    }
}
