package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.controller.dto.UserDtoRequest;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper mapper;

    @Override
    public UserDtoResponse create(UserDtoRequest request) {
        User user = mapper.toUser(request);
        userRepository.create(user);

        return mapper.toResponse(user);
    }

    @Override
    public UserDtoResponse update(Long id, UserDtoRequest user) {
        User userForUpdate = mapper.toUser(user);
        User updatedUser = userRepository.update(id, userForUpdate);
        return mapper.toResponse(updatedUser);
    }

    @Override
    public List<User> getAll() {
        return userRepository.getAll();
    }

    @Override
    public UserDtoResponse getUserById(Long id) {
        User user = userRepository.getUserById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return mapper.toResponse(user);
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(id);
    }

}