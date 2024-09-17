package ru.practicum.shareit.user.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@AllArgsConstructor
@Builder
public class UserDtoRequest {
    private Long id;
    private String name;
    private String email;
}