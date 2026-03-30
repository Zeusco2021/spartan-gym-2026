/**
 * Neptune Gremlin initialization script.
 *
 * Sets up the graph schema for Spartan Golden Gym:
 *   - Vertex labels: User, Exercise, MuscleGroup, Challenge, Group
 *   - Edge labels: FOLLOWS, FRIEND_OF, MEMBER_OF, COMPLETED, PARTICIPATES_IN, TARGETS, ALTERNATIVE_TO
 *
 * Usage:
 *   Connect to the Neptune Gremlin endpoint and execute this script,
 *   or run via the Gremlin console:
 *     :remote connect tinkerpop.server conf/neptune.yaml
 *     :remote console
 *     :load init-graph-schema.groovy
 *
 * Validates: Requirements 12.4, 12.5
 */

// ============================================================
// Seed vertex labels with a sentinel vertex per label.
// Neptune infers schema from data, so we insert one vertex
// per label to establish the label in the graph.
// ============================================================

// --- User vertex ---
if (g.V().hasLabel('User').has('id', 'seed-user').hasNext() == false) {
    g.addV('User')
        .property('id', 'seed-user')
        .property('name', '__seed__')
        .next()
    println 'Created seed User vertex'
}

// --- Exercise vertex ---
if (g.V().hasLabel('Exercise').has('id', 'seed-exercise').hasNext() == false) {
    g.addV('Exercise')
        .property('id', 'seed-exercise')
        .property('name', '__seed__')
        .property('muscleGroups', '[]')
        .next()
    println 'Created seed Exercise vertex'
}

// --- MuscleGroup vertex ---
if (g.V().hasLabel('MuscleGroup').has('id', 'seed-muscle-group').hasNext() == false) {
    g.addV('MuscleGroup')
        .property('id', 'seed-muscle-group')
        .property('name', '__seed__')
        .next()
    println 'Created seed MuscleGroup vertex'
}

// --- Challenge vertex ---
if (g.V().hasLabel('Challenge').has('id', 'seed-challenge').hasNext() == false) {
    g.addV('Challenge')
        .property('id', 'seed-challenge')
        .property('name', '__seed__')
        .next()
    println 'Created seed Challenge vertex'
}

// --- Group vertex ---
if (g.V().hasLabel('Group').has('id', 'seed-group').hasNext() == false) {
    g.addV('Group')
        .property('id', 'seed-group')
        .property('name', '__seed__')
        .next()
    println 'Created seed Group vertex'
}

// ============================================================
// Seed edge labels with sentinel edges between seed vertices.
// ============================================================

def seedUser      = g.V().hasLabel('User').has('id', 'seed-user').next()
def seedExercise  = g.V().hasLabel('Exercise').has('id', 'seed-exercise').next()
def seedMuscle    = g.V().hasLabel('MuscleGroup').has('id', 'seed-muscle-group').next()
def seedChallenge = g.V().hasLabel('Challenge').has('id', 'seed-challenge').next()
def seedGroup     = g.V().hasLabel('Group').has('id', 'seed-group').next()

// User -[FOLLOWS]-> User
if (g.V(seedUser).outE('FOLLOWS').hasNext() == false) {
    g.V(seedUser).addE('FOLLOWS').to(seedUser).next()
    println 'Created seed FOLLOWS edge'
}

// User -[FRIEND_OF]-> User
if (g.V(seedUser).outE('FRIEND_OF').hasNext() == false) {
    g.V(seedUser).addE('FRIEND_OF').to(seedUser).next()
    println 'Created seed FRIEND_OF edge'
}

// User -[MEMBER_OF]-> Group
if (g.V(seedUser).outE('MEMBER_OF').hasNext() == false) {
    g.V(seedUser).addE('MEMBER_OF').to(seedGroup).next()
    println 'Created seed MEMBER_OF edge'
}

// User -[COMPLETED]-> Exercise (with properties: weight, reps, date)
if (g.V(seedUser).outE('COMPLETED').hasNext() == false) {
    g.V(seedUser).addE('COMPLETED').to(seedExercise)
        .property('weight', 0.0)
        .property('reps', 0)
        .property('date', '1970-01-01')
        .next()
    println 'Created seed COMPLETED edge'
}

// User -[PARTICIPATES_IN]-> Challenge
if (g.V(seedUser).outE('PARTICIPATES_IN').hasNext() == false) {
    g.V(seedUser).addE('PARTICIPATES_IN').to(seedChallenge).next()
    println 'Created seed PARTICIPATES_IN edge'
}

// Exercise -[TARGETS]-> MuscleGroup
if (g.V(seedExercise).outE('TARGETS').hasNext() == false) {
    g.V(seedExercise).addE('TARGETS').to(seedMuscle).next()
    println 'Created seed TARGETS edge'
}

// Exercise -[ALTERNATIVE_TO]-> Exercise
if (g.V(seedExercise).outE('ALTERNATIVE_TO').hasNext() == false) {
    g.V(seedExercise).addE('ALTERNATIVE_TO').to(seedExercise).next()
    println 'Created seed ALTERNATIVE_TO edge'
}

println '--- Neptune graph schema initialization complete ---'
