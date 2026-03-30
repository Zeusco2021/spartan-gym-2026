package com.spartangoldengym.entrenamiento.entity;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "routines")
public class Routine {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private TrainingPlan plan;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public Routine() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public TrainingPlan getPlan() { return plan; }
    public void setPlan(TrainingPlan plan) { this.plan = plan; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
