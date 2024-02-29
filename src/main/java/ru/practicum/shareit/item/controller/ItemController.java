package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.model.Item;
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
public class ItemController {

    private final ItemServiceImpl itemService;

    @PostMapping
    public ItemDtoResponse add(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @RequestBody @Validated({Marker.OnCreate.class}) ItemDtoRequest request) {
        return itemService.addNewItem(userId, request);
    }

    @GetMapping("/{itemId}")
    public ItemDtoResponse getItemById(@RequestHeader("X-Sharer-User-Id") long userId,
                                       @Valid @PathVariable long itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDtoResponse> get(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getItemsByUserId(userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDtoResponse update(@RequestHeader("X-Sharer-User-Id") long userId,
                                  @Positive @PathVariable long itemId,
                                  @RequestBody @Validated({Marker.OnUpdate.class}) ItemDtoRequest request) {
        return itemService.update(userId, itemId, request);
    }

    @GetMapping("/search")
    public List<ItemDtoResponse> findItemsByText(@RequestHeader("X-Sharer-User-Id") long userId,
                                      @RequestParam(name = "text") String text) {
        if (Objects.equals(text, "")) {
            return Collections.emptyList();
        }
        return itemService.findItemsByText(text);
    }
}
