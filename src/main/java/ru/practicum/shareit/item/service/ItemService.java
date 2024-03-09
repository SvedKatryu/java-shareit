package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.controller.dto.CommentDto;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;

import java.util.List;

public interface ItemService {

    ItemDtoResponse addNewItem(Long userId, ItemDtoRequest request);

    ItemDtoResponse update(Long userId, Long itemId, ItemDtoRequest request);

    ItemDtoResponse getItemById(Long userId, Long itemId);

    List<ItemDtoResponse> getItemsByUserId(Long userId);

    List<ItemDtoResponse> findItemsByText(String text);

    CommentDto addComment(Long bookerId, Long itemId, CommentDto commentDto);
}
