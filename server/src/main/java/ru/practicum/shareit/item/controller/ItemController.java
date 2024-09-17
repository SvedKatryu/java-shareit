package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.controller.dto.CommentDto;
import ru.practicum.shareit.item.controller.dto.ItemDto;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private static final String DEFAULT_PAGE_SIZE = "10";
    private final ItemServiceImpl itemService;

    @PostMapping
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") Long userId,
                       @RequestBody ItemDtoRequest request) {
        log.info("Получен запрос на добавление вещи {}", request);
        return itemService.addNewItem(userId, request);
    }

    @GetMapping("/{itemId}")
    public ItemDtoResponse getItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @PathVariable long itemId) {
        log.info("Получен запрос на получение вещи ID{}", itemId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemDtoResponse> get(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @RequestParam(defaultValue = "0") Long from,
                                     @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size) {
        log.info("Получен запрос на получение списка вещей пользователя с ID{}", userId);
        return itemService.getItemsByUserId(userId, from, size);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoResponse update(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @PathVariable long itemId,
                                  @RequestBody ItemDtoRequest request) {
        log.info("Получен запрос на обновление вещи ID{}", itemId);
        return itemService.update(userId, itemId, request);
    }

    @GetMapping("/search")
    public List<ItemDtoResponse> findItemsByText(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @RequestParam(name = "text") String text,
                                                 @RequestParam(defaultValue = "0") Long from,
                                                 @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size) {
        if (Objects.equals(text, "")) {
            return Collections.emptyList();
        }
        log.info("Получен запрос на получение списка вещей по поиску {}", text);
        return itemService.findItemsByText(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId,
                                 @RequestBody CommentDto commentDto) {
        log.info("Получен запрос на добавление комметнария к вещи с ID{}", itemId);
        return itemService.addComment(userId, itemId, commentDto);
    }
}
