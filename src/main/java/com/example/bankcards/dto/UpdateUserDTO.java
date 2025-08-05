package com.example.bankcards.dto;

import jakarta.validation.constraints.NotEmpty;

public class UpdateUserDTO {
    @NotEmpty(message = "имя не должно быть пустым")
    private String username;
    @NotEmpty
    private String password;
    @NotEmpty
    private String role;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
