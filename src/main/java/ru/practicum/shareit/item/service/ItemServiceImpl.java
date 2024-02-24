package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepositoryImpl;

import java.util.List;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepositoryImpl repository;
    private final ItemMapper mapper;

    @Override
    public ItemDtoResponse addNewItem(Long userId, ItemDtoRequest request) {
        Item item = mapper.toItem(request);
        repository.create(userId, item);

        return mapper.toResponse(item);
    }

    @Override
    public Item getItemById(Long itemId) {
        return repository.getItemById(itemId);
    }

    @Override
    public List<Item> getItemsByUserId(Long userId) {
        return repository.getItemsByUserId(userId);
    }


    @Override
    public Item update(Long userId, Long itemId, ItemDtoRequest request) {
        Item item = mapper.toItem(request);
        repository.update(userId, itemId, item);
        return item;
    }


    @Override
    public List<Item> findItemsByText(String text) {
       return repository.findItemsByText(text);
    }

}