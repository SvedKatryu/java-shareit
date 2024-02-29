package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
@Slf4j
public class UserRepositoryImpl implements UserRepository {

    private long id = 0;

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emailSet = new HashSet<>();

    private Long getNextId() {
        return ++id;
    }

    public User update(Long id, User user) {
        User currentUser = getUserById(id).orElseThrow(() -> new NotFoundException("Данный пользователь не найден"));
        if (emailSet.contains(user.getEmail()) && !Objects.equals(currentUser.getEmail(), user.getEmail())) {
            throw new ConflictException("Пользователь с таким Email уже существует");
        }
        if (user.getEmail() != null) {
            emailSet.remove(currentUser.getEmail());
            currentUser.setEmail(user.getEmail());
            emailSet.add(currentUser.getEmail());
        }
        if (user.getName() != null) {
            currentUser.setName(user.getName());
        }
        users.put(id, currentUser);
        log.info("Данные пользователя изменены");
        return currentUser;
    }

    public void create(User user) {
        if (emailSet.contains(user.getEmail())) {
            throw new ConflictException("Пользователь с таким Email уже существует");
        } else {
            emailSet.add(user.getEmail());
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
        return Optional.ofNullable(users.get(id));
    }

    public void delete(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь не найден");
        }
        emailSet.remove(users.get(id).getEmail());
        users.remove(id);
        log.info("Пользователь удален");
    }
}
