package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    public User update(Long id, User user);

    public void create(User user);

    public List<User> getAll();

    public Optional<User> getUserById(Long id);

    public void delete(Long id);
}
