package com.example.demo.rest.dto.UserDtos;



import lombok.Builder;

import java.util.Objects;
@Builder
public class UserDto {
    private String username;
    private String role;

    // Конструктор для всех полей
    public UserDto(String username, String role) {
        this.username = username;
        this.role = role;
    }

    // Пустой конструктор, если он необходим
    public UserDto() {
    }



    // Геттеры и сеттеры
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Метод toString()
    @Override
    public String toString() {
        return "UserDto{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    // Метод equals() для корректного сравнения объектов
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDto userDto = (UserDto) o;
        return Objects.equals(username, userDto.username) && Objects.equals(role, userDto.role);
    }

    // Метод hashCode()
    @Override
    public int hashCode() {
        return Objects.hash(username, role);
    }
}

