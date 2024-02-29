package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.controller.dto.UserDtoRequest;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    UserDtoResponse create(UserDtoRequest request);

    UserDtoResponse update(Long id, UserDtoRequest user);

    List<UserDtoResponse> getAll();

    UserDtoResponse getUserById(Long id);

    void delete(Long id);
}
