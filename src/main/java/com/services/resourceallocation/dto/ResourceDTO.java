package com.services.resourceallocation.dto;

public class ResourceDTO {

    private Integer resourceId;
    private String name;
    private String type;
    private String department;
    private Integer capacity;
    private String location;
    private String amenities;

    public ResourceDTO() {}

    public ResourceDTO(Integer resourceId, String name, String type,
                       String department, Integer capacity,
                       String location, String amenities) {
        this.resourceId  = resourceId;
        this.name        = name;
        this.type        = type;
        this.department  = department;
        this.capacity    = capacity;
        this.location    = location;
        this.amenities   = amenities;
    }

    // ── Getters & Setters ──────────────────────────

    public Integer getResourceId() { return resourceId; }
    public void setResourceId(Integer resourceId) { this.resourceId = resourceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
}