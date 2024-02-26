package ru.practicum.shareit.item.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
public class ItemDtoRequest {
    private final String id;
    @NotBlank
    private final String name;
    @NotBlank
    private final String description;
    @NotNull
    private final Boolean available;
}
