package ru.practicum.shareit.user.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.practicum.shareit.item.controller.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

@Getter
@AllArgsConstructor
public class UserDtoRequest {
    @Null(groups = Marker.OnCreate.class)
    private final Long id;
    @NotBlank(groups = Marker.OnCreate.class)
    private final String name;
    @Email(groups = Marker.OnCreate.class)
    @NotBlank(groups = Marker.OnCreate.class)
    private final String email;
}