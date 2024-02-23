package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {

    public static ItemDtoRequest toItemDto(Item item) {
        return new ItemDtoRequest(
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

}
