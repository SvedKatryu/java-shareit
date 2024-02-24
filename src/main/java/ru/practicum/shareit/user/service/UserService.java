package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.controller.dto.UserDtoRequest;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    UserDtoResponse create(UserDtoRequest request);

    User update(Long id, User user);

    List<User> getAll();

   User getUserById(Long id);

    void delete(Long id);
}
