package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.controller.dto.UserDtoRequest;
import ru.practicum.shareit.user.controller.dto.UserDtoResponse;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.JpaUserRepository;

import javax.transaction.Transactional;
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
    public UserDtoResponse update(Long id, UserDtoRequest user) {
        User userForUpdate = mapper.toUser(user);
        User updatedUser = userRepository.save(userForUpdate);
        return mapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public List<UserDtoResponse> getAll() {
        List<User> users = userRepository.findAll();
        return users.stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDtoResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return mapper.toResponse(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        userRepository.delete(user);
    }

}