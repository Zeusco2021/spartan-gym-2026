package com.spartangoldengym.gimnasio.entity;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "gym_equipment")
public class GymEquipment {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 20)
    private String status = "available";

    public GymEquipment() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Gym getGym() { return gym; }
    public void setGym(Gym gym) { this.gym = gym; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
