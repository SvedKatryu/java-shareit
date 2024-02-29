package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.controller.dto.ItemDtoRequest;
import ru.practicum.shareit.item.controller.dto.ItemDtoResponse;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper mapper;
    private final UserRepository userRepository;

    @Override
    public ItemDtoResponse addNewItem(Long userId, ItemDtoRequest request) {
        Item item = mapper.toItem(request);
        User user = userRepository.getUserById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item createdItem = itemRepository.create(userId, item);

        return mapper.toResponse(createdItem);
    }

    @Override
    public ItemDtoResponse getItemById(Long itemId) {
        Item item = itemRepository.getItemById(itemId);
        return mapper.toResponse(item);
    }

    @Override
    public List<ItemDtoResponse> getItemsByUserId(Long userId) {
        List<Item> items = itemRepository.getItemsByUserId(userId);
        return items.stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public ItemDtoResponse update(Long userId, Long itemId, ItemDtoRequest request) {
        Item item = mapper.toItem(request);
        Item updatedItem = itemRepository.update(userId, itemId, item);
        return mapper.toResponse(updatedItem);
    }

    @Override
    public List<ItemDtoResponse> findItemsByText(String text) {
        List<Item> items = itemRepository.findItemsByText(text);
        return items.stream().map(mapper::toResponse).collect(Collectors.toList());
    }

}