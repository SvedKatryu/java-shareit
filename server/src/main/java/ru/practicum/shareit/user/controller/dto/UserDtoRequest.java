package ru.practicum.shareit.user.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.markers.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;


@Data
@AllArgsConstructor
@Builder
public class UserDtoRequest {
    @Null(groups = Marker.OnCreate.class)
    private Long id;
    @NotBlank(groups = Marker.OnCreate.class)
    private String name;
    @Email(groups = Marker.OnCreate.class)
    @NotBlank(groups = Marker.OnCreate.class)
    private String email;
}