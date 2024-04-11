package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private static final String DEFAULT_PAGE_SIZE = "10";

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDtoResponse addNewItemRequest(@RequestHeader("X-Sharer-User-id") Long userId,
                                                    @RequestBody ItemRequestDtoRequest itemRequestDtoRequest) {
        log.info("Добавлен новый запрос вещи");
        return itemRequestService.addNewItemRequest(userId, itemRequestDtoRequest);
    }

    @GetMapping
    public List<ItemRequestDtoResponse> getAllItemRequestsFromUser(@RequestHeader("X-Sharer-User-id") Long userId) {
        log.info("Получен запрос на получение списка запросов с ID{}", userId);
        return itemRequestService.getAllItemRequestsFromUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDtoResponse> getAvailableItemRequests(@RequestHeader("X-Sharer-User-id") Long userId,
                                                                 @RequestParam(defaultValue = "0") Long from,
                                                                 @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size) {
        log.info("Получение списка запросов, начиная с '{}', по '{}' элемента на странице.", from, size);
        return itemRequestService.getAvailableItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoResponse getItemRequestById(@RequestHeader("X-Sharer-User-id") Long userId,
                                                     @PathVariable Long requestId) {
        log.info("Получение запроса с id '{}'.", requestId);
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}