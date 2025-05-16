package com.example.demo.service.impl;

import com.example.demo.config.SecurityUtils;
import com.example.demo.domain.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.exception.UserNotFoundExceptionByName;
import com.example.demo.repos.UserRepo;
import com.example.demo.rest.dto.UserDtos.NewUserRequest;
import com.example.demo.rest.dto.UserDtos.UserDto;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
@Service
public class UserServiceImpl implements UserService {
    private UserRepo userRepo;
    private  final SecurityUtils securityUtils;

    @Autowired
    public UserServiceImpl(UserRepo userRepo, SecurityUtils securityUtils)
 {
        this.userRepo = userRepo;
        this.securityUtils=securityUtils;
    }

    public  Long  getCurrentUserId(Authentication authentication) {
        Long userId = null;

        // Сначала проверяем обычную аутентификацию
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            userId = userDetails.getId();
        }
        // Если обычной аутентификации нет, пытаемся получить ID из SecurityUtils
        else {
            userId = securityUtils.getCurrentUserId();
        }
        return userId;
    }

    public void save(User user) {
        userRepo.saveAndFlush(user);
    }
    @Override
    public User findById(Long id) {
        Optional<User> element = userRepo.findById(id);
            if (element.isEmpty())
        {
            throw new UserNotFoundException("Пользователь с таким id не существует");
        }

        return element.get();
    }



        @Override
        public User findByUsername(String username) {


            return userRepo.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }




    @Transactional
    @Override
    public Long addNewUser(NewUserRequest request) {
        System.out.println("Добавляем нового пользователя: " + request);

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        userRepo.saveAndFlush(user);
        return user.getId();


    }
    @Transactional
    @Override
    public void  deleteById(Long id) {
        userRepo.deleteById(id);
    }





}
