package com.example.demo.service.impl;

import com.example.demo.domain.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.exception.UserNotFoundExceptionByName;
import com.example.demo.repos.UserRepo;
import com.example.demo.rest.dto.UserDtos.NewUserRequest;
import com.example.demo.rest.dto.UserDtos.UserDto;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
@Service
public class UserServiceImpl implements UserService {
    private UserRepo userRepo;
    @Autowired
    public UserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
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
        public UserDto findByUsername(String username) {
            Optional<User> element = userRepo.findByUsername(username);
            if (element.isEmpty()) {
                throw new UserNotFoundExceptionByName("Пользователь с таким именем не существует");
            }
             User user= element.get();
            return new UserDto(user.getUsername(),user.getRole(),user.getIsOnline());
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
