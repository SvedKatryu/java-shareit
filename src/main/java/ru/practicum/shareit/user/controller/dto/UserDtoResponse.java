package ru.practicum.shareit.user.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDtoResponse {
    private Long id;
    private final String name;
    private final String email;
}
