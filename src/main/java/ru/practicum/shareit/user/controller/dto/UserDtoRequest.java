package ru.practicum.shareit.user.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
public class UserDtoRequest {
    private final long id;
    @NotBlank
    private final String name;
    @Email
    @NotBlank
    private final String email;
}