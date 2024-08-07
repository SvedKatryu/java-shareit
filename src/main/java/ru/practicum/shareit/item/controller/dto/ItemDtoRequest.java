package ru.practicum.shareit.item.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.markers.Marker;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Data
@AllArgsConstructor
@Builder
public class ItemDtoRequest {
    @Null(groups = Marker.OnCreate.class)
    private final Long id;
    @NotBlank(groups = Marker.OnCreate.class)
    private final String name;
    @NotBlank(groups = Marker.OnCreate.class)
    private final String description;
    @NotNull(groups = Marker.OnCreate.class)
    private final Boolean available;
    private Long requestId;
}
