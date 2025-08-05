package com.example.bankcards.util;

import com.example.bankcards.entity.Users;
import com.example.bankcards.service.UsersDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UsersValidator implements Validator {

    private final UsersDetailsService usersDetailsService;

    public UsersValidator(UsersDetailsService usersDetailsService) {
        this.usersDetailsService = usersDetailsService;
    }


    public boolean supports(Class<?> aClass) {
        return Users.class.equals(aClass);
    }

    public void validate(Object o, Errors errors) {
        Users users = (Users) o;

        try {
            usersDetailsService.loadUserByUsername(users.getUsername());
        } catch (UsernameNotFoundException ignored) {
            return; // все ок, пользователь не найден
        }

        errors.rejectValue("username", "", "Человек с таким именем пользователя уже существует");
    }
}
