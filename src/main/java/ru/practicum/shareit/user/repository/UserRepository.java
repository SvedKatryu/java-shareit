package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

public interface UserRepository {
    public void update(User user);

    public void create(User user);

    public List<User> getAll();

    public User getUserById(Long id);

    public void delete(Long id);
}
