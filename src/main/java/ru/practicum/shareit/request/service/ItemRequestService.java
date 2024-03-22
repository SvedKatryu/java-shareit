package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDtoResponse addNewItemRequest(Long userId, ItemRequestDtoRequest itemRequestDtoRequest);

    List<ItemRequestDtoResponse> getAllItemRequestsFromUser(Long userId);

    List<ItemRequestDtoResponse> getAvailableItemRequests(Long userId, Long from, Integer size);

    ItemRequestDtoResponse getItemRequestById(Long userId, Long requestId);
}
