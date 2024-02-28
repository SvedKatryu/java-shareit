package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDtoResponse addNewItem(Long userId, ItemDtoRequest request);

    ItemDtoResponse update(Long userId, Long itemId, ItemDtoRequest request);

    ItemDtoResponse getItemById(Long itemId);

    List<Item> getItemsByUserId(Long userId);

    List<Item> findItemsByText(String text);
}
