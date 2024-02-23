package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.controller.dto.UserDtoRequest;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    UserDtoResponse create(UserDtoRequest request);

    User update(User user);

    List<User> getAll();

    User getUserById(Long id);

    void delete(Long id);
}
