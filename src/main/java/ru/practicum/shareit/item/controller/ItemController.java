package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.controller.dto.CommentDto;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.markers.Marker;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {

    private final ItemServiceImpl itemService;

    @PostMapping
    public ItemDtoResponse add(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @RequestBody @Validated({Marker.OnCreate.class}) ItemDtoRequest request) {
        log.info("Получен запрос на добавление вещи {}", request);
        return itemService.addNewItem(userId, request);
    }

    @GetMapping("/{itemId}")
    public ItemDtoResponse getItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @Valid @PathVariable long itemId) {
        log.info("Получен запрос на получение вещи ID{}", itemId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoResponse> get(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Получен запрос на получение списка вещей пользователя с ID{}", userId);
        return itemService.getItemsByUserId(userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoResponse update(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @Positive @PathVariable long itemId,
                                  @RequestBody @Validated({Marker.OnUpdate.class}) ItemDtoRequest request) {
        log.info("Получен запрос на обновление вещи ID{}", itemId);
        return itemService.update(userId, itemId, request);
    }

    @GetMapping("/search")
    public List<ItemDtoResponse> findItemsByText(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @RequestParam(name = "text") String text) {
        if (Objects.equals(text, "")) {
            return Collections.emptyList();
        }
        log.info("Получен запрос на получение списка вещей по поиску {}", text);
        return itemService.findItemsByText(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable @Positive long itemId,
                                 @RequestBody @Validated CommentDto commentDto) {
        log.info("Получен запрос на добавление комметнария к вещи с ID{}", itemId);
        return itemService.addComment(userId, itemId, commentDto);
    }
}
