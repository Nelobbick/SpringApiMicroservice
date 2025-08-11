package com.example.bankcards.security;

import com.example.bankcards.entity.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class UsersDetailsImpl implements UserDetails {


    private Long id;
    private String username;
    private String password;
    private String role;
    private final List<GrantedAuthority> authorities;

    public UsersDetailsImpl(Long id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority(
                        role.startsWith("ROLE_") ? role : "ROLE_" + role
                )
        );
    }

    public static UsersDetailsImpl build(Users users) {
        if (users == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (users.getRole() == null || users.getRole().isEmpty()) {
            throw new IllegalStateException("User must have a role assigned");
        }

        return new UsersDetailsImpl(
                users.getId(),
                users.getUsername(),
                users.getPassword(),
                users.getRole()
        );
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
