package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.controller.dto.ItemDto;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    Item toItem(ItemDtoRequest request);

   ItemDto toItemWithRequest(Item item);

    ItemDtoResponse toResponse(Item item);
}
