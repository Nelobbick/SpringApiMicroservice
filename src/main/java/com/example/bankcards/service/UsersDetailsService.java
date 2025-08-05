package com.example.bankcards.service;

import com.example.bankcards.dto.CreateUserDTO;
import com.example.bankcards.dto.UpdateUserDTO;
import com.example.bankcards.dto.UsersDTO;
import com.example.bankcards.entity.Users;
import com.example.bankcards.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsersDetailsService implements UserDetailsService {

    private final UsersRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public UsersDetailsService(UsersRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Создание нового пользователя
     */
    public Users createUser(CreateUserDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        return userRepository.save(user);
    }

    /**
     * Получение пользователя по ID
     */
    public Optional<Users> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Получение пользователя по имени
     */
    public Users getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
    }

    /**
     * Получение всех пользователей
     */
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Обновление пользователя
     */
    public Users updateUser(Long id, UpdateUserDTO request) {
        Users user = getUserById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!user.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        user.setUsername(request.getUsername());
        user.setRole(request.getRole().toUpperCase());

        return userRepository.save(user);
    }

    /**
     * Удаление пользователя
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Пользователь не найден");
        }
        userRepository.deleteById(id);
    }

    /**
     * Реализация UserDetailsService для Spring Security
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }

    /**
     * Аутентификация пользователя
     */
    public boolean authenticateUser(String username, String password) {
        Optional<Users> userOptional = userRepository.findByUsername(username);
        return userOptional.filter(user -> passwordEncoder.matches(password, user.getPassword())).isPresent();
    }
}
