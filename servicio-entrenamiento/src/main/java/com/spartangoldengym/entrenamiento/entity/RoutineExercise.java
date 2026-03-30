package com.spartangoldengym.entrenamiento.entity;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "routine_exercises")
public class RoutineExercise {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    private Routine routine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    @Column(nullable = false)
    private Integer sets;

    @Column(length = 50)
    private String reps;

    @Column(name = "rest_seconds")
    private Integer restSeconds;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public RoutineExercise() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Routine getRoutine() { return routine; }
    public void setRoutine(Routine routine) { this.routine = routine; }
    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }
    public Integer getSets() { return sets; }
    public void setSets(Integer sets) { this.sets = sets; }
    public String getReps() { return reps; }
    public void setReps(String reps) { this.reps = reps; }
    public Integer getRestSeconds() { return restSeconds; }
    public void setRestSeconds(Integer restSeconds) { this.restSeconds = restSeconds; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
