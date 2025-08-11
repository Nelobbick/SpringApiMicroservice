package com.example.bankcards.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;


public class CreateUserDTO {
    @NotEmpty(message = "имя не должно быть пустым")
    private String username;
    @NotEmpty(message = "Пароль не должен быть пустым")
    private String password;
    @Pattern(regexp = "^(ADMIN|USER)$",
            message = "Роль должна быть ADMIN или USER")
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

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
