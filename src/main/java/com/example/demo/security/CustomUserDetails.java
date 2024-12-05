package com.example.demo.security;

import com.example.demo.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public  Long getId(){
        return user.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }


    @Override
    public boolean isAccountNonExpired() {
        return true; // Или ваша логика проверки
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Или ваша логика проверки
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Или ваша логика проверки
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

};
