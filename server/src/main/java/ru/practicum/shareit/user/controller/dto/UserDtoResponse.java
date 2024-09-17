package ru.practicum.shareit.user.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@AllArgsConstructor
@Builder
@Data
public class UserDtoResponse {
    private Long id;
    private String name;
    private String email;
}
