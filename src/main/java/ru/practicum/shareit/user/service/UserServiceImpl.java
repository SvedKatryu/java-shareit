package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.controller.dto.UserDtoRequest;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepositoryImpl;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepositoryImpl userRepository;
    private final UserMapper mapper;

    @Override
    public UserDtoResponse create(UserDtoRequest request) {
        User user = mapper.toUser(request);
        userRepository.create(user);

        return mapper.toResponse(user);
    }

    @Override
    public User update(Long id, User user) {
        return userRepository.update(id, user);
    }

    @Override
    public List<User> getAll() {
        return userRepository.getAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.getUserById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(id);
    }

}