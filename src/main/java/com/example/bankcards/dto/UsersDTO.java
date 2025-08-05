package com.example.bankcards.dto;

import jakarta.validation.constraints.NotEmpty;

public class UsersDTO {
    @NotEmpty(message = "имя не должно быть пустым")
    private String username;
    @NotEmpty
    private String password;

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
