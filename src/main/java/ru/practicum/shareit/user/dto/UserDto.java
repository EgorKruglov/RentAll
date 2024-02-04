package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Positive;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDto {
    @Positive
    private Integer id;
    private String name;
    @Email(message = "Некорректный email адрес")
    private String email;

    public UserDto(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
