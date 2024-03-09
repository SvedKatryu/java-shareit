package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ItemRepositoryImpl implements ItemRepository {
    private long id = 0;
    private final Map<Long, List<Item>> userItems = new HashMap<>();
    private final Map<Long, Item> items = new HashMap<>();

    private Long getNextId() {
        return ++id;
    }

    @Override
    public Item create(Long userId, Item item) {
        item.setId(getNextId());
//        item.setOwner(userId);
        items.put(item.getId(), item);

        userItems.computeIfAbsent(userId, k -> new ArrayList<>()).add(item);
        log.info("Добавили новую вещь", item);
        return item;
    }

    @Override
    public Item getItemById(Long itemId) {
        return items.getOrDefault(itemId, null);
    }

    @Override
    public List<Item> getItemsByUserId(Long userId) {
        return userItems.getOrDefault(userId, Collections.emptyList());
    }

    @Override
    public Item update(Long userId, Long itemId, Item item) {
        Item curentItem = items.get(itemId);
        if (!Objects.equals(curentItem.getOwner().getId(), userId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }
            if (item.getName() != null) {
                curentItem.setName(item.getName());
            }
            if (item.getDescription() != null) {
                curentItem.setDescription(item.getDescription());
            }
            if (item.getAvailable() != null) {
                curentItem.setAvailable(item.getAvailable());
            }
            items.put(curentItem.getId(), curentItem);
            userItems.computeIfPresent(item.getOwner().getId(), (key, userItemsList) -> {
                userItemsList.add(curentItem);
                return userItemsList;
            });
            log.info("Данные изменены");
            return curentItem;
    }

    @Override
    public List<Item> findItemsByText(String text) {
        log.info("Найдено по запросу");
        return items.values().stream()
                .filter(i -> (i.getName().toLowerCase().contains(text.toLowerCase())
                        || i.getDescription().toLowerCase().contains(text.toLowerCase()))
                        && i.getAvailable().equals(true))
                .collect(Collectors.toList());
    }
}
