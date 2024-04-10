package ru.practicum.shareit.item.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ItemDtoRequest {
    private final Long id;
    private final String name;
    private final String description;
    private final Boolean available;
    private Long requestId;
}
