package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.pageable.OffsetPageRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.JpaItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor

@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final JpaItemRequestRepository itemRequestRepository;
    private final JpaUserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    public ItemRequestDtoResponse addNewItemRequest(Long userId, ItemRequestDtoRequest itemRequestDtoRequest) {
        User requester = getUserIfPresent(userId);
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDtoRequest);
        itemRequest.setRequester(requester);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.toResponse(savedRequest);
    }

    @Override
    public List<ItemRequestDtoResponse> getAllItemRequestsFromUser(Long userId) {
        getUserIfPresent(userId);
        List<ItemRequest> requests = itemRequestRepository.findRequestsFromUser(userId);
        List<ItemRequestDtoResponse> responseItems = itemRequestMapper.toDtoResponseList(requests);
        addRequestIdToItem(responseItems);
        return responseItems;
    }

    @Override
    public List<ItemRequestDtoResponse> getAvailableItemRequests(Long userId, Long from, Integer size) {
        getUserIfPresent(userId);
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        Page<ItemRequest> requests = itemRequestRepository.findAvailableRequests(userId, pageRequest);
        List<ItemRequestDtoResponse> responseItems = itemRequestMapper.toDtoResponseList(requests.getContent());
        addRequestIdToItem(responseItems);
        return responseItems;
    }

    @Override
    public ItemRequestDtoResponse getItemRequestById(Long userId, Long requestId) {
        getUserIfPresent(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Запрос с ID%d не найден", requestId)));
        ItemRequestDtoResponse itemRequestDtoResponse = itemRequestMapper.toResponse(itemRequest);
        itemRequestDtoResponse.getItems().forEach(item -> {
            item.setRequestId(itemRequestDtoResponse.getId());
        });
        return itemRequestDtoResponse;
    }

    private void addRequestIdToItem(List<ItemRequestDtoResponse> responseItems) {
        responseItems.forEach(res -> {
            res.getItems().forEach(item -> {
                item.setRequestId(res.getId());
            });
        });
    }

    private User getUserIfPresent(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID%d не найден", userId)));
    }
}