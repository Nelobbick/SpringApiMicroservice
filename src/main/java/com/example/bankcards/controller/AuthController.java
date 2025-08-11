package com.example.bankcards.controller;

import com.example.bankcards.dto.UsersDTO;
import com.example.bankcards.entity.Users;
import com.example.bankcards.repository.UsersRepository;
import com.example.bankcards.security.JWTCore;
import com.example.bankcards.dto.AuthenticationDTO;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final JWTCore jwtCore;
    private final AuthenticationManager authenticationManager;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;


    public AuthController(JWTCore jwtCore, AuthenticationManager authenticationManager, UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.jwtCore = jwtCore;
        this.authenticationManager = authenticationManager;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registration")
    public ResponseEntity<?> performRegistration(@RequestBody @Valid UsersDTO usersDTO,
                                                 BindingResult bindingResult) {
        log.info("Registration attempt for username: {}", usersDTO.getUsername());
        // Проверка ошибок валидации
        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();
            bindingResult.getFieldErrors().forEach(error ->
                    errorMsg.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
            );
            log.warn("Validation errors during registration: {}", errorMsg.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMsg.toString());
        }
        // Проверка существования пользователя
        if (usersRepository.existsByUsername(usersDTO.getUsername())) {
            log.warn("Registration failed: username {} already exists", usersDTO.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This username already used");
        }
        try {
            // Создание нового пользователя
            Users users = new Users();
            users.setUsername(usersDTO.getUsername());
            users.setPassword(passwordEncoder.encode(usersDTO.getPassword()));
            users.setRole("ROLE_USER"); // По умолчанию обычный пользователь

            usersRepository.save(users);

            log.info("User {} registered successfully", users.getUsername());
            return ResponseEntity.ok("Success registration");
        } catch (Exception e) {
            log.error("Error during registration for user {}", usersDTO.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> performLogin(@RequestBody AuthenticationDTO authenticationDTO) {
        log.info("Login attempt for username: {}", authenticationDTO.getUsername());

        try {
            // Попытка аутентификации
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationDTO.getUsername(),
                            authenticationDTO.getPassword()
                    )
            );
            // Установка аутентификации в контекст безопасности
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Генерация JWT токена
            String jwt = jwtCore.generateToken(authentication);
            log.info("User {} logged in successfully", authenticationDTO.getUsername());
            return ResponseEntity.ok(jwt);
        } catch (BadCredentialsException e) {
            log.warn("Login failed for user {}: Invalid credentials", authenticationDTO.getUsername());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Error during login for user {}", authenticationDTO.getUsername(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }
}