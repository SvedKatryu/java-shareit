package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
@Slf4j
public class UserRepositoryImpl implements UserRepository {

    private long id = 0;

    private final Map<Long, User> users = new HashMap<>();

    private Long getNextId() {
        return ++id;
    }

    public User update(Long id, User user) {
        User currentUser = getUserById(id).orElseThrow(() -> new NotFoundException("Данный пользователь не найден"));
        if (user.getEmail() != null) {
            Optional<User> someUser = users.values().stream()
                    .filter(u -> !Objects.equals(u.getId(), id))
                    .filter(u -> Objects.equals(u.getEmail(), user.getEmail())).findFirst();
            if (someUser.isPresent()) {
                throw new ValidationException("Пользователь с таким Email уже существует");
            }
            currentUser.setEmail(user.getEmail());
        }
        if (user.getName() != null) {
            currentUser.setName(user.getName());
        }
        users.put(id, currentUser);
        log.info("Данные пользователя изменены");
        return currentUser;
    }

    public void create(User user) {
        if (users.values().stream().anyMatch(u -> Objects.equals(u.getEmail(), user.getEmail()))) {
            throw new ValidationException("Пользователь с таким Email уже существует");
        } else {
            user.setId(getNextId());
            users.put(user.getId(), user);
            log.info("Добавили нового пользователя", user);
        }
    }

    public List<User> getAll() {
        log.info("Добавили всех пользователей");
        return new ArrayList<>(users.values());
    }

    public Optional<User> getUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь не найден");
        }
        return Optional.ofNullable(users.get(id));
    }

    public void delete(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь не найден");
        }
        users.remove(id);
        log.info("Пользователь удален");
    }
}
