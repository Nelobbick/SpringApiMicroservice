package com.example.bankcards.util;

import com.example.bankcards.entity.Users;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UsersValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Users.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Users user = (Users) target;

        // Проверка username
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            errors.rejectValue("username", "required", "Username is required");
        } else if (user.getUsername().length() < 3 || user.getUsername().length() > 50) {
            errors.rejectValue("username", "size", "Username must be between 3 and 50 characters");
        }

        // Проверка password
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            errors.rejectValue("password", "required", "Password is required");
        } else if (user.getPassword().length() < 8 || user.getPassword().length() > 100) {
            errors.rejectValue("password", "size", "Password must be between 8 and 100 characters");
        }

        // Проверка role
        if (user.getRole() == null || user.getRole().isEmpty()) {
            errors.rejectValue("role", "required", "Role is required");
        } else if (!isValidRole(user.getRole())) {
            errors.rejectValue("role", "invalid", "Invalid role. Allowed values: ADMIN, USER");
        }
    }

    private boolean isValidRole(String role) {
        return "ROLE_ADMIN".equals(role) || "ROLE_USER".equals(role);
    }
}