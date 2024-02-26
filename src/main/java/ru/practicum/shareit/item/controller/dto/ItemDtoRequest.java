package ru.practicum.shareit.item.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.practicum.shareit.item.controller.Marker;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Getter
@AllArgsConstructor
public class ItemDtoRequest {
    @Null(groups = Marker.OnCreate.class)
    private final String id;
    @NotBlank(groups = Marker.OnCreate.class)
    private final String name;
    @NotBlank(groups = Marker.OnCreate.class)
    private final String description;
    @NotNull(groups = Marker.OnCreate.class)
    private final Boolean available;
}
