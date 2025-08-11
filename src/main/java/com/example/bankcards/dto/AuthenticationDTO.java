package com.example.bankcards.dto;

import jakarta.validation.constraints.NotEmpty;


public class AuthenticationDTO {
    @NotEmpty(message = "имя не должно быть пустым")
    private String username;
    @NotEmpty(message = "пароль не должен быть пустым")
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}