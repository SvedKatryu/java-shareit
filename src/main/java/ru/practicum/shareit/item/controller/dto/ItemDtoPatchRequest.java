package ru.practicum.shareit.item.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemDtoPatchRequest {
    private final String id;
    private final String name;
    private final String description;
    private final Boolean available;
}
