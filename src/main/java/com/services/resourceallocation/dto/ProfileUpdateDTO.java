package com.services.resourceallocation.dto;

public class ProfileUpdateDTO {
    private String fullName;
    private String email;
    private String phone;
    private String department;
    private String dob;

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
}