package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.controller.dto.UserDtoRequest;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final JpaUserRepository userRepository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserDtoResponse create(UserDtoRequest request) {
        User user = mapper.toUser(request);
        userRepository.save(user);

        return mapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserDtoResponse update(Long id, UserDtoRequest requestUser) {
        User userForUpdate = userRepository.findById(id).orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID%d не найден", id)));
        if (requestUser.getName() != null) userForUpdate.setName(requestUser.getName());
        if (requestUser.getEmail() != null) userForUpdate.setEmail(requestUser.getEmail());
        User updatedUser = userRepository.save(userForUpdate);
        return mapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDtoResponse> getAll() {
        List<User> users = userRepository.findAll();
        return users.stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDtoResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID%d не найден", id)));
        return mapper.toResponse(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID%d не найден", id)));
        userRepository.delete(user);
    }

}