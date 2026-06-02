package com.services.resourceallocation.dto;

public class UserProfileDTO {
    private Integer id;
    private String fullName;
    private String email;
    private String username;
    private String phone;
    private String role;
    private String department;
    private String dob;
    // private String createdAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    // public String getCreatedAt() { return createdAt; }
    // public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}