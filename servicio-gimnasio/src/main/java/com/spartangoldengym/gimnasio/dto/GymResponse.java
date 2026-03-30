package com.spartangoldengym.gimnasio.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class GymResponse {

    private UUID id;
    private UUID chainId;
    private String chainName;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String operatingHours;
    private Integer maxCapacity;
    private Instant createdAt;

    public GymResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getChainId() { return chainId; }
    public void setChainId(UUID chainId) { this.chainId = chainId; }
    public String getChainName() { return chainName; }
    public void setChainName(String chainName) { this.chainName = chainName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getOperatingHours() { return operatingHours; }
    public void setOperatingHours(String operatingHours) { this.operatingHours = operatingHours; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
