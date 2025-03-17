package com.example.demo.config;

import com.example.demo.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            // Если это тестовая аутентификация с ID пользователя в details
            if (authentication.getDetails() instanceof Long) {
                return (Long) authentication.getDetails();
            }

            // Если это обычная аутентификация с CustomUserDetails
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                return ((CustomUserDetails) authentication.getPrincipal()).getId();
            }
        }

        return null; // Если пользователь не аутентифицирован
    }
}