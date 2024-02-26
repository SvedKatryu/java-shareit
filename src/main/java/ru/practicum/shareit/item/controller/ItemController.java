package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemServiceImpl itemService;

    @PostMapping
    @Validated({Marker.OnCreate.class})
    public ItemDtoResponse add(@RequestHeader("X-Sharer-User-Id") Long userId,
                                @RequestBody @Valid ItemDtoRequest request) {
        return itemService.addNewItem(userId, request);
    }

    @GetMapping("/{itemId}")
    public Item getItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                            @Valid @PathVariable long itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<Item> get(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getItemsByUserId(userId);
    }

    @PatchMapping("/{itemId}")
    @Validated({Marker.OnUpdate.class})
    public ItemDtoResponse update(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @Valid @PathVariable long itemId,
                                  @RequestBody @Valid ItemDtoRequest request) {
        return itemService.update(userId, itemId, request);
    }

    @GetMapping("/search")
    public List<Item> findItemsByText(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @RequestParam(name = "text") String text) {
        return itemService.findItemsByText(text);
    }
}
