package com.smarterevententrysystem.model;

public class Admin {
    private int adminId;
    private String username;
    private String fullName;

    public Admin(int adminId, String username, String fullName) {
        this.adminId = adminId;
        this.username = username;
        this.fullName = fullName;
    }

    public int getAdminId() { return adminId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
}
