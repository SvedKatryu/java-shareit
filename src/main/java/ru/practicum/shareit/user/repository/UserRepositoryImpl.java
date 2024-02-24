package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UserRepositoryImpl implements UserRepository {

    private long id = 0;

    private final Map<Long, User> users = new HashMap<>();

    private Long getNextId() {
        return ++id;
    }

    public void update(User user) {
        //validate(user);
        if (users.containsKey(user.getId())) {

            users.put(user.getId(), user);
            log.info("Данные пользователя изменены");
        } else {
            throw new NotFoundException("Данный пользователь не найден");
        }
    }

    public void create(User user) {
        //validate(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Добавили нового пользователя", user);
    }

    public List<User> getAll() {
        log.info("Добавили всех пользователей");
        return new ArrayList<>(users.values());
    }

    public User getUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь не найден");
        }
        return users.get(id);
    }

    public void delete(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь не найден");
        }
        users.remove(id);
        log.info("Пользователь удален");
    }
}
