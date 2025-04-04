package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.rest.dto.UserDtos.NewUserRequest;
import com.example.demo.rest.dto.UserDtos.UserDto;

public interface UserService {
    User findById(Long id);


    Long addNewUser(NewUserRequest request);
    void deleteById(Long id);
    UserDto findByUsername(String username);
    void save(User author);

}
