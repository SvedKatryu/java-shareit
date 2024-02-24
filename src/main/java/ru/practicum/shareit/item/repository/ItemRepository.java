package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

public interface ItemRepository {
    void create(Long userId, Item item);

    Item getItemById(Long itemId);

    List<Item> getItemsByUserId(Long userId);

    Item update(Long userId, Long itemId, Item item);


    List<Item> findItemsByText (String text);
}
