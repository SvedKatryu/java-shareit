package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemServiceImpl itemService;

    @PostMapping
    public ItemDtoResponse add(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @Validated @RequestBody ItemDtoRequest request) {
        return itemService.addNewItem(userId, request);
    }

    @GetMapping("/{itemId}")
    public Item getItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                            @PathVariable long itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<Item> get(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getItemsByUserId(userId);
    }

    @PatchMapping("/{itemId}")
    public Item update(@RequestHeader("X-Sharer-User-Id") long userId,
                       @Validated @PathVariable long itemId,
                       @Validated @RequestBody ItemDtoRequest request) {
        return itemService.update(userId, itemId, request);
    }

    @GetMapping("/search")
    public List<Item> findItemsByText(@RequestHeader("X-Sharer-User-Id") long userId,
                           @RequestParam(name = "text") String text) {
        return itemService.findItemsByText(text);
    }
}
