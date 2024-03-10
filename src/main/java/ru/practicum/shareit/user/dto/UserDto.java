package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Positive;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDto {
    @Positive
    private Integer id;
    private String name;
    @Email(message = "Некорректный email адрес")
    private String email;
}
