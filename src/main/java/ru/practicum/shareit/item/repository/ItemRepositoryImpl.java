package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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
    public void create(Long userId, Item item) {
        item.setId(getNextId());
        item.setOwner(userId);
        items.put(item.getId(), item);
        userItems.compute(item.getOwner(), (key, userItemsList) -> {
            if (userItemsList == null) {
                userItemsList = new ArrayList<>();
            }
            userItemsList.add(item);
            return userItemsList;
        });

//        return item;
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
        Item curentItem = items.get(item.getId());
        if (Objects.equals(curentItem.getOwner(), userId)) {
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
            userItems.computeIfPresent(item.getOwner(), (key, userItemsList) -> {
                userItemsList.add(item);
                return userItemsList;
            });
        }
        return curentItem;
    }


    @Override
    public List<Item> findItemsByText(String text) {
        return items.values().stream()
                .filter(i -> (i.getName().contains(text) || i.getDescription().contains(text)) && i.getAvailable().equals(true))
                .collect(Collectors.toList());
    }

//    private long getId() {
//        long lastId = items.values()
//                .stream()
//                .flatMap(Collection::stream)
//                .mapToLong(Item::getId)
//                .max()
//                .orElse(0);
//        return lastId + 1;
//    }
}
