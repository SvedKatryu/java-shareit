package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.controller.dto.CommentDto;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.controller.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto addNewItem(Long userId, ItemDtoRequest request);

    ItemDtoResponse update(Long userId, Long itemId, ItemDtoRequest request);

    ItemDtoResponse getItemById(Long userId, Long itemId);

    List<ItemDtoResponse> getItemsByUserId(Long userId, Long from, Integer size);

    List<ItemDtoResponse> findItemsByText(String text, Long from, Integer size);

    CommentDto addComment(Long bookerId, Long itemId, CommentDto commentDto);
}
