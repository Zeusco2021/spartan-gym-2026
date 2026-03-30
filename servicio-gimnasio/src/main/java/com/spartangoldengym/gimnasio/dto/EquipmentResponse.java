package com.spartangoldengym.gimnasio.dto;

import java.util.UUID;

public class EquipmentResponse {

    private UUID id;
    private UUID gymId;
    private String name;
    private String category;
    private Integer quantity;
    private String status;

    public EquipmentResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getGymId() { return gymId; }
    public void setGymId(UUID gymId) { this.gymId = gymId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
